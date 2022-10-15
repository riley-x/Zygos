package com.example.zygos.network

import com.example.zygos.viewModel.PREFERENCE_TD_API_KEY_KEY
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
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

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(tdService.url)
    .build()

interface TdService {
    @GET("v1/marketdata/quotes") // this is the http endpoint!
    suspend fun getQuote(
        @Query("apikey") apiKey: String,
        @Query("symbol") symbols: String, // Comma separated
    ): Map<String, TdQuote>

    @GET("v1/marketdata/quotes") // this is the http endpoint!
    suspend fun getOptionQuote(
        @Query("apikey") apiKey: String,
        @Query("symbol") symbols: String, // Comma separated
    ): Map<String, TdOptionQuote>
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
}