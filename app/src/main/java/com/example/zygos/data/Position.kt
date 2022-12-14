package com.example.zygos.data

import android.util.Log
import androidx.compose.runtime.Immutable
import com.example.zygos.network.getTdOptionName
import kotlin.math.abs


enum class PositionType(val displayName: String, val isOption: Boolean = false, val isShort: Boolean = false, val isSpread: Boolean = false) {
    CASH("Cash"),
    STOCK("Stock"),
    CALL_LONG("Call", true),
    PUT_LONG("Put", true),
    CALL_DEBIT_SPREAD("CDS", true, false, true), // all spreads can include diagonals/calendars
    CALL_CREDIT_SPREAD("CCS", true, true, true),
    PUT_DEBIT_SPREAD("PDS", true, false, true),
    PUT_CREDIT_SPREAD("PCS", true, true, true),
    CASH_SECURED_PUT("Short Put", true, true),
    COVERED_CALL("Short Call", true, true),
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
 * @property instrumentName A name to lookup the current price with from a map of prices. This can
 * be set to some arbitrary display name for compound positions, use instead the individual sub-
 * positions.
 *
 *
 * @property shares is always positive
 *
 *
 * @property cashEffect is the change in the cash balance caused by this position. It is negative
 * for BTO. This probably isn't necessary anymore since the CASH lot in the database stores the sum
 * of cash effects from all transactions.
 * @property costBasis Used for % return calculation, the amount of cash needed to open the position.
 * For long positions this is essentially [shares] * [priceOpen], but can be different due to
 * rounding and fees. For short position this includes the cash collateral needed. Uniquely for
 * covered calls, which need 0 cash collateral, this will be abs([cashEffect]). The % return is
 * somewhat meaningless in this case, but there's nothing really better to use.
 * @property taxBasis Actual basis used for taxes. Usually the same but could be adjusted for wash
 * sales. TODO unimplemented right now.
 * @property feesAndRounding All other quantities are divisible by [shares]. This field keeps track
 * of all rounding errors and fees. Note the fees are not split by share amount if this position is
 * half-closed, and are kept until the entire position is fully closed. This is usually positive to
 * indicate the amount of fees charged.
 *
 *
 * @property realizedOpen Realized returns from open positions, i.e. dividends. Note STO proceeds are
 * not included here, see [cashEffect] instead. These returns are included in % return calculations.
 * @property realizedClosed Realized returns from closed positions. These returns are not included in
 * % return calculations.
 *
 *
 * @property collateral For cash-secured short positions, amount of cash collateral.
 *
 *
 * @property subPositions Constituent positions, if any. For example a stock position can consist of
 * many lot positions.
 */
@Immutable
interface Position {
    /** Identifiers **/
    val account: String
    val ticker: String
    val type: PositionType
    val date: Int
    val instrumentName: String
    /** Per share **/
    val shares: Long
    val priceOpen: Long
    /** Basis **/
    val cashEffect: Long
    val costBasis: Long
    val taxBasis: Long
    val feesAndRounding: Long
    /** Realized **/
    val realizedOpen: Long
    val realizedClosed: Long
    val realized: Long
    /** Options **/
    val expiration: Int
    val strike: Long
    val collateral: Long
    val priceUnderlyingOpen: Long
    /** Sub-positions **/
    val subPositions: List<Position>

    fun equity(prices: Map<String, Long>): Long
    fun unrealized(prices: Map<String, Long>): Long
    fun returns(prices: Map<String, Long>): Long // open only
    fun returnsPercent(prices: Map<String, Long>): Float
    fun returnsPeriod(pricesStart: Map<String, Long>, pricesEnd: Map<String, Long>): Long
//    fun returnsPeriodPercent(pricesStart: Map<String, Long>, pricesEnd: Map<String, Long>): Float

