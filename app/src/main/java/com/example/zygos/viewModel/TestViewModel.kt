package com.example.zygos.viewModel

import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import com.example.zygos.data.database.Transaction
import com.example.zygos.data.database.TransactionType
import com.example.zygos.ui.holdings.holdingsListDisplayOptions
import com.example.zygos.ui.holdings.holdingsListSortOptions
import com.example.zygos.ui.theme.defaultTickerColors

class TestViewModel: ViewModel() {
    val accounts = mutableStateListOf<String>("Robinhood", "Arista", "TD Ameritrade", "Alhena", "All Accounts")
    var currentAccount by mutableStateOf(accounts[0])

    val tickerColors = SnapshotStateMap<String, Color>()
    init {
        tickerColors.putAll(defaultTickerColors)
    }

    /** PerformanceScreen **/
    var accountPerformanceState = mutableStateOf(AccountPerformanceState(
        xAxisLoc = 12f,
        values = List(20) {
            TimeSeries(it * if (it % 2 == 0) 1.2f else 0.8f, it, "$it/${it * 2}")
        },
        ticksY = listOf(5f, 10f, 15f, 20f),
        ticksX = listOf(5, 10, 15),
        minY = 0f,
        maxY = 25f,
    ))
    val accountPerformanceTimeRange = mutableStateOf(accountPerformanceRangeOptions.items.last())

    val watchlist = mutableStateListOf(
        Quote("t1", Color.Blue,  123.23f,  21.20f, 0.123f),
        Quote("t2", Color.Black, 1263.23f, 3.02f,  -0.123f),
        Quote("t3", Color.Green, 1923.23f, 120.69f,0.263f),
        Quote("t4", Color.Cyan,  1423.23f, 0.59f,  1.23f),
        Quote("t5", Color.Blue,  123.23f,  21.20f, 0.123f),
        Quote("t6", Color.Black, 1263.23f, 3.02f,  -0.123f),
        Quote("t7", Color.Green, 1923.23f, 120.69f,0.263f),
        Quote("t8", Color.Cyan,  1423.23f, 0.59f,  1.23f),
    )

    // These variables are merely the ui state of the options selection menu
    // The actual sorting is called in sortWatchlist() via a callback when
    // the menu is hidden.
    var watchlistSortOption by mutableStateOf(watchlistSortOptions.items[0])
        private set
    var watchlistSortIsAscending by mutableStateOf(true)
        private set
    var watchlistDisplayOption by mutableStateOf(watchlistDisplayOptions.items[0])


    /** Holdings **/
    val positions = mutableStateListOf(
        Position("p1", 0.2f, Color(0xFF004940)),
        Position("p2", 0.3f, Color(0xFF005D57)),
        Position("p3", 0.4f, Color(0xFF04B97F)),
        Position("p4", 0.1f, Color(0xFF37EFBA)),
        Position("p5", 0.2f, Color(0xFF004940)),
        Position("p6", 0.3f, Color(0xFF005D57)),
        Position("p7", 0.4f, Color(0xFF04B97F)),
        Position("p8", 0.1f, Color(0xFF37EFBA))
    )

    // These variables are merely the ui state of the options selection menu
    // The actual sorting is called in sortHoldingsList() via a callback when
    // the menu is hidden.
    var holdingsSortOption by mutableStateOf(holdingsListSortOptions.items[0])
        private set
    var holdingsSortIsAscending by mutableStateOf(true)
        private set
    var holdingsDisplayOption by mutableStateOf(holdingsListDisplayOptions.items[0])


    /** ChartScreen **/
    val chartTicker = mutableStateOf("")
    val chartState = mutableStateOf(ChartState(
        values = List(21) {
            OhlcNamed(it.toFloat(), it * 2f, 0.5f * it,it * if (it % 2 == 0) 1.2f else 0.8f, "$it/${it * 2}")
        }.drop(1),
        ticksY = listOf(5f, 10f, 15f, 20f),
        ticksX = listOf(5, 10, 15),
        padX = 1f,
        minY = 0f,
        maxY = 25f,
    ))
    val chartRange = mutableStateOf(chartRangeOptions.items.last())

    /** TransactionScreen **/
    val transactions = mutableStateListOf(
        Transaction(
            ticker = "CASH",
            account = "Robinhood",
            date = 20201020,
            note = "",
            type = TransactionType.TRANSFER,
            value = 100000000,
        ),
        Transaction(
            transactionId = 0,
            account = "Robinhood",
            ticker = "MSFT",
            note = "",
            type = TransactionType.STOCK,
            shares = 5,
            date = 20220928,
            expiration = 0,
            price = 2000000,
            value = 10000000,
            fees = 0
        ),
        Transaction(
            transactionId = 1,
            account = "Arista",
            ticker = "AAPL",
            note = "",
            type = TransactionType.DIVIDEND,
            shares = 0,
            date = 20220928,
            expiration = 0,
            price = 345100,
            value = 5413000,
            fees = 0
        ),
        Transaction(
            transactionId = 2,
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
            fees = 0
        ),
    )
}