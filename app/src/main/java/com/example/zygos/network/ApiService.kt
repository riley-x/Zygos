package com.example.zygos.network

import com.example.zygos.viewModel.PREFERENCE_IEX_API_KEY_KEY

data class ApiService(
    val name: String,
    val preferenceKey: String,
    val url: String,
)

// TODO
val apiServices = listOf(
    ApiService(
        name = "IEX",
        preferenceKey = PREFERENCE_IEX_API_KEY_KEY,
        url = "",
    ),
    ApiService(
        name = "Alpha Vantage",
        preferenceKey = "",
        url = "",
    ),
    ApiService(
        name = "Polygon",
        preferenceKey = "",
        url = "",
    ),
)