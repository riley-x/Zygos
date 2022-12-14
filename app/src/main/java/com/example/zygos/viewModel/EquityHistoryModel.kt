package com.example.zygos.viewModel

import android.icu.util.Calendar
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.core.math.MathUtils.clamp
import androidx.lifecycle.viewModelScope
import com.example.zygos.data.*
import com.example.zygos.data.database.EquityHistory
import com.example.zygos.data.database.Ohlc
import com.example.zygos.network.*
import com.example.zygos.ui.components.allAccounts
import com.example.zygos.ui.components.formatDateInt
import com.example.zygos.ui.graphing.TimeSeriesGraphState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

class EquityHistoryModel(private val parent: ZygosViewModel) {

    var lastUpdateAttemptDate = 0

    var initialContributions = 0L

    /** These are used by the performance screen **/
    var current = mutableStateOf(0L)
    var changeToday = mutableStateOf(0L)
    var changePercent = mutableStateOf(0f)


    fun setCurrent(positions: List<Position>, market: MarketModel) {
        current.value = positions.sumOf { it.equity(market.markPrices) }
        changeToday.value = positions.sumOf { it.returnsPeriod(market.closePrices, market.markPrices) }
        val yesterday = current.value - changeToday.value
        changePercent.value = if (yesterday == 0L) 0f else changeToday.value.toFloat() / yesterday
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
        val pad = (if (maxY == minY) maxY else (maxY - minY)) * performanceGraphYPad
        maxY += pad
        minY -= pad

        /** Get the axis positions **/
        val stepX = ((history.lastIndex - startIndex).toFloat() / performanceGraphTickDivisionsX).roundToInt()
        val ticksX = IntRange(1, performanceGraphTickDivisionsX - 1).map {
            val index = clamp(stepX * it, 0, history.lastIndex - startIndex)
            NamedValue(
                value = index.toFloat(),
                name = formatDateInt(history[startIndex + index].date)
            )
        }
        val ticksY = autoYTicks(
            minY,
            maxY,
            performanceGraphTickDivisionsY,
            performanceGraphTickDivisionsY - 1
        )
        val xAxisLoc = initialContributions.toFloatDollar()

        return TimeSeriesGraphState(
            values = history.slice(startIndex..history.lastIndex).map { TimeSeries((it.returns + initialContributions).toFloatDollar(), it.date) },
            minY = minY,
            maxY = maxY,
            ticksX = ticksX,
            ticksY = ticksY,
            xAxisLoc = if (xAxisLoc > minY && xAxisLoc < maxY) xAxisLoc else null
        )
    }

    private fun Map<String, List<Position>>.getTickers(): MutableSet<String> {
        val tickers = mutableSetOf<String>()
        forEach { accountPositions ->
            accountPositions.value.forEach {
                if (it.ticker != CASH_TICKER) tickers.add(it.ticker)
            }
        }
        return tickers
    }


    private fun getNextHistoryDates(
        positions: Map<String, List<Position>>,
    ): MutableMap<String, Int> {
        val nextHistoryDates = parent.equityHistoryDao.getLastEntries().mapValues {
            val cal = Calendar.getInstance().toNewYork()
            cal.setIntDate(it.value)
            cal.add(Calendar.DATE, 1)
            cal.toIntDate()
        }.toMutableMap()
        positions.forEach { (account, accountPositions) ->
            if (accountPositions.isNotEmpty()) {
                nextHistoryDates.getOrPut(account) {
                    accountPositions.find { it.type == PositionType.CASH }?.date
                        ?: throw RuntimeException("Account $account has no cash lot")
                }
            }
        }
        return nextHistoryDates
    }


