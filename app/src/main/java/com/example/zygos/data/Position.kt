package com.example.zygos.data

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class Position(
    val ticker: String,
    val value: Float,
    val color: Color,
)