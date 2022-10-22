package com.example.zygos.network

import android.icu.text.NumberFormat
import android.icu.util.Calendar
import android.icu.util.TimeZone
import android.util.Log
import com.example.zygos.data.*
import com.example.zygos.data.database.Ohlc
import com.example.zygos.viewModel.PREFERENCE_TD_API_KEY_KEY
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

val tdService = ApiService(
    name = "TD Ameritrade",
    preferenceKey = PREFERENCE_TD_API_KEY_KEY,
    url = "https://api.tdameritrade.com",
)


private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(tdService.url)
    .build()


interface TdService {
    @GET("v1/marketdata/quotes")
    suspend fun getQuote(
        @Query("apikey") apiKey: String,
        @Query("symbol") symbols: String, // Comma separated
    ): Map<String, TdQuote>

    @GET("v1/marketdata/quotes")
    suspend fun getOptionQuote(
        @Query("apikey") apiKey: String,
        @Query("symbol") symbols: String, // Comma separated
    ): Map<String, TdOptionQuote>

    /** For the below, must specify startDate and endDate instead of period to get current-day data **/

    @GET("v1/marketdata/{symbol}/pricehistory?periodType=year&frequencyType=daily")
    suspend fun getOhlc(
        @Path("symbol") symbol: String,
        @Query("apikey") apiKey: String,
        @Query("startDate") startDate: Long, // in millis
        @Query("endDate") endDate: Long, // in millis
    ): TdPriceHistory

    @GET("v1/marketdata/{symbol}/pricehistory?periodType=day&frequencyType=minute&frequency=30")
    suspend fun getOhlc30Min(
        @Path("symbol") symbol: String,
        @Query("apikey") apiKey: String,
        @Query("startDate") startDate: Long, // in millis
        @Query("endDate") endDate: Long, // in millis
    ): TdPriceHistory


    @GET("v1/marketdata/{symbol}/pricehistory?periodType=year&frequencyType=monthly")
    suspend fun getOhlcMonthly(
        @Path("symbol") symbol: String,
        @Query("apikey") apiKey: String,
        @Query("startDate") startDate: Long, // in millis
        @Query("endDate") endDate: Long, // in millis
    ): TdPriceHistory
}

object TdApi {
    val tdService : TdService by lazy {
        retrofit.create(TdService::class.java)
    }

    suspend fun getQuote(
        apiKey: String,
        symbols: Collection<String>,
    ): Map<String, TdQuote> {
        return tdService.getQuote(apiKey, symbols.joinToString(","))
    }

    suspend fun getOptionQuote(
        apiKey: String,
        symbols: Collection<String>,
    ): Map<String, TdOptionQuote> {
        return tdService.getOptionQuote(apiKey, symbols.joinToString(","))
    }

    suspend fun getOhlc(
        apiKey: String,
        symbol: String,
        startDate: Long,
        endDate: Long,
    ): List<Ohlc> {
        return tdService.getOhlc(
            symbol = symbol,
            apiKey = apiKey,
            startDate = startDate,
            endDate = endDate,
        ).candles.map {
            Ohlc(
                ticker = symbol,
                date = it.datetime.toIntDate(),
                open = it.open.toLongDollar(),
                high = it.high.toLongDollar(),
                low = it.low.toLongDollar(),
                close = it.close.toLongDollar(),
                volume = it.volume,
            )
        }
    }

    suspend fun getOhlc5Day(
        apiKey: String,
        symbol: String,
    ): List<TdOhlc> {
        val cal = Calendar.getInstance().toNewYork()
        val endDate = cal.timeInMillis
        cal.toMidnight().add(Calendar.DATE, -7) // well it's really one week not 5 days
        val startDate = cal.timeInMillis

        return tdService.getOhlc30Min(
            symbol = symbol,
            apiKey = apiKey,
            startDate = startDate,
            endDate = endDate,
        ).candles
    }

    suspend fun getOhlc1Year(
        apiKey: String,
        symbol: String,
    ): List<TdOhlc> {
        val cal = Calendar.getInstance().toNewYork()
        val endDate = cal.timeInMillis
        cal.toMidnight().add(Calendar.YEAR, -1)
        val startDate = cal.timeInMillis

        return tdService.getOhlc(
            symbol = symbol,
            apiKey = apiKey,
            startDate = startDate,
            endDate = endDate,
        ).candles
    }

    suspend fun getOhlc20Year(
        apiKey: String,
        symbol: String,
    ): List<TdOhlc> {
        val cal = Calendar.getInstance().toNewYork()
        val endDate = cal.timeInMillis
        cal.toMidnight().add(Calendar.YEAR, -20)
        val startDate = cal.timeInMillis

        return tdService.getOhlcMonthly(
            symbol = symbol,
            apiKey = apiKey,
            startDate = startDate,
            endDate = endDate,
        ).candles
    }
}


fun getTdOptionName(ticker: String, type: PositionType, expiration: Int, strike: Long): String {
    val format = NumberFormat.getNumberInstance()
    format.isGroupingUsed = false
    format.minimumFractionDigits = 0
    format.minimumIntegerDigits = 2

    val day = format.format(getDay(expiration))
    val month = format.format(getMonth(expiration))
    val year = format.format(getYearShort(expiration) % 100)
    val formattedStrike = format.format(strike.toFloatDollar())

    val typeLetter = when (type) {
        PositionType.PUT_LONG -> "P"
        PositionType.CASH_SECURED_PUT -> "P"
        PositionType.CALL_LONG -> "C"
        PositionType.COVERED_CALL -> "C"
        else -> return ""
    }

    return "${ticker}_$month$day$year$typeLetter$formattedStrike"
}

