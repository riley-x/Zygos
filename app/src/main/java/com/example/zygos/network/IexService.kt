package com.example.zygos.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET

private const val BASE_URL =
    "https://android-kotlin-fun-mars-server.appspot.com"

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .build()

interface IexService {
    @GET("endpoint") // this is the http endpoint!
    suspend fun getPhotos(): List<IexQuote>
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
