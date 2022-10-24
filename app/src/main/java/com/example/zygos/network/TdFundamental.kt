package com.example.zygos.network

import androidx.compose.runtime.Immutable
import com.squareup.moshi.Json


@Immutable
data class TdInstrument (
//    val cusip: String,
//    val symbol: String,
    val description: String,
    val fundamental: TdFundamental
)

@Immutable
data class TdFundamental (
    val symbol: String = "",
    val high52: Float = 0f,
    val low52: Float = 0f,
    val dividendAmount: Float = 0f,
    val dividendYield: Float = 0f,
    val dividendDate: String = "",
    val peRatio: Float = 0f,
//    val pegRatio: Float = 0f,
    val pcfRatio: Float = 0f,
//    val grossMarginTTM: Float = 0f,
//    val grossMaginMRQ: Float = 0f,
    val netProfitMarginTTM: Float = 0f,
    val operatingMarginTTM: Float = 0f,
    val quickRatio: Float = 0f,
    val currentRatio: Float = 0f,
//    @Transient @Json(ignore = true) val description: String? = "",
// For some reason the above doesn't ignore the field at all :(
// Actually this probably works fine, bug was in the Instrument
)