package com.example.zygos.ui.theme

import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.graphics.Color

val Purple200 = Color(0xFFBB86FC)
val Purple500 = Color(0xFF6200EE)
val Purple700 = Color(0xFF3700B3)
val Teal200 = Color(0xFF03DAC5)

val Green500 = Color(0xFF1EB969)
val DarkBlue900 = Color(0xFF26282F)

val CandleGreen = Color(0xFF2DBF78)
val CandleRed = Color(0xFFEF4747)

val defaultTickerColors = mapOf(
    "CASH" to Color(0xFF1EB969), // Green500
    "AMD" to Color(0xffed1c24),
    "MSFT" to Color(0xff00a1f1),
)

fun SnapshotStateMap<String, Color>.getOrRandom(ticker: String) =
    getOrPut(ticker) { Color((Math.random() * 16777215).toInt() or (0xFF shl 24)) }

