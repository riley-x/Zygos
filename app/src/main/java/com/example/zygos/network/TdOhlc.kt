package com.example.zygos.network


data class TdPriceHistory(
    val candles: List<TdOhlc>
)

data class TdOhlc (
    val open: Float,
    val high: Float,
    val low: Float,
    val close: Float,
    val volume: Long,
    val datetime: Long,
)