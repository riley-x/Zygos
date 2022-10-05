package com.example.zygos.data

import com.example.zygos.data.database.Lot
import com.example.zygos.data.database.LotWithTransactions
import com.example.zygos.data.database.TransactionType
import com.example.zygos.ui.components.formatDateInt
import com.example.zygos.ui.components.formatDollarNoSymbol
import kotlin.math.abs


fun getCashPosition(lot: Lot): LotPosition {
    return LotPosition(
        /** Identifiers **/
        account = lot.account,
        ticker = "CASH",
        type = PositionType.CASH,
        /** Basis and returns **/
        costBasis = lot.sharesOpen,
        realizedClosed = lot.realizedClosed,
    )
}


/**
 * Converts lots from a single ticker to a ticker position
 */
fun getTickerPosition(
    account: String,
    ticker: String,
    openLots: List<LotWithTransactions>,
    realizedClosed: Long
): TickerPosition {
    if (openLots.isEmpty()) throw UnsupportedOperationException("getPositions() passed an empty list")

    var stockPosition = LotPosition(
        ticker = ticker,
        account = account,
        type = PositionType.STOCK,
    )
    val optionLots = mutableListOf<LotWithTransactions>()

    for (lot in openLots) {
        if (lot.openTransaction.type == TransactionType.STOCK) {
            stockPosition += stockLotToPosition(lot)
        } else if (lot.openTransaction.type.isOption) {
            optionLots.add(lot)
        } else throw RuntimeException("Unknown lot type ${lot.openTransaction.type}")
    }

    val (coveredCalls, longOptions, shortOptions) = collateOptions(optionLots)
    if (coveredCalls.sumOf(LotPosition::shares) > stockPosition.shares)
        throw NotImplementedError("Naked Shorts")

    return TickerPosition(
        realizedFromClosedLots = realizedClosed,
        stock = stockPosition,
        coveredCalls = coveredCalls,
        longOptions = longOptions,
        shortOptions = shortOptions,
    )
}


fun collateOptions(optionLots: List<LotWithTransactions>): List<List<LotPosition>> {

    /** Outputs **/
    val coveredCalls = mutableListOf<LotPosition>()
    val longOptions = mutableListOf<LotPosition>() // includes debit spreads
    val shortOptions = mutableListOf<LotPosition>() // includes credit spreads

    /** Add options from the end (should be time-ordered). When on a short option,
        invalidate corresponding long options by decrementing their shares. Fees, rounding, and
        realizedClosed should be added to whatever exhausts the shares. **/
    val unmatchedShares = optionLots.map { it.lot.sharesOpen }.toMutableList()

    for (i in optionLots.lastIndex downTo 0) {
        if (unmatchedShares[i] <= 0L) continue

        /** Create spreads **/
        if (optionLots[i].openTransaction.type.isShort) {
            for (iLong in i-1 downTo 0) {
                if (unmatchedShares[i] <= 0L) break
                if (unmatchedShares[iLong] < 0 || !matches(optionLots[i], optionLots[iLong])) continue

                val shares = minOf(unmatchedShares[i], unmatchedShares[iLong])
                unmatchedShares[i] -= shares
                unmatchedShares[iLong] -= shares

                val (pos, isLong) = makeSpreadPosition(shares, optionLots[i], optionLots[iLong])
                (if (isLong) longOptions else shortOptions).add(pos)
            }
        }
        /** Single leg options **/
        if (unmatchedShares[i] > 0L) {
            val pos = makeSingleOptionPosition(unmatchedShares[i], optionLots[i])
            when (pos.type) {
                PositionType.COVERED_CALL -> coveredCalls
                PositionType.CASH_SECURED_PUT -> shortOptions
                else -> longOptions
            }.add(pos)
        }
    }

    return listOf(coveredCalls, longOptions, shortOptions)
}



private fun matches(short: LotWithTransactions, long: LotWithTransactions) : Boolean {
    val isCall = short.openTransaction.type == TransactionType.CALL_SHORT && long.openTransaction.type == TransactionType.CALL_LONG
    val isPut = short.openTransaction.type == TransactionType.PUT_SHORT && long.openTransaction.type == TransactionType.PUT_LONG
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
        costBasis = lot.lot.sharesOpen * lot.openTransaction.price + lot.lot.feesAndRounding,
        taxBasis = 0, // TODO
        realizedOpen = lot.lot.dividendsPerShare * lot.lot.sharesOpen,
        realizedClosed = lot.lot.realizedClosed,
    )
}

