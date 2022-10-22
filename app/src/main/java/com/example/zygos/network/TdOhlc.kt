package com.example.zygos.network

import androidx.compose.runtime.Immutable


data class TdPriceHistory(
    val candles: List<TdOhlc>
)

@Immutable
data class TdOhlc (
    val open: Float,
    val high: Float,
    val low: Float,
    val close: Float,
    val volume: Long,
    val datetime: Long,
)