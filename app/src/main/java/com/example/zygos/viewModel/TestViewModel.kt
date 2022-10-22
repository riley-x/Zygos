package com.example.zygos.viewModel

import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import com.example.zygos.data.LotPosition
import com.example.zygos.data.PositionType
import com.example.zygos.data.PricedPosition
import com.example.zygos.data.database.Transaction
import com.example.zygos.data.database.TransactionType
import com.example.zygos.data.toLongDollar
import com.example.zygos.network.TdOhlc
import com.example.zygos.network.TdQuote
import com.example.zygos.ui.graphing.TimeSeriesGraphState
import com.example.zygos.ui.holdings.HoldingsListSortOptions
import com.example.zygos.ui.theme.defaultTickerColors
import java.util.*

class TestViewModel: ViewModel() {
    val accounts = mutableStateListOf<String>("Robinhood", "Arista", "TD Ameritrade", "Alhena", "All Accounts")
    var currentAccount by mutableStateOf(accounts[0])

    val tickerColors = SnapshotStateMap<String, Color>()
    init {
        tickerColors.putAll(defaultTickerColors)
    }

    /** PerformanceScreen **/
    var accountPerformanceState = mutableStateOf(TimeSeriesGraphState(
        xAxisLoc = 12f,
        values = List(20) {
            TimeSeries(it * if (it % 2 == 0) 1.2f else 0.8f, it)
        },
        ticksY = listOf(5f, 10f, 15f, 20f).map { NamedValue(it, it.toString()) },
        ticksX = listOf(5f, 10f, 15f).map { NamedValue(it, it.toString()) },
        minY = 0f,
        maxY = 25f,
    ))
    val accountPerformanceTimeRange = mutableStateOf(accountPerformanceRangeOptions.items.last())

    val watchlist = mutableStateListOf(
        Quote(0, "t1", Color.Blue,  123.23f,  21.20f, 0.123f),
        Quote(1, "t2", Color.Black, 1263.23f, 3.02f,  -0.123f),
        Quote(2, "t3", Color.Green, 1923.23f, 120.69f,0.263f),
        Quote(3, "t4", Color.Cyan,  1423.23f, 0.59f,  1.23f),
        Quote(4, "t5", Color.Blue,  123.23f,  21.20f, 0.123f),
        Quote(5, "t6", Color.Black, 1263.23f, 3.02f,  -0.123f),
        Quote(6, "t7", Color.Green, 1923.23f, 120.69f,0.263f),
        Quote(7, "t8", Color.Cyan,  1423.23f, 0.59f,  1.23f),
    )

    // These variables are merely the ui state of the options selection menu
    // The actual sorting is called in sortWatchlist() via a callback when
    // the menu is hidden.
    var watchlistSortOption by mutableStateOf(watchlistSortOptions.items[0])
        private set
    var watchlistSortIsAscending by mutableStateOf(true)
        private set
    var watchlistDisplayOption = mutableStateOf(watchlistDisplayOptions.items[0])

    /** Holdings **/

