package com.example.zygos.viewModel

import android.util.Log
import com.example.zygos.data.toLongDollar
import com.example.zygos.network.TdApi
import com.example.zygos.network.tdService

class MarketModel(private val parent: ZygosViewModel) {
    val latestPrices = mutableMapOf<String, Long>()

    suspend fun updatePrices(tickers: MutableSet<String>): Boolean {
        val key = parent.apiKeys[tdService.name]
        if (key.isNullOrBlank()) return false

        try {
            tickers.remove("CASH")
            val quotes = TdApi.getQuote(key, tickers)
            Log.d("Zygos/MarketModel/updatePrices", "$quotes")
            quotes.mapValuesTo(latestPrices) { it.value.mark.toLongDollar() }
            return true
        } catch (e: Exception) {
            Log.w("Zygos/MarketModel/updatePrices", "Failure: ${e.message}")
        }
        return false
    }
}