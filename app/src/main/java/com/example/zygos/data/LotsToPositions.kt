package com.example.zygos.data

import com.example.zygos.data.database.Lot
import com.example.zygos.data.database.LotWithTransactions
import com.example.zygos.data.database.TransactionType
import com.example.zygos.ui.components.formatDollarNoSymbol
import kotlin.math.abs


/** There is one cash lot per account, but "All Accounts" uses them all **/
fun getCashPosition(openLots: List<LotWithTransactions>): Position {
    return openLots.map {
        LotPosition(
            /** Identifiers **/
            account = it.lot.account,
            ticker = "CASH",
            type = PositionType.CASH,
            /** Basis and returns **/
            shares = it.lot.sharesOpen, // this makes cashEffect correct. Net transactions
            priceOpen = -1,
            realizedOpen = it.lot.realizedClosed, // interest
            realizedClosed = 0, // MUST subtract all other cashEffects
        )
    }.reduce(Position::plus)
}


/**
 * Converts lots to positions, collating options.
 */
fun getTickerPositions(openLots: List<LotWithTransactions>): List<Position> {
    var stockPosition: Position? = null
    val optionLots = mutableListOf<LotWithTransactions>()

    for (lot in openLots) {
        if (lot.openTransaction.type == TransactionType.STOCK) {
            val pos = stockLotToPosition(lot)
            if (stockPosition == null) stockPosition = pos
            else stockPosition += pos
        } else if (lot.openTransaction.type.isOption) {
            optionLots.add(lot)
        } else throw RuntimeException("Unknown lot type ${lot.openTransaction.type}")
    }

    val options = collateOptions(optionLots)

    return if (stockPosition != null) listOf(stockPosition) + options else options
}


fun collateOptions(optionLots: List<LotWithTransactions>): List<Position> {
    /** Add options from the end (should be time-ordered). When on a short option,
    invalidate corresponding long options by decrementing their shares. Fees, rounding, and
    realizedClosed should be added to whatever exhausts the shares. **/
    val unmatchedShares = optionLots.map { it.lot.sharesOpen }.toMutableList()
    val out = mutableListOf<Position>()

    for (i in optionLots.lastIndex downTo 0) {
        if (unmatchedShares[i] <= 0L) continue

        /** Create spreads from as many shares as possible **/
        if (optionLots[i].openTransaction.type.isShort) {
            for (iLong in i - 1 downTo 0) {
                if (unmatchedShares[i] <= 0L) break
                if (unmatchedShares[iLong] <= 0L) continue
                if (!matches(optionLots[i], optionLots[iLong])) continue

                val shares = minOf(unmatchedShares[i], unmatchedShares[iLong])
                unmatchedShares[i] -= shares
                unmatchedShares[iLong] -= shares

                out.add(makeSpreadPosition(
                    shares,
                    optionLots[i],
                    optionLots[iLong],
                    unmatchedShares[i] == 0L,
                    unmatchedShares[iLong] == 0L
                ))
            }
        }
        /** Remaining options are single leg **/
        if (unmatchedShares[i] > 0L) {
            out.add(makeSingleOptionPosition(unmatchedShares[i], optionLots[i]))
        }
    }

    return out
}


private fun matches(short: LotWithTransactions, long: LotWithTransactions): Boolean {
    val isCall =
        short.openTransaction.type == TransactionType.CALL_SHORT && long.openTransaction.type == TransactionType.CALL_LONG
    val isPut =
        short.openTransaction.type == TransactionType.PUT_SHORT && long.openTransaction.type == TransactionType.PUT_LONG
    return (isCall || isPut) && short.openTransaction.expiration <= long.openTransaction.expiration
}