    operator fun plus(b: Position): AggregatePosition {
        return AggregatePosition(
            realizedClosedExtra = 0L,
            type = if (type == b.type) type else PositionType.NONE,
            subPositions = subPositions.ifEmpty { listOf(this) } + b.subPositions.ifEmpty { listOf(b) }
        )
    }
}


/**
 * This is the base position class, representing a single lot. [subPositions] must be empty.
 */
@Immutable
data class LotPosition(
    /** Identifiers **/
    override val account: String = "",
    override val ticker: String = "",
    override val type: PositionType = PositionType.NONE,
    override val date: Int = 0,
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
    override val priceUnderlyingOpen: Long = 0,
    override val instrumentName: String = if (type.isOption) getTdOptionName(ticker, type, expiration, strike) else ticker, // this is modified for spreads. always use subPositions for unrealized
) : Position {
    /** Derived values **/
    override val subPositions = emptyList<Position>()
    override val realized = realizedOpen + realizedClosed
    override val cashEffect: Long
    override val costBasis: Long
    init {
        val purchaseValue = feesAndRounding + shares * priceOpen * if (type.isShort) -1 else 1
        cashEffect = realized - purchaseValue
        costBasis = abs(collateral + purchaseValue) // for covered calls, the arg may be negative
    }

    override fun unrealized(prices: Map<String, Long>): Long {
        if (type == PositionType.CASH) return 0
        val price = prices[instrumentName]
        if (price == null) Log.w("Zygos/LotPosition", "Couldn't find price for $instrumentName")
        return ((price ?: priceOpen) - priceOpen) * shares * if (type.isShort) -1 else 1
    }
    override fun returns(prices: Map<String, Long>) = realizedOpen + unrealized(prices)
    override fun equity(prices: Map<String, Long>): Long {
        if (type == PositionType.CASH) return cashEffect
        val price = prices[instrumentName]
        if (price == null) Log.w("Zygos/LotPosition", "Couldn't find price for $instrumentName")
        return (price ?: priceOpen) * shares * (if (type.isShort) -1 else 1)
    }

    override fun returnsPercent(prices: Map<String, Long>) =
        if (type == PositionType.CASH || costBasis == 0L) 0f
        else (realizedOpen + unrealized(prices)).toFloat() / costBasis

    override fun returnsPeriod(pricesStart: Map<String, Long>, pricesEnd: Map<String, Long>) =
        unrealized(pricesEnd) - unrealized(pricesStart)

//    override fun returnsPeriodPercent(pricesStart: Map<String, Long>, pricesEnd: Map<String, Long>) =
//        if (type == PositionType.CASH || costBasis == 0L) 0f
//        else unrealized(pricesEnd).toFloat() / equity(pricesStart)
}


fun samePosition(subPositions: List<Position>) =
    if (subPositions.isEmpty()) true
    else subPositions.all { it.instrumentName == subPositions[0].instrumentName }

private fun<T> List<Position>.ifAllEqual(fn: Position.() -> T, default: T): T =
    if (isEmpty()) default
    else if (all { it.fn() == first().fn() }) first().fn()
    else default


/**
 * This class represents a combined position. For example, it could aggregate multiple stock lots,
 * or combine two option legs into a spread. Most of the fields are fixed based on the [subPositions],
 * and are evaluated below with [ifAllEqual] or [sumOf]. Spreads may edit some of the option
 * description fields.
 *
 * Note that for the priced functions like [unrealized], this class merely sums the values from
 * [subPositions]. So the [instrumentName] set here is never used for indexing the price maps.
 *
 * @param realizedClosedExtra Additional realized returns from closed lots, that is not included in
 * any of the [subPositions]
 */
@Immutable
data class AggregatePosition (
    val realizedClosedExtra: Long,
    override val subPositions: List<Position>,
    /** Default values for +. Spreads can alter these **/
    override val type: PositionType = subPositions.ifAllEqual(Position::type, PositionType.NONE),
    override val shares: Long = if (samePosition(subPositions)) subPositions.sumOf(Position::shares) else 0L,
    override val priceOpen: Long = if (samePosition(subPositions)) subPositions.sumOf { it.priceOpen * it.shares } / shares else 0L, // TODO this rounds to the nearest .01 cents
    override val instrumentName: String = subPositions.ifAllEqual(Position::instrumentName, ""),
) : Position {
    /** Identifiers **/
    override val account = subPositions.ifAllEqual(Position::account, "")
    override val ticker = subPositions.ifAllEqual(Position::ticker, "")
    override val date = subPositions.ifAllEqual(Position::date, 0)
    /** Basis **/
    override val cashEffect = subPositions.sumOf(Position::cashEffect) + realizedClosedExtra
    override val costBasis = subPositions.sumOf(Position::costBasis)
    override val taxBasis = subPositions.sumOf(Position::taxBasis)
    override val feesAndRounding = subPositions.sumOf(Position::feesAndRounding)
    /** Realized **/
    override val realizedOpen = subPositions.sumOf(Position::realizedOpen)
    override val realizedClosed = subPositions.sumOf(Position::realizedClosed) + realizedClosedExtra
    override val realized = realizedOpen + realizedClosed
    /** Options **/
    override val expiration = subPositions.ifAllEqual(Position::expiration, 0)
    override val strike = subPositions.ifAllEqual(Position::strike, 0)
    override val collateral = subPositions.sumOf(Position::collateral)
    override val priceUnderlyingOpen = subPositions.ifAllEqual(Position::priceUnderlyingOpen, 0)

    /** Functions **/
    override fun unrealized(prices: Map<String, Long>) = subPositions.sumOf { it.unrealized(prices) }
    override fun returns(prices: Map<String, Long>) = subPositions.sumOf { it.returns(prices) }
    override fun equity(prices: Map<String, Long>) = subPositions.sumOf { it.equity(prices) } + if (type == PositionType.CASH) realizedClosedExtra else 0
    override fun returnsPercent(prices: Map<String, Long>) =
        if (type == PositionType.CASH || costBasis == 0L) 0f
        else (realizedOpen + unrealized(prices)).toFloat() / costBasis

    override fun returnsPeriod(pricesStart: Map<String, Long>, pricesEnd: Map<String, Long>): Long =
        subPositions.sumOf { it.returnsPeriod(pricesStart, pricesEnd) }
//    override fun returnsPeriodPercent(pricesStart: Map<String, Long>, pricesEnd: Map<String, Long>) =
//        if (type == PositionType.CASH || costBasis == 0L) 0f
//        else returnsPeriod(pricesStart, pricesEnd).toFloat() / costBasis
}


fun MutableList<Position>.join(realizedClosedExtra: Long = 0): Position {
    if (isEmpty()) throw RuntimeException("MutableList<Position>::join passed an empty list")
    return if (size == 1) {
        if (first() is AggregatePosition) {
            val first = (first() as AggregatePosition)
            first.copy(realizedClosedExtra = first.realizedClosedExtra + realizedClosedExtra)
        } else if (first() is LotPosition) {
            val first = (first() as LotPosition)
            first.copy(realizedClosed = first.realizedClosed + realizedClosedExtra)
        } else throw RuntimeException("MutableList<Position>::join passed unknown type ${first()::class}")
    } else AggregatePosition(
        realizedClosedExtra = realizedClosedExtra,
        subPositions = this,
    )
}