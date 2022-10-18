package com.example.zygos.network

import android.util.Log
import androidx.compose.runtime.Immutable
import com.example.zygos.data.getIntDate
import com.example.zygos.data.toLongDollar
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
    companion object Factory {
        operator fun invoke(rawQuote: AlphaVantageRawQuote): AlphaVantageQuote {
            return AlphaVantageQuote(
                symbol = rawQuote.symbol,
                open = (rawQuote.open.toFloat()).toLongDollar(),
                high = (rawQuote.high.toFloat()).toLongDollar(),
                low = rawQuote.low.toFloat().toLongDollar(),
                price = rawQuote.price.toFloat().toLongDollar(),
                volume = rawQuote.volume.toLong(),
                latestTradingDay = parseAlphaDate(rawQuote.latestTradingDay),
                previousClose = rawQuote.previousClose.toFloat().toLongDollar(),
                change = rawQuote.change.toFloat().toLongDollar(),
                changePercent = rawQuote.changePercent.dropLast(1).toFloat() / 100f, // Trailing '%' sign
            )
        }
    }
}

private fun parseAlphaDate(string: String): Int {
    val split = string.split("-")
    if (split.size != 3) throw RuntimeException("parseAlphaDate() couldn't parse: $string")

    val year = split[0].toInt()
    val month = split[1].toInt()
    val day = split[2].toInt()
    return getIntDate(year, month, day)
}