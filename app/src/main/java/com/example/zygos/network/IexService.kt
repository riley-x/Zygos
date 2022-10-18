package com.example.zygos.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

private const val BASE_URL =
    "https://cloud.iexapis.com"

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .build()

interface IexService {
    @GET("stable/stock/{ticker}/quote") // this is the http endpoint!
    suspend fun getQuote(@Path("ticker") ticker: String): IexQuote
}

object IexApi {
    val iexService : IexService by lazy {
        retrofit.create(IexService::class.java)
    }
}

//viewModelScope.launch {
//    try {
//        val listResult = IexAPi.iexService.getPhotos()
//    } catch (e: Exception) {
//        println("Failure: ${e.message}")
//    }
//}
