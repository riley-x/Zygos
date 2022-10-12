package com.example.zygos.network

data class TdQuote(
    val symbol: String,
    val lastPrice: Float,
    val openPrice: Float,
    val highPrice: Float,
    val lowPrice: Float,
)