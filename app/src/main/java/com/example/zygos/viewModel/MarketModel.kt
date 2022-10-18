package com.example.zygos.viewModel

import android.util.Log
import androidx.compose.runtime.mutableStateMapOf
import com.example.zygos.data.toLongDollar
import com.example.zygos.network.TdApi
import com.example.zygos.network.TdOptionQuote
import com.example.zygos.network.TdQuote
import com.example.zygos.network.tdService

class MarketModel(private val parent: ZygosViewModel) {
    val stockQuotes = mutableMapOf<String, TdQuote>()
    val optionQuotes = mutableMapOf<String, TdOptionQuote>()

    val markPrices = mutableMapOf<String, Long>()
    val closePrices = mutableMapOf<String, Long>()
    val percentChanges = mutableMapOf<String, Float>()

    suspend fun updatePrices(stocks: MutableSet<String>, options: MutableSet<String>): Boolean {
        val key = parent.apiKeys[tdService.name]
        if (key.isNullOrBlank()) return false

        try {
            stocks.remove("CASH")
            val newQuotes = if (stocks.isNotEmpty()) TdApi.getQuote(key, stocks) else emptyMap()
            val newOptionQuotes = if (options.isNotEmpty()) TdApi.getOptionQuote(key, options) else emptyMap()

            stockQuotes.putAll(newQuotes)
            optionQuotes.putAll(newOptionQuotes)

            stockQuotes.mapValuesTo(markPrices) { it.value.mark.toLongDollar() }
            optionQuotes.mapValuesTo(markPrices) { it.value.mark.toLongDollar() }
            stockQuotes.mapValuesTo(closePrices) { it.value.closePrice.toLongDollar() }
            optionQuotes.mapValuesTo(closePrices) { it.value.closePrice.toLongDollar() }
            stockQuotes.mapValuesTo(percentChanges) { it.value.netPercentChangeInDouble / 100f }
            optionQuotes.mapValuesTo(percentChanges) { it.value.markPercentChangeInDouble / 100f }

            Log.d("Zygos/MarketModel/updatePrices", "$stockQuotes")
            Log.d("Zygos/MarketModel/updatePrices", "$optionQuotes")
            return true
        } catch (e: Exception) {
            Log.w("Zygos/MarketModel/updatePrices", "Failure: ${e.message}")
        }
        return false
    }
}