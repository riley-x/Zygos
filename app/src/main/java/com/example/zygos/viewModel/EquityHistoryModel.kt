package com.example.zygos.viewModel

import android.icu.util.Calendar
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.example.zygos.data.*
import com.example.zygos.data.database.EquityHistory
import com.example.zygos.network.*
import com.example.zygos.ui.components.allAccounts
import com.example.zygos.ui.components.noAccountMessage
import com.example.zygos.ui.graphing.TimeSeriesGraphState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

class EquityHistoryModel(private val parent: ZygosViewModel) {

    var initialContributions = 0L

    /** These are used by the performance screen **/
    var current by mutableStateOf(0L)
    var changeToday by mutableStateOf(0L)
    var changePercent by mutableStateOf(0f)


    fun setCurrent(positions: List<Position>, market: MarketModel) {
        current = positions.sumOf { it.equity(market.markPrices) }
        changeToday = positions.sumOf { it.returnsPeriod(market.closePrices, market.markPrices) }
        changePercent = changeToday.toFloat() / (current - changeToday)
    }


    val timeRange = mutableStateOf(accountPerformanceRangeOptions.items.last()) // must be state to pass down to button group derivedStateOf
    val graphState = mutableStateOf(TimeSeriesGraphState<TimeSeries>())

    private var history = mutableListOf<EquityHistory>() // Full history, graph.values are sliced from this

    fun updateTimeRange(range: String) {
        if (timeRange.value != range)
            setTimeRange(range)
    }

    private fun setTimeRange(range: String) {
        // don't check if time range is the same, since this is called in startup too. Use update instead
        timeRange.value = range
        if (history.isEmpty()) {
            graphState.value = TimeSeriesGraphState()
            return
        }

        parent.viewModelScope.launch {
            graphState.value = withContext(Dispatchers.Default) {
                getGraphState(range)
            }
        }
    }

    /** Make sure initialContributions is set already **/
    internal fun loadLaunched(account: String) {
        parent.viewModelScope.launch {
            /** Get series from database **/
            val loadedHistory = withContext(Dispatchers.IO) {
                if (account == allAccounts) parent.equityHistoryDao.getAllAccounts()
                else parent.equityHistoryDao.getAccount(account)
            }
            /** Upload to in-memory list. This should happen in the main thread since setTimeRange
             * might be happening in parallel **/
            history.clear()
            history.addAll(loadedHistory)

            /** Update graph state **/
            if (history.isNotEmpty()) {
                graphState.value = withContext(Dispatchers.Default) {
                    getGraphState(timeRange.value)
                }
            }
        }
    }


    private fun getGraphState(range: String): TimeSeriesGraphState<TimeSeries> {
        /** Get the y min/max and xrange of the performance plot **/
        var minReturns = history.last().returns
        var maxReturns = history.last().returns
        var startIndex = 0
        val startDate = if (range == "All") 0 else {
            val cal = Calendar.getInstance()
            when (range) {
                "1m" -> cal.add(Calendar.MONTH, -1)
                "3m" -> cal.add(Calendar.MONTH, -3)
                "1y" -> cal.add(Calendar.YEAR, -1)
                "5y" -> cal.add(Calendar.YEAR, -5)
            }
            cal.toIntDate()
        }
        for (i in history.lastIndex downTo 0) {
            val x = history[i]
            if (x.date < startDate) {
                startIndex = i + 1
                break
            }
            if (x.returns < minReturns) minReturns = x.returns
            if (x.returns > maxReturns) maxReturns = x.returns
        }
        var minY = (minReturns + initialContributions).toFloatDollar()
        var maxY = (maxReturns + initialContributions).toFloatDollar()
        val pad = (maxY - minY) * performanceGraphYPad
        maxY += pad
        minY -= pad

        /** Get the axis positions **/
        val stepX = (history.lastIndex - startIndex).toFloat() / performanceGraphTickDivisionsX
        val ticksX =
            IntRange(1, performanceGraphTickDivisionsX - 1).map { (stepX * it).roundToInt() }
        val ticksY = autoYTicks(
            minY,
            maxY,
            performanceGraphTickDivisionsY,
            performanceGraphTickDivisionsY - 1
        )

        return TimeSeriesGraphState(
            values = history.slice(startIndex..history.lastIndex).map { TimeSeries((it.returns + initialContributions).toFloatDollar(), it.date) },
            minY = minY,
            maxY = maxY,
            ticksX = ticksX,
            ticksY = ticksY,
            xAxisLoc = initialContributions.toFloatDollar()
        )
    }


