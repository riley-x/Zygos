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
data class OhlcNamed (
    val open: Float,
    val high: Float,
    val low: Float,
    val close: Float,
    override val name: String,
) : HasName


@Immutable
data class Quote (
    /** This is required since the ticker rows in the watchlist remember the swipeable state. If you
     * use the ticker as the key, and delete a row then add it back, it'll remember the swipe state
     * to be deleted still **/
    val lazyKey: Int,
    val ticker: String,
    val color: Color,
    val price: Float,
    val change: Float,
    val percentChange: Float,
)


