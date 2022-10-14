package com.example.zygos.network

data class TdQuote(
    val symbol: String,
    val mark: Float, // this is market hours only
    val lastPrice: Float, // this and ohlc seem to update with after hours
    val openPrice: Float,
    val highPrice: Float,
    val lowPrice: Float,
    val closePrice: Float, // this seems to always be the previous day's close? Even at 12:47 am.
    val netPercentChangeInDouble: Float, // this seems to include after hours
    val netChange: Float, // this seems to include after hours
    val regularMarketPercentChangeInDouble: Float, // this doesn't
    val regularMarketNetChange: Float, // this doesn't
)