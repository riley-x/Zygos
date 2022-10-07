package com.example.zygos.data

import androidx.compose.runtime.Immutable


enum class PositionType(val displayName: String, val isOption: Boolean = false, val isShort: Boolean = false) {
    CASH("Cash"),
    STOCK("Stock"),
    CALL_LONG("Call", true),
    PUT_LONG("Put", true),
    CALL_DEBIT_SPREAD("Call Debit Spread", true), // all spreads can include diagonals/calendars
    CALL_CREDIT_SPREAD("Call Credit Spread", true, true),
    PUT_DEBIT_SPREAD("Put Debit Spread", true),
    PUT_CREDIT_SPREAD("Put Credit Spread", true, true),
    CASH_SECURED_PUT("CSP", true, true),
    COVERED_CALL("Covered Call", true, true),
    BOND("Bond"),
    NONE("None");

    override fun toString(): String {
        return displayName
    }
}

/**
 * Summarizes the current holdings and returns of a lot (or multiple lots combined together).
 * Fields that depend on the current market price, like unrealized, are implemented as functions.
 *
 * @param shares is always positive
 *
 * @param costBasis Used for % return calculation, the amount of cash needed to open the position.
 * For long positions this is essentially [shares] * [priceOpen], but can be different due to
 * rounding and fees. For short position this includes the cash collateral needed.
 * @param taxBasis Actual basis used for taxes. Usually the same but could be adjusted for wash sales.
 * @param feesAndRounding All other quantities are divisible by [shares]. This field keeps track of
 * all rounding errors and fees. Note the fees are not split by share amount if this position is
 * half-closed, and are kept until the entire position is fully closed.
 * @param realizedOpen Realized returns from open positions, i.e. dividends. Note STO proceeds and
 * not included here, see [cashEffect] instead. These returns are included in % return calculations.
 * @param realizedClosed Realized returns from closed positions. These returns are not included in
 * % return calculations.
 *
 * @param collateral For short positions, amount of cash collateral.
 * @param instrumentName A name to lookup the current price with from a map of prices. This is set
 *
 * @param subPositions Constituent positions, if any. For example a stock position can consist of
 * many lot positions.
 * to some display name for compound positions, use instead the individual sub-positions.
 */
@Immutable
data class LotPosition(
    /** Identifiers **/
    val account: String = "",
    val ticker: String = "",
    val type: PositionType = PositionType.NONE,
    /** Per share **/
    val shares: Long = 0,
    val priceOpen: Long = 0,
    /** Basis and returns **/
    val taxBasis: Long = 0,
    val feesAndRounding: Long = 0,
    val realizedOpen: Long = 0,
    val realizedClosed: Long = 0,
    /** Options **/
    val expiration: Int = 0,
    val strike: Long = 0,
    val collateral: Long = 0,
    val instrumentName: String = if (type.isOption) "$ticker $type $expiration $strike" else ticker, // this is modified for spreads. always use subPositions for unrealized
    /** Sub positions **/
    val subPositions: List<LotPosition> = emptyList(),
) {
    /** Derived values **/
    val realized = realizedOpen + realizedClosed
    val cashEffect: Long
    val costBasis: Long
    init {
        if (subPositions.isEmpty()) {
            val purchaseValue = feesAndRounding + shares * priceOpen * if (type.isShort) -1 else 1
            cashEffect = realized - purchaseValue
            costBasis = collateral + purchaseValue
        } else {
            cashEffect = subPositions.sumOf(LotPosition::cashEffect)
            costBasis = subPositions.sumOf(LotPosition::costBasis)
        }
    }

    /** Single lot (not composite) functions. These assume subPositions.isEmpty() **/
    private fun unrealized(priceCurrent: Long?) = ((priceCurrent ?: priceOpen) - priceOpen) * shares * if (type.isShort) -1 else 1
    private fun returns(priceCurrent: Long?) = realized + unrealized(priceCurrent)
    private fun equity(priceCurrent: Long?) =
        if (type == PositionType.CASH) cashEffect
        else (priceCurrent ?: priceOpen) * shares * (if (type.isShort) -1 else 1)

    /** Aggregator **/
    private fun sumSubPositions(
        prices: Map<String, Long>,
        fn: LotPosition.(Long?) -> Long
    ): Long {
        return if (subPositions.isEmpty()) fn(prices[instrumentName])
        else subPositions.sumOf { it.sumSubPositions(prices, fn) }
    }

    /** Public functions **/
    fun unrealized(prices: Map<String, Long> = emptyMap()) = sumSubPositions(prices) { unrealized(it) }
    fun returns(prices: Map<String, Long> = emptyMap()) = sumSubPositions(prices) { returns(it) }
    fun equity(prices: Map<String, Long> = emptyMap()) = sumSubPositions(prices) { equity(it) }
    fun returnsPercent(prices: Map<String, Long>) = if (type == PositionType.CASH || costBasis == 0L) 0f else (realizedOpen + unrealized(prices)).toFloat() / costBasis

    /** Forms a compound position with the constituents as subPositions **/
    operator fun plus(b: LotPosition): LotPosition {
        val sameStock = type == PositionType.STOCK && b.type == PositionType.STOCK && ticker == b.ticker
        fun <T> ifEqual(default: T, field: LotPosition.() -> T) = if (this.field() == b.field()) this.field() else default
        return LotPosition(
            /** Identifiers **/
            account = ifEqual("") { account },
            ticker = ifEqual("") { ticker },
            type = ifEqual(PositionType.NONE) { type },
            /** Per share **/
            shares = if (sameStock) shares + b.shares else 0,
            priceOpen = if (sameStock) (priceOpen * shares + b.priceOpen * b.shares) / (shares + b.shares) else 0, // this rounds to the nearest .01 cents
            /** Basis and returns **/
            taxBasis = taxBasis + b.taxBasis,
            feesAndRounding = feesAndRounding + b.feesAndRounding,
            realizedOpen = realizedOpen + b.realizedOpen,
            realizedClosed = realizedClosed + b.realizedClosed,
            /** Sub positions **/
            subPositions = subPositions.ifEmpty { listOf(this) } + b.subPositions.ifEmpty { listOf(b) }
        )
    }
}