    internal suspend fun updateEquityHistory(
        positions: Map<String, List<Position>>,
        stockQuotes: Map<String, TdQuote>,
        optionQuotes: Map<String, TdOptionQuote>
    ) {
        /** Input checking **/
        val tdKey = parent.apiKeys[tdService.name]
        if (tdKey.isNullOrBlank()) return

        if (positions.isEmpty()) return
        val tickers = positions.getTickers()

        /** Get latest close date **/
        // The below doesn't work because the ticker might be an EFT, i.e., and there the tradeTime = regularMarketTradeTime
        // so the hacky logic in isMarketClosed doesn't work
//        val (marketClosedToday, lastCloseDate) = // note last close date might be a sunday, i.e.
//            if (stockQuotes.isEmpty()) Pair(false, getLastCloseDate() ?: return)
//            else isMarketClosed(stockQuotes.values.first())
        val lastCloseDate = getLastCloseDate() ?: return
        Log.d("Zygos/EquityHistoryModel", "lastCloseDate: $lastCloseDate")
        Log.d("Zygos/EquityHistoryModel", "lastUpdateAttemptDate: $lastUpdateAttemptDate")
        if (lastCloseDate <= lastUpdateAttemptDate) return
        lastUpdateAttemptDate = lastCloseDate

        /** Get the next day needed for history. Note that this might not be a market date, i.e. a
         * Saturday if the last history entry is a Friday. **/
        val nextHistoryDates = withContext(Dispatchers.IO) {
            getNextHistoryDates(positions)
        }
        Log.d("Zygos/EquityHistoryModel", "nextHistoryDates $nextHistoryDates")
        val earliestNextHistoryDate = nextHistoryDates.minOf { it.value }
        if (lastCloseDate < earliestNextHistoryDate) return

        /** Use the OHLC from one random ticker to check when market was open **/
        val keyTicker = if (tickers.isEmpty()) "MSFT" else tickers.first()
        val ohlc = updateOhlc(
            ticker = keyTicker,
            apiKey = tdKey,
            startTime = getTimestamp(earliestNextHistoryDate),
            endTime = getTimestamp(lastCloseDate),
        )
        if (ohlc.isEmpty()) return
        val marketDates = ohlc.map { it.date }
        Log.d("Zygos/EquityHistoryModel",  "firstOhlcDate: ${marketDates.first()} lastOhlcDate: ${marketDates.last()}")

        /** Cache ohlc fetches here, also save/load option ohlc **/
        val tickerOhlcs = mutableMapOf(keyTicker to ohlc)
        val quoteDate = stockQuotes[keyTicker]?.regularMarketTradeTimeInLong?.toIntDate() ?: 0
        for (optionQuote in optionQuotes) {
           if (quoteDate == lastCloseDate)
               tickerOhlcs[optionQuote.key] = getOrUpdateOptionOhlc(optionQuote.key, lastCloseDate, optionQuote.value)
        }
        Log.d("Zygos/EquityHistoryModel", "quoteDate $quoteDate")

        /** Update each account's history **/
        for ((account, accountPositions) in positions) {
            if (accountPositions.isEmpty()) continue
            val nextHistoryDate = nextHistoryDates[account] ?: throw RuntimeException("lastHistoryDates missing account")

            /** If the only date is today, use the quotes instead of OHLC **/
            if (marketDates.size == 1 && marketDates[0] == quoteDate) {
                updateEquityFromQuotes(account, accountPositions, quoteDate, stockQuotes, optionQuotes)
            }
            /** Else use OHLC **/
            else if (nextHistoryDate <= marketDates.last()) {
               updateEquityFromOhlc(tdKey, account, accountPositions, tickerOhlcs, nextHistoryDate, marketDates)
            }
        }

        /** Reload UI State **/
        // TODO this sometimes doesn't work because the database might be mid-write still
        loadLaunched(parent.currentAccount)
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

    private fun isMarketClosed(quote: TdQuote): Pair<Boolean, Int> {
        val regularTime = Calendar.getInstance()
        regularTime.timeInMillis = quote.regularMarketTradeTimeInLong
        val tradeTime = Calendar.getInstance()
        tradeTime.timeInMillis = quote.tradeTimeInLong

        val closedToday = regularTime < tradeTime && regularTime.toIntDate() == tradeTime.toIntDate()
        if (!closedToday) regularTime.add(Calendar.DATE, -1)

        Log.d("Zygos/EquityHistoryModel", "closedToday: $closedToday ${regularTime.toIntDate()} ${tradeTime.toIntDate()} quote: $quote")
        return Pair(closedToday, regularTime.toIntDate())
    }

    private fun updateEquityFromQuotes(
        account: String,
        positions: List<Position>,
        date: Int,
        stockQuotes: Map<String, TdQuote>,
        optionQuotes: Map<String, TdOptionQuote>
    ): List<EquityHistory> {
        val prices = stockQuotes.mapValues { it.value.regularMarketLastPrice.toLongDollar() }.toMutableMap()
        optionQuotes.mapValuesTo(prices) { it.value.mark.toLongDollar() }

        val initialContributions = positions.find { it.type == PositionType.CASH }?.shares
            ?: throw RuntimeException("Account $account has no cash lot")

        val newEquity = EquityHistory(
            account = account,
            date = date,
            returns = positions.sumOf { it.equity(prices) } - initialContributions,
        )

        Log.d("Zygos/EquityHistoryModel", "newEquity: $newEquity")
        parent.viewModelScope.launch(Dispatchers.IO) {
            parent.equityHistoryDao.add(newEquity)
        }

        return listOf(newEquity)
    }

    /** This checks and updates the equity history from the ohlc endpoint on TD. It assumes that
     * the positions do not change during this time (TODO).
     */
    private suspend fun updateEquityFromOhlc(
        apiKey: String,
        account: String,
        positions: List<Position>,
        tickerOhlcs: MutableMap<String, List<Ohlc>>,
        nextHistoryDate: Int,
        marketDates: List<Int>,
    ) : List<EquityHistory> {

        val startTime = getTimestamp(nextHistoryDate)
        val initialContributions = positions.find { it.type == PositionType.CASH }?.shares
            ?: throw RuntimeException("Account $account has no cash lot")

        /** Get ohlc for every ticker, transpose to time series **/
        val prices = marketDates.associateBy({ it }, { mutableMapOf<String, Long>() }).toMutableMap()
        positions.forEach {
            if (!it.type.isOption) { // options are done above using today's quotes
                /** First check for ohlc in memory. If none, try to load from database, then from internet **/
                val ohlc = tickerOhlcs.getOrPut(it.ticker) {
                    getOrUpdateOhlc(
                        ticker = it.ticker,
                        apiKey = apiKey,
                        startTime = startTime,
                        endDate = marketDates.last(),
                    )
                }
                Log.d("Zygos/EquityHistoryModel/updateEquityFromOhlc", "tickerOhlc: ${it.ticker} $ohlc")
                for (candle in ohlc) {
                    if (candle.date < nextHistoryDate) continue
                    prices.getOrPut(candle.date) { mutableMapOf() }[it.ticker] = candle.close
                }
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
            Log.d("Zygos/EquityHistoryModel/updateEquityFromOhlc", "datePrices: $datePrices")
        }
        Log.d("Zygos/EquityHistoryModel", "newHistory: $newHistory")
        parent.viewModelScope.launch(Dispatchers.IO) {
            parent.equityHistoryDao.add(newHistory)
        }

        return newHistory
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
            if (savedOhlc.isNotEmpty() && savedOhlc.last().date >= endDate) savedOhlc
            else updateOhlc(
                ticker = ticker,
                apiKey = apiKey,
                startTime = startTime,
                endTime = getTimestamp(endDate),
            )
        }
    }

    private suspend fun getOrUpdateOptionOhlc(symbol: String, date: Int, quote: TdOptionQuote): MutableList<Ohlc> {
        return withContext(Dispatchers.IO) {
            val savedOhlc = parent.ohlcDao.getTicker(symbol).toMutableList()
            if (savedOhlc.isNotEmpty() && savedOhlc.last().date >= date) savedOhlc
            else {
                val ohlc = Ohlc(
                    ticker = symbol,
                    date = date,
                    open = quote.openPrice.toLongDollar(),
                    high = quote.highPrice.toLongDollar(),
                    low = quote.lowPrice.toLongDollar(),
                    close = quote.mark.toLongDollar(),
                    volume = quote.totalVolume,
                )
                parent.viewModelScope.launch(Dispatchers.IO) {
                    parent.ohlcDao.add(ohlc)
                }
                savedOhlc.add(ohlc)
                savedOhlc
            }
        }
    }



}