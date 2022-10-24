package com.example.zygos.network

import androidx.compose.runtime.Immutable


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
    val description: String = "",
)