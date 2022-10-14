package com.example.zygos.viewModel

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.example.zygos.data.*
import com.example.zygos.network.TdQuote
import com.example.zygos.ui.components.formatDateInt

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
): HasName, HasValue {
    override val name = formatDateInt(date)
}

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
    val date: Int = 0,
    val instrumentName: String = "",
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
    val returnsToday: Float = 0f,
    val returnsTodayPercent: Float = 0f,
    val equity: Float = 0f,
    /** Sub-positions **/
    val subPositions: List<PricedPosition> = emptyList(),
) {
    companion object Factory {
        operator fun invoke(
            lot: Position,
            quotes: Map<String, TdQuote>,
        ): PricedPosition {
            val markPrices = quotes.mapValues { it.value.mark.toLongDollar() }
            val closePrices = quotes.mapValues { it.value.closePrice.toLongDollar() }
            val realizedOpen = lot.realizedOpen.toFloatDollar()
            val unrealized = lot.unrealized(markPrices).toFloatDollar()
            return PricedPosition(
                /** Identifiers **/
                account = lot.account,
                ticker = lot.ticker,
                type = lot.type,
                date = lot.date,
                instrumentName = lot.instrumentName,
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
                returnsPercent = lot.returnsPercent(markPrices),
                returnsTotal = lot.returns(markPrices).toFloatDollar(),
                returnsToday = lot.returnsPeriod(closePrices, markPrices).toFloatDollar(),
                returnsTodayPercent = quotes[lot.instrumentName]?.netPercentChangeInDouble ?: 0f,
                equity = lot.equity(markPrices).toFloatDollar(),
                /** Sub-positions **/
                subPositions = lot.subPositions.map { PricedPosition(it, quotes) },
            )
        }
    }
}

