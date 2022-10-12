package com.example.zygos.network

import com.squareup.moshi.Json

// The names of these variables must match the JSON keys, or else use @Json
data class IexQuote (
    val symbol: String,
    val open: Float,
    val close: Float,
    @Json(name = "json_key") val customName: String
)