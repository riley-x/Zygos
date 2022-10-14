package com.example.zygos.viewModel

import android.util.Log
import androidx.compose.runtime.mutableStateMapOf
import com.example.zygos.data.toLongDollar
import com.example.zygos.network.TdApi
import com.example.zygos.network.TdQuote
import com.example.zygos.network.tdService

class MarketModel(private val parent: ZygosViewModel) {
    val quotes = mutableStateMapOf<String, TdQuote>()

    suspend fun updatePrices(tickers: MutableSet<String>): Boolean {
        val key = parent.apiKeys[tdService.name]
        if (key.isNullOrBlank()) return false

        try {
            tickers.remove("CASH")
            val newQuotes = TdApi.getQuote(key, tickers)
            quotes.clear()
            quotes.putAll(newQuotes)
            Log.d("Zygos/MarketModel/updatePrices", "$quotes")
            return true
        } catch (e: Exception) {
            Log.w("Zygos/MarketModel/updatePrices", "Failure: ${e.message}")
        }
        return false
    }
}