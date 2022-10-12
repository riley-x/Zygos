package com.example.zygos.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

private const val BASE_URL =
    "https://api.tdameritrade.com"

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .build()

interface TdService {
    @GET("v1/marketdata/quotes") // this is the http endpoint!
    suspend fun getQuote(
        @Query("apikey") apiKey: String,
        @Query("symbol") symbols: String, // Comma separated
    ): Map<String, TdQuote>

    suspend fun getQuote(
        apiKey: String,
        symbols: List<String>,
    ): Map<String, TdQuote> {
        return getQuote(apiKey, symbols.joinToString(","))
    }
}

object TdApi {
    val tdService : TdService by lazy {
        retrofit.create(TdService::class.java)
    }
}

//viewModelScope.launch {
//    try {
//        val listResult = IexAPi.iexService.getPhotos()
//    } catch (e: Exception) {
//        println("Failure: ${e.message}")
//    }
//}