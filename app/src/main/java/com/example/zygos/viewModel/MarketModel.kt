package com.example.zygos.viewModel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.viewModelScope
import com.example.zygos.data.database.ColorSettings
import com.example.zygos.data.toLongDollar
import com.example.zygos.network.TdApi
import com.example.zygos.network.tdService
import com.example.zygos.ui.theme.defaultTickerColors
import com.example.zygos.ui.theme.randomColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MarketModel(private val parent: ZygosViewModel) {
    val latestPrices = mutableMapOf<String, Long>()

    suspend fun updatePrices(tickers: MutableSet<String>): Boolean {
        val key = parent.apiKeys[tdService.name]
        if (key.isNullOrBlank()) return false

        try {
            tickers.remove("CASH")
            val quotes = TdApi.getQuote(key, tickers)
            quotes.mapValuesTo(latestPrices) { it.value.lastPrice.toLongDollar() }
            return true
        } catch (e: Exception) {
            Log.w("Zygos/ZygosViewModel", "Failure: ${e.message}")
        }
        return false
    }
}