fun stockLotToPosition(lot: LotWithTransactions): LotPosition {
    return LotPosition(
        /** Identifiers **/
        account = lot.lot.account,
        ticker = lot.lot.ticker,
        type = PositionType.STOCK,
        /** Per share **/
        shares = lot.lot.sharesOpen,
        priceOpen = lot.openTransaction.price,
        /** Basis and returns **/
        taxBasis = 0, // TODO
        feesAndRounding = lot.lot.feesAndRounding,
        realizedOpen = lot.lot.dividendsPerShare * lot.lot.sharesOpen,
        realizedClosed = lot.lot.realizedClosed,
    )
}

/** This is the final position generated from the lot, so include fees, rounding, and realized here **/
fun makeSingleOptionPosition(shares: Long, lot: LotWithTransactions): LotPosition {
    val type = when (lot.openTransaction.type) {
        TransactionType.CALL_LONG -> PositionType.CALL_LONG
        TransactionType.PUT_LONG -> PositionType.PUT_LONG
        TransactionType.CALL_SHORT -> PositionType.COVERED_CALL
        TransactionType.PUT_SHORT -> PositionType.CASH_SECURED_PUT
        else -> throw RuntimeException("makeSingleOptionPosition() passed unknown transaction type ${lot.openTransaction.type}")
    }
    val collateral =
        if (type == PositionType.CASH_SECURED_PUT) shares * lot.openTransaction.strike else 0

    return LotPosition(
        /** Identifiers **/
        account = lot.lot.account,
        ticker = lot.lot.ticker,
        type = type,
        /** Per share **/
        shares = shares,
        priceOpen = lot.openTransaction.price,
        /** Basis and returns **/
        taxBasis = 0, // TODO
        feesAndRounding = lot.lot.feesAndRounding,
        realizedOpen = lot.lot.dividendsPerShare * shares,
        realizedClosed = lot.lot.realizedClosed,
        /** Options **/
        expiration = lot.openTransaction.expiration,
        strike = lot.openTransaction.strike,
        collateral = collateral,
    )
}


fun makeSpreadPosition(
    shares: Long,
    short: LotWithTransactions,
    long: LotWithTransactions,
    shortExhausted: Boolean,
    longExhausted: Boolean,
): Position {
    val isLong = long.openTransaction.price > short.openTransaction.price
    val type = when (short.openTransaction.type) {
        TransactionType.CALL_SHORT -> if (isLong) PositionType.CALL_DEBIT_SPREAD else PositionType.CALL_CREDIT_SPREAD
        TransactionType.PUT_SHORT -> if (isLong) PositionType.PUT_DEBIT_SPREAD else PositionType.PUT_CREDIT_SPREAD
        else -> throw RuntimeException("makeSpreadPosition() passed a non-short lot: ${short.openTransaction.type}")
    }
    val primaryStrike = formatDollarNoSymbol((if (isLong) long.openTransaction.strike else short.openTransaction.strike).toFloatDollar())
    val ancillaryStrike = formatDollarNoSymbol((if (!isLong) long.openTransaction.strike else short.openTransaction.strike).toFloatDollar())
    val collateral = if (isLong) 0 else abs(long.openTransaction.strike - short.openTransaction.strike) * shares

    /** First create individual positions **/
    var longPosition = makeSingleOptionPosition(shares, long)
    var shortPosition = makeSingleOptionPosition(shares, short).copy(collateral = collateral)

    /** Unless this is the final position generated from the lot, don't include fees, rounding, and realized here **/
    if (!longExhausted) longPosition = longPosition.copy(feesAndRounding = 0, realizedClosed = 0)
    if (!shortExhausted) shortPosition = shortPosition.copy(feesAndRounding = 0, realizedClosed = 0)

    /** Spread is just the sum **/
    return AggregatePosition(
        realizedClosedExtra = 0L,
        type = type,
        shares = shares,
        priceOpen = abs(long.openTransaction.price - short.openTransaction.price),
        instrumentName = "${shortPosition.ticker} $type ${shortPosition.expiration} $primaryStrike-$ancillaryStrike",
        subPositions = listOf(longPosition, shortPosition)
    )
}



