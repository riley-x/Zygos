package com.example.zygos.viewModel

import android.util.Log
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import com.example.zygos.ui.holdings.holdingsListDisplayOptions
import com.example.zygos.ui.holdings.holdingsListSortOptions

class TestViewModel: ViewModel() {
    val accounts = mutableStateListOf<String>("Robinhood", "Arista", "TD Ameritrade", "Alhena", "All Accounts")
    var currentAccount by mutableStateOf(accounts[0])
        private set

    /** PerformanceScreen **/
    val accountStartingValue = 12f
    val accountPerformance = List(20) {
        NamedValue(it * if (it % 2 == 0) 1.2f else 0.8f, "$it/${it * 2}")
    }.toMutableStateList()
    val accountPerformanceTicksY = mutableStateListOf(5f, 10f, 15f, 20f)
    val accountPerformanceTicksX = mutableStateListOf(5, 10, 15)
    val accountPerformanceRange = mutableStateOf(accountPerformanceRangeOptions.items.last())

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
    val ohlc = List(21) {
        Ohlc(it.toFloat(), it * 2f, 0.5f * it,it * if (it % 2 == 0) 1.2f else 0.8f, "$it/${it * 2}")
    }.drop(1).toMutableStateList()
}