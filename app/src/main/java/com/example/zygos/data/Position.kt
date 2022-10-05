package com.example.zygos.data

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.example.zygos.data.database.LotWithTransactions
import com.example.zygos.data.database.TransactionType


enum class PositionType(val displayName: String, val isShort: Boolean = false) {
    CASH("Cash"),
    STOCK("Stock"),
    CALL_LONG("Call"),
    PUT_LONG("Put"),
    CALL_DEBIT_SPREAD("Call Debit Spread"),
    PUT_DEBIT_SPREAD("Put Debit Spread"),
    SHORT_OPTION("Short Option", true), // includes credit spreads, but not covered calls
    COVERED_CALL("Covered Call", true), // unique since they have shares as collateral
    BOND("Bond"),
    NONE("None");

    override fun toString(): String {
        return displayName
    }
}



/**
 * Summarizes the current holdings and returns of a position, whether the entire account or a single
 * lot.
 *
 * @param shares should be negative for short positions!
 *
 * @param costBasis Used for % return calculation, the amount of cash needed to open the position.
 * For long positions this is essentially [shares] * [priceOpen], but can be different due to
 * rounding and fees. For short position this includes the cash collateral needed.
 * @param taxBasis Actual basis used for taxes. Usually the same but could be adjusted for wash sales.
 * @param realizedOpen Realized returns from open positions, i.e. dividends. Note STO proceeds and
 * not included here, see [cashEffect] instead. These returns are included in % return calculations.
 * @param realizedClosed Realized returns from closed positions. These returns are not included in
 * % return calculations.
 *
 * @param subPositions Constituent positions, if any. For example a stock position can consist of many lot positions.
 * @param name For options positions, a name to summarize the position
 * @param collateral For short positions, amount of cash collateral. This is usually [shares] * [strike].
 */
@Immutable
data class Position(
    /** Identifiers **/
    val account: String = "",
    val ticker: String = "",
    val type: PositionType = PositionType.NONE,
    /** Per share **/
    val shares: Long = 0,
    val priceOpen: Float = 0f,
    /** Basis and returns **/
    val costBasis: Float = 0f,
    val taxBasis: Float = 0f,
    val realizedOpen: Float = 0f,
    val realizedClosed: Float = 0f,
    /** Options **/
    val name: String = "",
    val collateral: Float = 0f,
) {
    val realized = realizedOpen + realizedClosed
    val cashEffect = realized + collateral + if (type == PositionType.CASH) costBasis else -costBasis // works for short positions too!
    fun unrealized(priceCurrent: Float) = (priceCurrent - priceOpen) * shares
    fun returns(priceCurrent: Float) = realized + unrealized(priceCurrent)
    fun returnsPercent(priceCurrent: Float) = (realizedOpen + unrealized(priceCurrent)) / costBasis
    fun equity(priceCurrent: Float) = if (type == PositionType.CASH) cashEffect else priceCurrent * shares

    operator fun plus(b: Position): Position {
        if (type == PositionType.STOCK && b.type == PositionType.STOCK && ticker == b.ticker) {
            return Position(
                /** Identifiers **/
                account = if (account == b.account) account else "",
                ticker = ticker,
                type = type,
                /** Per share **/
                shares = shares + b.shares,
                priceOpen = (priceOpen * shares + b.priceOpen * b.shares) / (shares + b.shares),
                /** Basis and returns **/
                costBasis = costBasis + b.costBasis,
                taxBasis = taxBasis + b.taxBasis,
                realizedOpen = realizedOpen + b.realizedOpen,
                realizedClosed = realizedClosed + b.realizedClosed,
            )
        }
        return Position(
            account = if (account == b.account) account else "",
            ticker = if (ticker == b.ticker) ticker else "",
            costBasis = costBasis + b.costBasis,
            taxBasis = taxBasis + b.taxBasis,
            realizedOpen = realizedOpen + b.realizedOpen,
            realizedClosed = realizedClosed + b.realizedClosed,
            collateral = collateral + b.collateral, // used by cashEffect
        )
    }

}


class TickerPosition(
    val realizedClosed: Float,
    val stock: Position,
    val coveredCalls: List<Position>,
    val longOptions: List<Position>, // includes debit spreads
    val shortOptions: List<Position>, // includes credit spreads
) {
    val total: Position
    init {
        val p = stock + coveredCalls.reduce(Position::plus) + longOptions.reduce(Position::plus) + shortOptions.reduce(Position::plus)
        total = p.copy(realizedClosed = p.realizedClosed + realizedClosed)
    }
}