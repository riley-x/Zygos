package com.example.zygos.viewModel

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.example.zygos.data.LotPosition
import com.example.zygos.data.PositionType
import com.example.zygos.data.TickerPosition
import com.example.zygos.data.toFloatDollar

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
data class Position (
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
    val expiration: String = "",
    val strike: String = "",
    val collateral: Float = 0f,
    /** Price-dependent **/
    val unrealized: Float = 0f,
    val returnsOpen: Float = 0f,
    val returnsPercent: Float = 0f,
    val returnsTotal: Float = 0f,
    val equity: Float = 0f,
    /** Sub-positions **/
    val subPositions: List<Position> = emptyList(),
) {
    companion object Factory {
        operator fun invoke(
            lot: LotPosition,
            prices: Map<String, Long> = emptyMap()
        ): Position {
            val realizedOpen = lot.realizedOpen.toFloatDollar()
            val unrealized = lot.unrealized(prices[lot.ticker]).toFloatDollar()
            return Position(
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
                strike = lot.strike,
                collateral = lot.collateral.toFloatDollar(),
                /** Price-dependent **/
                unrealized = unrealized,
                returnsOpen = realizedOpen + unrealized,
                returnsPercent = lot.returnsPercent(prices[lot.ticker]).toFloat(),
                returnsTotal = lot.returns(prices[lot.ticker]).toFloatDollar(),
                equity = lot.equity(prices[lot.ticker]).toFloatDollar(),
            )
        }


        fun getLongPosition(
            tickerPosition: TickerPosition,
            prices: Map<String, Long> = emptyMap()
        ): Position {
            val subPositions = mutableListOf<Position>()
            if (tickerPosition.stock.shares > 0) subPositions.add(Position(tickerPosition.stock))
            tickerPosition.longOptions.forEach { subPositions.add(Position(it)) }
            tickerPosition.coveredCalls.forEach { subPositions.add(Position(it)) }

            val realizedOpen = tickerPosition.realizedOpenLong.toFloatDollar()
            val unrealized = tickerPosition.unrealized(prices, true).toFloatDollar()

            return Position(
                /** Identifiers **/
                account = tickerPosition.account,
                ticker = tickerPosition.ticker,
                /** Basis and returns **/
                costBasis = realizedOpen,
                realizedOpen = tickerPosition.realizedOpenLong.toFloatDollar(),
                realizedClosed = tickerPosition.realizedClosed.toFloatDollar(),
                /** Price-dependent **/
                unrealized = unrealized,
                returnsOpen = realizedOpen + unrealized,
                returnsPercent = tickerPosition.returnsPercent(prices, true).toFloat(),
                returnsTotal = tickerPosition.returns(prices, true).toFloatDollar(),
                equity = tickerPosition.equity(prices, true).toFloatDollar(),
                /** Sub-positions **/
                subPositions = subPositions,
            )
        }
    }
}