/** This is the final position generated from the lot, so include fees, rounding, and realized here **/
fun makeSingleOptionPosition(shares: Long, lot: LotWithTransactions): LotPosition {
    val type: PositionType
    var costBasis = 0L
    var collateral = 0L
    var cashEffect = 0L
    when (lot.openTransaction.type) {
        TransactionType.CALL_LONG -> {
            type = PositionType.CALL_LONG
            costBasis = shares * lot.openTransaction.price + lot.lot.feesAndRounding
        }
        TransactionType.PUT_LONG -> {
            type = PositionType.PUT_LONG
            costBasis = shares * lot.openTransaction.price + lot.lot.feesAndRounding
        }
        TransactionType.CALL_SHORT -> {
            type = PositionType.COVERED_CALL
            cashEffect = shares * lot.openTransaction.price - lot.lot.feesAndRounding
        }
        TransactionType.PUT_SHORT -> {
            type = PositionType.CASH_SECURED_PUT
            collateral = shares * lot.openTransaction.strike
            costBasis = collateral - shares * lot.openTransaction.price + lot.lot.feesAndRounding
        }
        else -> throw RuntimeException("makeSingleOptionPosition() passed unknown transaction type ${lot.openTransaction.type}")
    }

    return LotPosition(
        /** Identifiers **/
        account = lot.lot.account,
        ticker = lot.lot.ticker,
        type = type,
        /** Per share **/
        shares = shares,
        priceOpen = lot.openTransaction.price,
        /** Basis and returns **/
        costBasis = costBasis,
        taxBasis = 0, // TODO
        realizedOpen = lot.lot.dividendsPerShare * shares,
        realizedClosed = lot.lot.realizedClosed,
        /** Options **/
        strike = formatDollarNoSymbol(lot.openTransaction.strike.toFloatDollar()),
        expiration = formatDateInt(lot.openTransaction.expiration),
        collateral = collateral,
        cashEffect = cashEffect
    )
}


fun makeSpreadPosition(shares: Long, short: LotWithTransactions, long: LotWithTransactions): Pair<LotPosition, Boolean> {
    val isLong = long.openTransaction.price > short.openTransaction.price
    val type = when (short.openTransaction.type) {
        TransactionType.CALL_SHORT -> if (isLong) PositionType.CALL_DEBIT_SPREAD else PositionType.CALL_CREDIT_SPREAD
        TransactionType.PUT_SHORT -> if (isLong) PositionType.PUT_DEBIT_SPREAD else PositionType.PUT_CREDIT_SPREAD
        else -> throw RuntimeException("makeSpreadPosition() passed a non-short lot: ${short.openTransaction.type}")
    }

    val strike1 = if (isLong) long.openTransaction.strike else short.openTransaction.strike
    val strike2 = if (isLong) short.openTransaction.strike else long.openTransaction.strike
    val price = long.openTransaction.price - short.openTransaction.price
    val collateral = if (isLong) 0 else abs(strike1 - strike2) * shares

    /** If this is the final position generated from the lot, include fees, rounding, and realized here **/
    var fees = 0L
    var realizedClosed = 0L
    if (long.lot.sharesOpen == shares) {
        fees += long.lot.feesAndRounding
        realizedClosed += long.lot.realizedClosed
    }
    if (short.lot.sharesOpen == shares) {
        fees += short.lot.feesAndRounding
        realizedClosed += short.lot.realizedClosed
    }

    val pos = LotPosition(
        /** Identifiers **/
        account = short.lot.account,
        ticker = short.lot.ticker,
        type = type,
        /** Per share **/
        shares = shares,
        priceOpen = abs(price),
        /** Basis and returns **/
        costBasis = collateral + (price * shares + fees),
        taxBasis = 0, // TODO
        realizedOpen = long.lot.dividendsPerShare * shares + short.lot.dividendsPerShare * shares,
        realizedClosed = realizedClosed,
        /** Options **/
        strike = "${formatDollarNoSymbol(strike1.toFloatDollar())}-${formatDollarNoSymbol(strike2.toFloatDollar())}",
        expiration = formatDateInt(short.openTransaction.expiration),
        collateral = collateral,
    )

    return Pair(pos, isLong)
}



