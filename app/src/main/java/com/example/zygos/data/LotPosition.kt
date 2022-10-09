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
interface Position {
    /** Identifiers **/
    val account: String
    val ticker: String
    val type: PositionType
    /** Per share **/
    val shares: Long
    val priceOpen: Long
    /** Basis **/
    val cashEffect: Long
    val costBasis: Long
    val taxBasis: Long
    val feesAndRounding: Long
    /** Returns **/
    val realizedOpen: Long
    val realizedClosed: Long
    val realized: Long
    fun unrealized(prices: Map<String, Long>): Long
    fun returns(prices: Map<String, Long>): Long
    fun equity(prices: Map<String, Long>): Long
    fun returnsPercent(prices: Map<String, Long>): Float
    /** Options **/
    val expiration: Int
    val strike: Long
    val collateral: Long
    val instrumentName: String
    /** Sub-positions **/
    val subPositions: List<Position>
}


data class LotPosition(
    /** Identifiers **/
    override val account: String = "",
    override val ticker: String = "",
    override val type: PositionType = PositionType.NONE,
    /** Per share **/
    override val shares: Long = 0,
    override val priceOpen: Long = 0,
    /** Basis **/
    override val taxBasis: Long = 0,
    override val feesAndRounding: Long = 0,
    /** Returns **/
    override val realizedOpen: Long = 0,
    override val realizedClosed: Long = 0,
    /** Options **/
    override val expiration: Int = 0,
    override val strike: Long = 0,
    override val collateral: Long = 0,
    override val instrumentName: String = if (type.isOption) "$ticker $type $expiration $strike" else ticker, // this is modified for spreads. always use subPositions for unrealized
) : Position {
    /** Derived values **/
    override val subPositions = emptyList<Position>()
    override val realized = realizedOpen + realizedClosed
    override val cashEffect: Long
    override val costBasis: Long
    init {
        val purchaseValue = feesAndRounding + shares * priceOpen * if (type.isShort) -1 else 1
        cashEffect = realized - purchaseValue
        costBasis = collateral + purchaseValue
    }
//            cashEffect = subPositions.sumOf(LotPosition::cashEffect) // TODO this ignore fields of the parent that might be set differently, i.e. realizedClosed
//            costBasis = subPositions.sumOf(LotPosition::costBasis)

    override fun unrealized(prices: Map<String, Long>) =
        ((prices[instrumentName] ?: priceOpen) - priceOpen) * shares * if (type.isShort) -1 else 1

    override fun returns(prices: Map<String, Long>) = realized + unrealized(prices)
    override fun equity(prices: Map<String, Long>) =
        if (type == PositionType.CASH) cashEffect
        else (prices[instrumentName] ?: priceOpen) * shares * (if (type.isShort) -1 else 1)

    override fun returnsPercent(prices: Map<String, Long>) =
        if (type == PositionType.CASH || costBasis == 0L) 0f
        else (realizedOpen + unrealized(prices)).toFloat() / costBasis


}


class AggregatePosition (
    val realizedClosedExtra: Long,
    override val subPositions: List<Position>,
) : Position {
    /** Aggregator **/
    val samePosition =
        if (subPositions.isEmpty()) false
        else subPositions.all { it.instrumentName == subPositions[0].instrumentName }

    private fun<T> ifAllEqual(fn: Position.() -> T, default: T): T {
        return if (subPositions.isEmpty()) default
        else if (subPositions.all { it.fn() == subPositions[0].fn() }) subPositions[0].fn()
        else default
    }
    private fun sumSubPositions(
        prices: Map<String, Long>,
        fn: Position.(Map<String, Long>?) -> Long
    ): Long {
        return subPositions.sumOf { it.fn(prices) }
    }

//    /** Public functions **/
//    fun unrealized(prices: Map<String, Long> = emptyMap()) = sumSubPositions(prices) { unrealized(it) }
//    fun returns(prices: Map<String, Long> = emptyMap()) = sumSubPositions(prices) { returns(it) }
//    fun equity(prices: Map<String, Long> = emptyMap()) = sumSubPositions(prices) { equity(it) }
//    fun returnsPercent(prices: Map<String, Long>) = if (type == PositionType.CASH || costBasis == 0L) 0f else (realizedOpen + unrealized(prices)).toFloat() / costBasis

    /** Identifiers **/
    override val account = ifAllEqual(Position::account, "")
    override val ticker = ifAllEqual(Position::ticker, "")
    override val type = ifAllEqual(Position::type, PositionType.NONE)
    /** Per share **/
    override val shares = if (samePosition) subPositions.sumOf(Position::shares) else 0L
    override val priceOpen = if (samePosition) subPositions.sumOf { it.priceOpen * it.shares } / shares else 0L // TODO this rounds to the nearest .01 cents
    /** Basis **/
    override val cashEffect = subPositions.sumOf(Position::cashEffect) + realizedClosedExtra
    override val costBasis = subPositions.sumOf(Position::costBasis)
    override val taxBasis = subPositions.sumOf(Position::taxBasis)
    override val feesAndRounding = subPositions.sumOf(Position::feesAndRounding)
    /** Returns **/
    override val realizedOpen = subPositions.sumOf(Position::realizedOpen)
    override val realizedClosed = subPositions.sumOf(Position::realizedClosed) + realizedClosedExtra
    override val realized = realizedOpen + realizedClosed
    override fun unrealized(prices: Map<String, Long>) = subPositions.sumOf { it.unrealized(prices) }
    override fun returns(prices: Map<String, Long>) = subPositions.sumOf { it.returns(prices) }
    override fun equity(prices: Map<String, Long>) = subPositions.sumOf { it.equity(prices) }
    override fun returnsPercent(prices: Map<String, Long>) =
        if (type == PositionType.CASH || costBasis == 0L) 0f
        else (realizedOpen + unrealized(prices)).toFloat() / costBasis
    /** Options **/
    override val expiration = ifAllEqual(Position::expiration, 0)
    override val strike = ifAllEqual(Position::strike, 0)
    override val collateral
    override val instrumentName = ifAllEqual(Position::instrumentName, "")
}

// Allows for nested subPositions
fun List<LotPosition>.join(): LotPosition {
    if (size == 1) return first()
    return reduce { a, b -> a + b }.copy(subPositions = this)
}


operator fun plus(b: LotPosition): AggregatePosition {
    fun <T> ifEqual(default: T, field: LotPosition.() -> T) = if (this.field() == b.field()) this.field() else default
    return AggregatePosition(
        subPositions = subPositions.ifEmpty { listOf(this) } + b.subPositions.ifEmpty { listOf(b) }
    )
}