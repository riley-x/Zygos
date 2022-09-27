package com.example.zygos.viewModel

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * This file contains data classes that are passed into composables
 */

interface Named {
    val name: String
}

@Immutable
data class NamedValue(
    val value: Float,
    override val name: String,
): Named

@Immutable
data class Ohlc(
    val open: Float,
    val high: Float,
    val low: Float,
    val close: Float,
    override val name: String,
): Named


@Immutable
data class Position(
    val ticker: String,
    val value: Float,
    val color: Color,
)

@Immutable
data class Quote (
    val ticker: String,
    val color: Color,
    val price: Float,
    val change: Float,
    val percentChange: Float,
)