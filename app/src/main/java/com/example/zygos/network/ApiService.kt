package com.example.zygos.network

import com.example.zygos.viewModel.PREFERENCE_ALPHA_VANTAGE_API_KEY_KEY
import com.example.zygos.viewModel.PREFERENCE_IEX_API_KEY_KEY
import com.example.zygos.viewModel.PREFERENCE_POLYGON_API_KEY_KEY

data class ApiService(
    val name: String,
    val preferenceKey: String,
    val url: String,
)

// TODO
val apiServices = listOf(
    tdService,
    ApiService(
        name = "IEX",
        preferenceKey = PREFERENCE_IEX_API_KEY_KEY,
        url = "",
    ),
    ApiService(
        name = "Alpha Vantage",
        preferenceKey = PREFERENCE_ALPHA_VANTAGE_API_KEY_KEY,
        url = "",
    ),
    ApiService(
        name = "Polygon",
        preferenceKey = PREFERENCE_POLYGON_API_KEY_KEY,
        url = "",
    ),
)