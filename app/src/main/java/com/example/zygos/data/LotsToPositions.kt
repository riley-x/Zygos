package com.example.zygos.data

import com.example.zygos.data.database.Lot
import com.example.zygos.data.database.LotWithTransactions
import com.example.zygos.data.database.TransactionType
import com.example.zygos.ui.components.formatDateInt
import com.example.zygos.ui.components.formatDollarNoSymbol


fun getCashPosition(lot: Lot): Position {
    return Position(
        /** Identifiers **/
        account = lot.account,
        ticker = "CASH",
        type = PositionType.CASH,
        /** Basis and returns **/
        costBasis = lot.sharesOpen.toFloatDollar(),
        realizedClosed = lot.realizedClosed.toFloatDollar(),
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

    var stockPosition = Position(
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

    return TickerPosition(
        realizedClosed = realizedClosed.toFloatDollar(),
        stock = stockPosition,
        coveredCalls = coveredCalls,
        longOptions = longOptions,
        shortOptions = shortOptions,
    )
}


fun collateOptions(optionLots: List<LotWithTransactions>): List<List<Position>> {

    /** Outputs **/
    val coveredCalls = mutableListOf<Position>()
    val longOptions = mutableListOf<Position>() // includes debit spreads
    val shortOptions = mutableListOf<Position>() // includes credit spreads

    /** Add options from the end (should be time-ordered). When on a short option,
        invalidate corresponding long options by decrementing their shares. **/
    val shares = optionLots.map { it.lot.sharesOpen }.toMutableList()

    for (i in optionLots.lastIndex downTo 0) {
        if (shares[i] == 0L) continue

        /** Standalone long options **/
        if (!optionLots[i].openTransaction.type.isShort) {
            longOptions.add(longOptionLotToPosition(shares[i], optionLots[i]))
        }
        /** Short option -- needs matching to collateral **/
        else {

        }
    }

    return listOf(coveredCalls, longOptions, shortOptions)
}


fun stockLotToPosition(lot: LotWithTransactions): Position {
    return Position(
        /** Identifiers **/
        account = lot.lot.account,
        ticker = lot.lot.ticker,
        type = PositionType.STOCK,
        /** Per share **/
        shares = lot.lot.sharesOpen,
        priceOpen = lot.openTransaction.price.toFloatDollar(),
        /** Basis and returns **/
        costBasis = lot.lot.costBasisOpen.toFloatDollar(),
        taxBasis = lot.lot.taxBasisOpen.toFloatDollar(),
        realizedOpen = lot.lot.realizedOpen.toFloatDollar(),
        realizedClosed = lot.lot.realizedClosed.toFloatDollar(),
    )
}



fun longOptionLotToPosition(sharesLeft: Long, lot: LotWithTransactions): Position {
    val shareFraction = sharesLeft.toFloat() / lot.lot.sharesOpen // TODO fees and rounding are handled correctly?
    val type = when (lot.openTransaction.type) {
        TransactionType.CALL_LONG -> PositionType.CALL_LONG
        else -> PositionType.PUT_LONG
    }
    return Position(
        /** Identifiers **/
        account = lot.lot.account,
        ticker = lot.lot.ticker,
        type = type,
        /** Per share **/
        shares = sharesLeft,
        priceOpen = lot.openTransaction.price.toFloatDollar(),
        /** Basis and returns **/
        costBasis = lot.lot.costBasisOpen.toFloatDollar() * shareFraction,
        taxBasis = lot.lot.taxBasisOpen.toFloatDollar() * shareFraction,
        realizedOpen = lot.lot.realizedOpen.toFloatDollar() * shareFraction,
        realizedClosed = lot.lot.realizedClosed.toFloatDollar() * shareFraction,
        /** Options **/
        name = "$type ${formatDollarNoSymbol(lot.openTransaction.strike.toFloatDollar())} ${formatDateInt(lot.openTransaction.expiration)}",
        collateral = 0f,
    )
}


