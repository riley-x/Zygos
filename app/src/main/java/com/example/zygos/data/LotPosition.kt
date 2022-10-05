package com.example.zygos.data

import androidx.compose.runtime.Immutable


enum class PositionType(val displayName: String, val isOption: Boolean = false, val isShort: Boolean = false) {
    CASH("Cash"),
    STOCK("Stock"),
    CALL_LONG("Call", true),
    PUT_LONG("Put", true),
    CALL_DEBIT_SPREAD("Call Debit Spread", true), // all spreads can include diagonals/calendars
    CALL_CREDIT_SPREAD("Call Credit Spread", true),
    PUT_DEBIT_SPREAD("Put Debit Spread", true),
    PUT_CREDIT_SPREAD("Put Credit Spread", true),
    CASH_SECURED_PUT("CSP", true, true),
    COVERED_CALL("Covered Call", true, true),
    BOND("Bond"),
    NONE("None");

    override fun toString(): String {
        return displayName
    }
}



/**
 * Summarizes the current holdings and returns of a lot (or multiple lots combined together)
 *
 * @param shares is always positive
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
data class LotPosition(
    /** Identifiers **/
    val account: String = "",
    val ticker: String = "",
    val type: PositionType = PositionType.NONE,
    /** Per share **/
    val shares: Long = 0,
    val priceOpen: Long = 0,
    /** Basis and returns **/
    val costBasis: Long = 0,
    val taxBasis: Long = 0,
    val realizedOpen: Long = 0,
    val realizedClosed: Long = 0,
    /** Options **/
    val expiration: String = "",
    val strike: String = "",
    val collateral: Long = 0,
    val cashEffect: Long = realizedOpen + realizedClosed + collateral +
            if (type == PositionType.CASH) costBasis else -costBasis // this is valid for everything but covered calls
) {
    val name = if (type.isOption) "$type $strike $expiration" else ""
    val realized = realizedOpen + realizedClosed
    fun unrealized(priceCurrent: Long?) = ((priceCurrent ?: priceOpen) - priceOpen) * shares * if (type.isShort) -1 else 1
    fun returns(priceCurrent: Long?) = realized + unrealized(priceCurrent)
    fun returnsPercent(priceCurrent: Long?) = (realizedOpen + unrealized(priceCurrent)).toDouble() / costBasis
    fun equity(priceCurrent: Long?) =
        if (type == PositionType.CASH) cashEffect
        else (priceCurrent ?: priceOpen) * shares * (if (type.isShort) -1 else 1)

    operator fun plus(b: LotPosition): LotPosition {
        if (type == PositionType.STOCK && b.type == PositionType.STOCK && ticker == b.ticker) {
            return LotPosition(
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
        else throw RuntimeException("Position.plus() trying to add non-stock or different ticker positions!")
    }
}


class TickerPosition(
    realizedFromClosedLots: Long = 0,
    val stock: LotPosition,
    val coveredCalls: List<LotPosition> = emptyList(),
    val longOptions: List<LotPosition> = emptyList(), // includes debit spreads
    val shortOptions: List<LotPosition> = emptyList(), // includes credit spreads
) {
    private fun sumWith(longOnly: Boolean = false, fn: LotPosition.() -> Long): Long {
        var x = stock.fn() + coveredCalls.sumOf(fn) + longOptions.sumOf(fn)
        if (!longOnly) x += shortOptions.sumOf(fn)
        return x
    }
    private fun sumWith(prices: Map<String, Long>, longOnly: Boolean = false, fn: LotPosition.(Long?) -> Long): Long {
        var x = stock.fn(prices[stock.ticker]) +
                coveredCalls.sumOf { it.fn(prices[it.name]) } +
                longOptions.sumOf { it.fn(prices[it.name]) }
        if (!longOnly) x += shortOptions.sumOf { it.fn(prices[it.name]) }
        return x
    }

    val account = stock.account
    val ticker = stock.ticker
    val cashEffect = sumWith { cashEffect }
    val realizedClosed = realizedFromClosedLots + sumWith { realizedClosed }
    val realizedLong = sumWith(true) { realized }
    val realizedOpenLong = sumWith(true) { realizedOpen }
    val costBasisLong = sumWith(true) { costBasis }
    fun unrealizedLong(prices: Map<String, Long> = emptyMap()) = sumWith(prices, true) { unrealized(it) }
    fun returnsLong(prices: Map<String, Long> = emptyMap()) = sumWith(prices, true) { returns(it) }
    fun returnsPercentLong(prices: Map<String, Long> = emptyMap()) = returnsLong(prices).toDouble() / costBasisLong
    fun equityLong(prices: Map<String, Long> = emptyMap()) = sumWith(prices, true) { equity(it) }
}