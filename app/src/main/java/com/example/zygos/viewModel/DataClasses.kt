package com.example.zygos.viewModel

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * This file contains data classes that are passed into composables
 */


@Immutable
data class TimeSeriesTick(
    val index: Int,
    val label: String,
)

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