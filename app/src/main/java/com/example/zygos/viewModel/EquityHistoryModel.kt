package com.example.zygos.viewModel

import android.icu.util.Calendar
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.example.zygos.data.*
import com.example.zygos.data.database.EquityHistory
import com.example.zygos.data.database.Ohlc
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


    internal suspend fun updateEquityHistory(
        account: String,
        tickers: Set<String>,
        positions: List<Position>,
        stockQuotes: Map<String, TdQuote>,
        optionQuotes: Map<String, TdOptionQuote>
    ) {
        if (account == allAccounts || account == noAccountMessage || account.isBlank()) return

        val currentHistory = history.toList()

        /** First update history from ohlc. This checks the ohlc endpoint at least once.
         * At this point the equity history will be up-to-date with everything except the current
         * day (TD ohlc endpoint seems to update at midnight) **/
        val (lastOhlcDate, newEquityHistory) = withContext(Dispatchers.Default) {
            updateEquityFromOhlc(account, tickers.toMutableSet(), positions, currentHistory)
        } ?: return
        history.addAll(newEquityHistory)

        /** Now check current market data from quotes and update both history and ohlc if market has
         * closed **/
        if (stockQuotes.isEmpty()) return
        val (marketClosedToday, lastMarketDate) = isMarketClosed(stockQuotes.values.first())
        if (marketClosedToday && lastMarketDate > lastOhlcDate) {
            updateEquityWithQuotes(account, positions, stockQuotes, optionQuotes, lastMarketDate)
            saveOhlc(stockQuotes, optionQuotes, lastMarketDate)
        }
    }


    /** Replaced with isMarketClosed() below **/
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

    private fun isMarketClosed(quote: TdQuote): Pair<Boolean, Int> {
        val regularTime = Calendar.getInstance()
        regularTime.timeInMillis = quote.regularMarketTradeTimeInLong
        val tradeTime = Calendar.getInstance()
        tradeTime.timeInMillis = quote.tradeTimeInLong
        return Pair(regularTime < tradeTime && regularTime.toIntDate() == tradeTime.toIntDate(), regularTime.toIntDate())
    }


    private fun updateEquityWithQuotes(
        account: String,
        positions: List<Position>,
        stockQuotes: Map<String, TdQuote>,
        optionQuotes: Map<String, TdOptionQuote>,
        lastMarketDate: Int,
    ) {
        val prices = mutableMapOf<String, Long>()
        stockQuotes.mapValuesTo(prices) { it.value.regularMarketLastPrice.toLongDollar() }
        optionQuotes.mapValuesTo(prices) { it.value.mark.toLongDollar() }

        val closeReturns = positions.sumOf { it.equity(prices) } - initialContributions
        val equityHistory = EquityHistory(account = account, date = lastMarketDate, returns = closeReturns)
        history.add(equityHistory)

        parent.viewModelScope.launch(Dispatchers.IO) {
            parent.equityHistoryDao.add(equityHistory)
        }
    }

    private fun saveOhlc(
        stockQuotes: Map<String, TdQuote>,
        optionQuotes: Map<String, TdOptionQuote>,
        lastMarketDate: Int,
    ) {
        stockQuotes.forEach { (ticker, quote) ->
            parent.viewModelScope.launch(Dispatchers.IO) {
                parent.ohlcDao.add(Ohlc(
                    ticker = ticker,
                    date = lastMarketDate,
                    open = quote.openPrice.toLongDollar(),
                    high = quote.highPrice.toLongDollar(),
                    low = quote.lowPrice.toLongDollar(),
                    close = quote.closePrice.toLongDollar(),
                    volume = quote.totalVolume,
                ))
            }
        }
        optionQuotes.forEach { (ticker, quote) ->
            parent.viewModelScope.launch(Dispatchers.IO) {
                parent.ohlcDao.add(Ohlc(
                    ticker = ticker,
                    date = lastMarketDate,
                    open = quote.openPrice.toLongDollar(),
                    high = quote.highPrice.toLongDollar(),
                    low = quote.lowPrice.toLongDollar(),
                    close = quote.closePrice.toLongDollar(),
                    volume = quote.totalVolume,
                ))
            }
        }
    }



    /** This checks and updates the equity history from the ohlc endpoint on TD. It assumes that
     * the positions do not change during this time (TODO).
     */
    private suspend fun updateEquityFromOhlc(
        account: String,
        tickers: MutableSet<String>,
        positions: List<Position>,
        currentHistory: List<EquityHistory>)
    : Pair<Int, List<EquityHistory>>? {

        if (positions.isEmpty()) return null
        val cashPosition = positions.find { it.type == PositionType.CASH } ?: return null

        val tdKey = parent.apiKeys[tdService.name]
        if (tdKey.isNullOrBlank()) return null

        /** Get timestamps and dates **/
        val cal = Calendar.getInstance()
        val currentTime = cal.timeInMillis

        if (currentHistory.isEmpty()) {
            cal.setIntDate(cashPosition.date)
            cal.add(Calendar.DATE, -1)
        }
        else {
            cal.setIntDate(currentHistory.last().date)
            cal.add(Calendar.DATE, 1)
        }
        val oldLastTime = cal.timeInMillis
        val oldLastDate = cal.toIntDate()

        /** Use the OHLC from one random ticker to check when market was open **/
        val keyTicker = if (tickers.isEmpty()) "MSFT" else tickers.first()
        val ohlc = updateOhlc(
            ticker = keyTicker,
            apiKey = tdKey,
            startTime = oldLastTime,
            endTime = currentTime,
        )
        Log.d("Zygos/EquityHistoryModel", "ohlc: $ohlc")

        /** Check if there's any new data from last update **/
        val lastOhlcDate = ohlc.last().date
        if (ohlc.isEmpty() || lastOhlcDate <= oldLastDate) return Pair(oldLastDate, emptyList())

        /** Get ohlc for every ticker, transpose to time series **/
        val prices = mutableMapOf<Int, MutableMap<String, Long>>() // The returned map preserves the entry iteration order. TODO option prices
        tickers.add(keyTicker)
        tickers.forEach {
            val tickerOhlc = if (it == keyTicker) ohlc else getOrUpdateOhlc(
                ticker = it,
                apiKey = tdKey,
                startTime = oldLastTime,
                endDate = lastOhlcDate,
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
        Log.d("Zygos/EquityHistoryModel", "newHistory: $newHistory")
        parent.viewModelScope.launch(Dispatchers.IO) {
            parent.equityHistoryDao.add(newHistory)
        }

        return Pair(ohlc.last().date, newHistory)
    }


    private suspend fun updateOhlc(ticker: String, apiKey: String, startTime: Long, endTime: Long): List<Ohlc> {

        val ohlc = withContext(Dispatchers.IO) {
            TdApi.getOhlc(
                symbol = ticker,
                apiKey = apiKey,
                startDate = startTime,
                endDate = endTime,
            )
        }

        parent.viewModelScope.launch(Dispatchers.IO) {
            parent.ohlcDao.add(ohlc)
        }

        return ohlc
    }


    private suspend fun getOrUpdateOhlc(ticker: String, apiKey: String, startTime: Long, endDate: Int): List<Ohlc> {
        return withContext(Dispatchers.IO) {
            val savedOhlc = parent.ohlcDao.getTicker(ticker)
            if (savedOhlc.last().date >= endDate) savedOhlc
            else updateOhlc(
                ticker = ticker,
                apiKey = apiKey,
                startTime = startTime,
                endTime = getTimestamp(endDate),
            )
        }
    }



}