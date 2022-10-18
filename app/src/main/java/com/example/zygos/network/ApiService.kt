package com.example.zygos.network

import com.example.zygos.viewModel.PREFERENCE_ALPHA_VANTAGE_API_KEY_KEY
import com.example.zygos.viewModel.PREFERENCE_IEX_API_KEY_KEY
import com.example.zygos.viewModel.PREFERENCE_POLYGON_API_KEY_KEY
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

internal val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()


data class ApiService(
    val name: String,
    val preferenceKey: String,
    val url: String,
)

// TODO
val apiServices = listOf(
    tdService,
    alphaVantageService,
//    ApiService(
//        name = "IEX",
//        preferenceKey = PREFERENCE_IEX_API_KEY_KEY,
//        url = "",
//    ),
//    ApiService(
//        name = "Polygon",
//        preferenceKey = PREFERENCE_POLYGON_API_KEY_KEY,
//        url = "",
//    ),
)