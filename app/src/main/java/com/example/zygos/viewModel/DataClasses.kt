package com.example.zygos.viewModel

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.example.zygos.data.*

/**
 * This file contains data classes that are passed into composables
 */

interface HasName {
    val name: String
}

interface HasValue {
    val value: Float
}

@Immutable
data class NamedValue(
    override val value: Float,
    override val name: String,
): HasName, HasValue

@Immutable
data class TimeSeries(
    override val value: Float,
    val date: Int,
    override val name: String,
): HasName, HasValue

@Immutable
data class OhlcNamed(
    val open: Float,
    val high: Float,
    val low: Float,
    val close: Float,
    override val name: String,
): HasName


@Immutable
data class Quote (
    val ticker: String,
    val color: Color,
    val price: Float,
    val change: Float,
    val percentChange: Float,
)

@Immutable
data class PricedPosition (
    /** Identifiers **/
    val account: String = "",
    val ticker: String = "",
    val type: PositionType = PositionType.NONE,
    /** Per share **/
    val shares: Long = 0,
    val priceOpen: Float = 0f,
    /** Basis and returns **/
    val costBasis: Float = 0f,
    val realizedOpen: Float = 0f,
    val realizedClosed: Float = 0f,
    /** Options **/
    val expiration: Int = 0,
    val strike: Float = 0f,
    val collateral: Float = 0f,
    /** Price-dependent **/
    val unrealized: Float = 0f,
    val returnsOpen: Float = 0f,
    val returnsPercent: Float = 0f,
    val returnsTotal: Float = 0f,
    val equity: Float = 0f,
    /** Sub-positions **/
    val subPositions: List<PricedPosition> = emptyList(),
    val isSameInstrument: Boolean = false, // if the subpositions are just different lots of the same instrument
) {
    companion object Factory {
        operator fun invoke(
            lot: Position,
            prices: Map<String, Long>,
        ): PricedPosition {
            val realizedOpen = lot.realizedOpen.toFloatDollar()
            val unrealized = lot.unrealized(prices).toFloatDollar()
            return PricedPosition(
                /** Identifiers **/
                account = lot.account,
                ticker = lot.ticker,
                type = lot.type,
                /** Per share **/
                shares = lot.shares,
                priceOpen = lot.priceOpen.toFloatDollar(),
                /** Basis and returns **/
                costBasis = lot.costBasis.toFloatDollar(),
                realizedOpen = realizedOpen,
                realizedClosed = lot.realizedClosed.toFloatDollar(),
                /** Options **/
                expiration = lot.expiration,
                strike = lot.strike.toFloatDollar(),
                collateral = lot.collateral.toFloatDollar(),
                /** Price-dependent **/
                unrealized = unrealized,
                returnsOpen = realizedOpen + unrealized,
                returnsPercent = lot.returnsPercent(prices),
                returnsTotal = lot.returns(prices).toFloatDollar(),
                equity = lot.equity(prices).toFloatDollar(),
                /** Sub-positions **/
                subPositions = lot.subPositions.map { PricedPosition(it, prices) },
                isSameInstrument = samePosition(lot.subPositions)
            )
        }
    }
}

