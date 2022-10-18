package com.example.zygos.network

import androidx.compose.runtime.Immutable
import com.squareup.moshi.Json


data class AlphaVantageGlobalQuote(
    @Json(name = "Global Quote") val rawQuote: AlphaVantageRawQuote
)

data class AlphaVantageRawQuote(
    @Json(name = "01. symbol") val symbol: String,
    @Json(name = "02. open") val open: String,
    @Json(name = "03. high") val high: String,
    @Json(name = "04. low") val low: String,
    @Json(name = "05. price") val price: String,
    @Json(name = "06. volume") val volume: String,
    @Json(name = "07. latest trading day") val latestTradingDay: String,
    @Json(name = "08. previous close") val previousClose: String,
    @Json(name = "09. change") val change: String,
    @Json(name = "10. change percent") val changePercent: String,
)

@Immutable
data class AlphaVantageQuote(
    val symbol: String,
    val open: Long,
    val high: Long,
    val low: Long,
    val price: Long,
    val volume: Long,
    val latestTradingDay: Int,
    val previousClose: Long,
    val change: Long,
    val changePercent: Float
) {

}