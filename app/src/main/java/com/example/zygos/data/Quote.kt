package com.example.zygos.data

import androidx.compose.ui.graphics.Color

data class Quote (
    val ticker: String,
    val color: Color,
    val price: Float,
    val change: Float,
    val percentChange: Float,
)
