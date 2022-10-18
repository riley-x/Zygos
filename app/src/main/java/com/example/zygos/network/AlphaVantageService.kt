package com.example.zygos.network

import android.icu.text.NumberFormat
import com.example.zygos.data.*
import com.example.zygos.viewModel.PREFERENCE_ALPHA_VANTAGE_API_KEY_KEY
import com.example.zygos.viewModel.PREFERENCE_TD_API_KEY_KEY
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

val alphaVantageService = ApiService(
    name = "Alpha Vantage",
    preferenceKey = PREFERENCE_ALPHA_VANTAGE_API_KEY_KEY,
    url = "https://www.alphavantage.co",
)


private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(alphaVantageService.url)
    .build()


interface AlphaVantageService {
    @GET("query?function=GLOBAL_QUOTE")
    suspend fun getQuote(
        @Query("apikey") apiKey: String,
        @Query("symbol") symbols: String,
    ): AlphaVantageGlobalQuote
}

object AlphaVantageApi {
    val alphaVantageService : AlphaVantageService by lazy {
        retrofit.create(AlphaVantageService::class.java)
    }
}