    private suspend fun getLastCloseDate(): Int? {
        val alphaKey = parent.apiKeys[alphaVantageService.name]
        if (alphaKey.isNullOrBlank()) return null
        return try {
            AlphaVantageApi.getQuote(alphaKey, "MSFT").lastCloseDate
        } catch (e: Exception) {
            Log.w("Zygos/EquityHistoryModel/updateEquityHistory", "Failure: ${e::class} ${e.message}")
            null
        }
    }

    internal suspend fun updateEquityHistory(account: String, tickers: Set<String>, positions: List<Position>, stockQuotes: Map<String, TdQuote>, optionQuotes: Map<String, TdOptionQuote>) {

        if (account == allAccounts || account == noAccountMessage || account.isBlank()) return

//        if (stockQuotes.isEmpty()) return // TODO not right, what if cash?


        val lastDate = updateEquityFromOhlc(account, tickers.toMutableSet(), positions)

//        val regularTime = Calendar.getInstance()
//        regularTime.timeInMillis = quote.regularMarketTradeTimeInLong
//        val tradeTime = Calendar.getInstance()
//        tradeTime.timeInMillis = quote.tradeTimeInLong
//
//        val marketClosedToday = (regularTime < tradeTime && regularTime.toIntDate() == tradeTime.toIntDate())


//        val lastCloseDate = getLastCloseDate() ?: return



        /** Update stored ohlc history using current day quote **/
//        if (lastCloseDate == lastDate) {
//            // TODO
//        }
//
//        if (lastCloseDate <= history.last().date) return
//        if (stockQuotes.isEmpty()) return
    }

    private suspend fun updateEquityFromOhlc(account: String, tickers: MutableSet<String>, positions: List<Position>): Int? {

        if (positions.isEmpty()) return null
        val cashPosition = positions.find { it.type == PositionType.CASH } ?: return null

        val tdKey = parent.apiKeys[tdService.name]
        if (tdKey.isNullOrBlank()) return null

        val oldLastDate = if (history.isEmpty()) cashPosition.date - 1 else history.last().date
        val currentDate = Calendar.getInstance().toIntDate()

        /** Use the OHLC from one random ticker to check when market was open **/
        val keyTicker = if (tickers.isEmpty()) "MSFT" else tickers.first()
        val ohlc = TdApi.getOhlc(
            symbol = keyTicker,
            apiKey = tdKey,
            startDate = oldLastDate,
            endDate = currentDate,
        )
        Log.w("Zygos/EquityHistoryModel", "$ohlc")
        if (ohlc.isEmpty() || ohlc.last().date <= oldLastDate) return oldLastDate // no new info

        /** Get ohlc for every ticker, transpose to time series **/
        val prices = mutableMapOf<Int, MutableMap<String, Long>>() // The returned map preserves the entry iteration order.
        tickers.add(keyTicker)
        tickers.forEach {
            val tickerOhlc = if (it == keyTicker) ohlc else TdApi.getOhlc(
                symbol = it,
                apiKey = tdKey,
                startDate = oldLastDate,
                endDate = currentDate,
            )
            for (candle in tickerOhlc) {
                if (candle.date <= oldLastDate) continue
                prices.getOrPut(candle.date) { mutableMapOf() }[it] = candle.close
            }
        }

        /** Get new history and update **/
        val newHistory = mutableListOf<EquityHistory>()
        for (datePrices in prices) {
            newHistory.add(
                EquityHistory(
                    account = account,
                    date = datePrices.key,
                    returns = positions.sumOf { it.equity(datePrices.value) } - initialContributions,
                )
            )
        }
        Log.w("Zygos/EquityHistoryModel", "$newHistory")
        // TODO update here

        return ohlc.last().date
    }


}