package com.example.zygos.viewModel

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.example.zygos.data.LotPosition
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
    val lot: LotPosition,
    val unrealized: Float,
    val returns: Float,
    val returnsPercent: Float,
    val equity: Float,
    val subPositions: List<Position> = emptyList(),
) {
    val ticker = lot.ticker
    val account = lot.account

    companion object Factory {
        operator fun invoke(
            lot: LotPosition,
            subPositions: List<Position> = emptyList(),
            prices: Map<String, Long> = emptyMap()
        ): Position {
            return Position(
                lot = lot,
                unrealized = lot.unrealized(prices[lot.ticker]).toFloatDollar(),
                returns = lot.returns(prices[lot.ticker]).toFloatDollar(),
                returnsPercent = lot.returnsPercent(prices[lot.ticker]).toFloat(),
                equity = lot.equity(prices[lot.ticker]).toFloatDollar(),
                subPositions = subPositions,
            )
        }
    }
}

