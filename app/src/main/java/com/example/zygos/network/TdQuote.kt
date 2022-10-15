package com.example.zygos.network

import androidx.compose.runtime.Immutable

@Immutable
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

@Immutable
data class TdOptionQuote(
    val symbol: String,
    val mark: Float,
    val openPrice: Float,
    val highPrice: Float,
    val lowPrice: Float,
    val closePrice: Float,
    val delta: Float,
    val gamma: Float,
    val theta: Float,
    val vega: Float,
    val rho: Float,
    val openInterest: Int,
    val volatility: Float,
    val moneyIntrinsicValue: Float, // don't use "timeValue" since that uses latest trade price
    val daysToExpiration: Int,
    val theoreticalOptionValue: Float,
    val underlyingPrice: Float,
    val markChangeInDouble: Float, // don't use "net" values since those use latest trade price, which don't reflect B/A
    val markPercentChangeInDouble: Float,
)