    val quotes = mutableStateMapOf(
        "MSFT" to TdQuote(
            symbol = "MSFT",
            mark = 120f,
            lastPrice = 120f,
            openPrice = 120f,
            highPrice = 120f,
            lowPrice = 120f,
            closePrice = 120f,
            totalVolume = 1000000,
            netPercentChangeInDouble = 2f,
            netChange = 10f,
            tradeTimeInLong = 0,
            regularMarketLastPrice = 120f,
            regularMarketTradeTimeInLong = 0,
            regularMarketPercentChangeInDouble = 2f,
            regularMarketNetChange = 10f
        )
    )
    val markPrices = quotes.mapValues { it.value.mark.toLongDollar() }
    val closePrices = quotes.mapValues { it.value.closePrice.toLongDollar() }
    val percentChanges = quotes.mapValues { it.value.netPercentChangeInDouble }
    val lots = mutableListOf(
        LotPosition(
            account = "Robinhood",
            ticker = "MSFT",
            type = PositionType.STOCK,
            shares = 5,
            priceOpen = 2000000,
            realizedOpen = 200000,
            realizedClosed = 1000000,
        ),
        LotPosition(
            account = "Robinhood",
            ticker = "MSFT",
            type = PositionType.CALL_LONG,
            shares = 100,
            priceOpen = 500000,
            expiration = 20231010,
            strike = 1250000,
        ),
        LotPosition(
            account = "Robinhood",
            ticker = "AMD",
            type = PositionType.STOCK,
            shares = 10,
            priceOpen = 1000000, // $100
            realizedOpen = 200000, // $20
            realizedClosed = 0,
        ),
        LotPosition(
            account = "Robinhood",
            ticker = "CASH",
            type = PositionType.CASH,
            shares = 20000000,
            priceOpen = -1,
            realizedClosed = 670900,
        ),
    )
    val totalEquity = lots.sumOf { it.equity(markPrices) }
    val longPositions = mutableStateListOf(
        PricedPosition(lot = lots[0] + lots[1], markPrices = markPrices, closePrices = closePrices, percentChanges = percentChanges, totalEquity),
        PricedPosition(lot = lots[2], markPrices = markPrices, closePrices = closePrices, percentChanges = percentChanges, totalEquity),
        PricedPosition(lot = lots[3], markPrices = markPrices, closePrices = closePrices, percentChanges = percentChanges, totalEquity),
    )
    val shortPositions = mutableStateListOf<PricedPosition>()

    // These variables are merely the ui state of the options selection menu
    // The actual sorting is called in sortHoldingsList() via a callback when
    // the menu is hidden.
    var holdingsSortOption by mutableStateOf(HoldingsListSortOptions.EQUITY)
        private set
    var holdingsSortIsAscending by mutableStateOf(true)
        private set
    var holdingsDisplayOption by mutableStateOf(HoldingsListSortOptions.EQUITY)


    /** ChartScreen **/
    val chartTicker = mutableStateOf("")
    val chartState = mutableStateOf(
        TimeSeriesGraphState<OhlcNamed>(
            values = List(21) {
                OhlcNamed(
                    it.toFloat(),
                    it * 2f,
                    0.5f * it,
                    it * if (it % 2 == 0) 1.2f else 0.8f,
                    it.toString(),
                )
            }.drop(1),
            ticksY = listOf(5f, 10f, 15f, 20f).map { NamedValue(it, it.toString()) },
            ticksX = listOf(5f, 10f, 15f).map { NamedValue(it, it.toString()) },
            padX = 1f,
            minY = 0f,
            maxY = 25f,
        )
    )
    val chartRange = mutableStateOf(TimeRange.ONE_YEAR)

    /** TransactionScreen **/
    val transactions = mutableStateListOf(
        Transaction(
            transactionId = 0,
            ticker = "CASH",
            account = "Robinhood",
            date = 20201020,
            note = "",
            type = TransactionType.TRANSFER,
            value = 100000000,
        ),
        Transaction(
            transactionId = 1,
            account = "Robinhood",
            ticker = "MSFT",
            note = "",
            type = TransactionType.STOCK,
            shares = 5,
            date = 20220928,
            expiration = 0,
            price = 2000000,
            value = 10000000,
        ),
        Transaction(
            transactionId = 2,
            account = "Arista",
            ticker = "AAPL",
            note = "",
            type = TransactionType.DIVIDEND,
            shares = 0,
            date = 20220928,
            expiration = 0,
            price = 345100,
            value = 5413000,
        ),
        Transaction(
            transactionId = 3,
            account = "TD Ameritrade",
            ticker = "AMD",
            note = "",
            type = TransactionType.CALL_LONG,
            shares = 100,
            date = 20220928,
            expiration = 20240724,
            strike = 300000,
            price = 360000,
            value = -36000000,
        ),
    )
    val focusedTransaction = mutableStateOf(transactions[0]) // Current transaction that we're editing
    var transactionListSortOption by mutableStateOf(transactionSortOptions.items[0])
        private set
    var transactionListSortIsAscending by mutableStateOf(true)
        private set

    val apiKeys = mutableStateMapOf(
        "IEX" to "123456",
        "Alpha Vantage" to "asdfgh",
        "Polygon" to "qwertyu",
        "TD Ameritrade" to "qwertyu"
    )
}