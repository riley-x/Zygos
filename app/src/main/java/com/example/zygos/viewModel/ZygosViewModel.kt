package com.example.zygos.viewModel

import android.util.Log
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import com.example.zygos.data.readAccounts
import com.example.zygos.data.writeAccounts
import com.example.zygos.ui.components.allAccounts
import com.example.zygos.ui.components.noAccountMessage
import com.example.zygos.ui.holdings.holdingsListDisplayOptions
import com.example.zygos.ui.holdings.holdingsListSortOptions
import kotlinx.coroutines.delay
import java.io.File



class ZygosViewModel : ViewModel() {

    /** Account state **/
    val accounts = mutableStateListOf(noAccountMessage)
    //val accounts: List<String> = _accounts // Warning: these backing vals seem to ruin smart recomposition

    var currentAccount by mutableStateOf(accounts[0])
        private set

    // Sets the current account to display, loading the data elements into the ui state variables
    fun setAccount(account: String) {
        if (currentAccount == account) return
        // TODO
        currentAccount = account
    }

    /** PerformanceScreen **/
    val accountStartingValue by mutableStateOf(12f)
    val accountPerformance = List(20) {
        NamedValue(it * if (it % 2 == 0) 1.2f else 0.8f, "$it/${it * 2}")
    }.toMutableStateList()
    val accountPerformanceTicksY = mutableStateListOf(5f, 10f, 15f, 20f)
    val accountPerformanceTicksX = mutableStateListOf(5, 10, 15)
    val accountPerformanceRange = mutableStateOf(accountPerformanceRangeOptions.items.last())

    fun setPerformanceRange(range: String) {
        accountPerformanceRange.value = range
    }

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

    // Cached sort options to not re-sort if nothing was changed
    private var watchlistLastSortOption = ""
    private var watchlistLastSortIsAscending = true

    // Called from composable onClick callbacks
    fun setWatchlistSortMethod(opt: String) {
        if (watchlistSortOption == opt) watchlistSortIsAscending = !watchlistSortIsAscending
        else watchlistSortOption = opt
    }

    // This happens asynchronously! Make sure that all other state is ok with the positions list being modified
    fun sortWatchlist() {
        Log.i("ZygosViewModel/sortWatchlist", "$watchlistSortOption $watchlistSortIsAscending, last: $watchlistLastSortOption $watchlistLastSortIsAscending")
        if (watchlistLastSortOption == watchlistSortOption) {
            if (watchlistLastSortIsAscending != watchlistSortIsAscending) {
                watchlist.reverse()
            }
        } else {
            if (watchlistSortIsAscending) {
                when (watchlistSortOption) {
                    "Ticker" -> watchlist.sortBy(Quote::ticker)
                    else -> watchlist.sortBy(Quote::percentChange)
                }
            } else {
                when (watchlistSortOption) {
                    "Ticker" -> watchlist.sortByDescending(Quote::ticker)
                    else -> watchlist.sortByDescending(Quote::percentChange)
                }
            }
        }
        watchlistLastSortIsAscending = watchlistSortIsAscending
        watchlistLastSortOption = watchlistSortOption
    }

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

    // Cached sort options to not re-sort if nothing was changed
    private var holdingsLastSortOption = ""
    private var holdingsLastSortIsAscending = true

    // Called from composable onClick callbacks
    fun setHoldingsSortMethod(opt: String) {
        if (holdingsSortOption == opt) holdingsSortIsAscending = !holdingsSortIsAscending
        else holdingsSortOption = opt
    }

    // This happens asynchronously! Make sure that all other state is ok with the positions list being modified
    fun sortHoldingsList() {
        //Log.i("ZygosViewModel", "$holdingsSortOption $lastSortOption")
        if (holdingsLastSortOption == holdingsSortOption) {
            if (holdingsLastSortIsAscending != holdingsSortIsAscending) {
                positions.reverse()
            }
        } else {
            if (holdingsSortIsAscending) {
                when (holdingsSortOption) {
                    "Ticker" -> positions.sortBy(Position::ticker)
                    else -> positions.sortBy(Position::value)
                }
            } else {
                when (holdingsSortOption) {
                    "Ticker" -> positions.sortByDescending(Position::ticker)
                    else -> positions.sortByDescending(Position::value)
                }
            }
        }
        holdingsLastSortIsAscending = holdingsSortIsAscending
        holdingsLastSortOption = holdingsSortOption
    }

    fun sortList(whichList: String) {
        when(whichList) {
            "holdings" -> sortHoldingsList()
            "watchlist" -> sortWatchlist()
        }
    }

    /** ChartScreen **/
    val chartTicker = mutableStateOf("")
    val chartData = List(21) {
        Ohlc(it.toFloat(), it * 2f, 0.5f * it,it * if (it % 2 == 0) 1.2f else 0.8f, "$it/${it * 2}")
    }.drop(1).toMutableStateList()
    val chartTicksY = mutableStateListOf(5f, 10f, 15f, 20f)
    val chartTicksX = mutableStateListOf(5, 10, 15)
    val chartRange = mutableStateOf(chartRangeOptions.items.last())

    fun setTicker(ticker: String) {
        chartTicker.value = ticker
    }
    fun setChartRange(range: String) {
        chartRange.value = range
    }

    /** Main startup sequence that loads all data!
     * This should be called from a LaunchedEffect(Unit). UI will update as each state variable
     * gets updated.
     */
    fun startup(
        localFileDir: File,
    ) {
        Log.d("Zygos/ZygosViewModel/startup", localFileDir.absolutePath)
        val accs = readAccounts(localFileDir)
        if (accs.isEmpty()) {
            return // Initial values are set for empty data already
        }

        accounts.clear()
        accounts.addAll(accs)
        accounts.add(allAccounts)

        // Load all data into persistent memory?

        setAccount(allAccounts)
    }

    fun addAccount(localFileDir: File, account: String) {
        if (accounts.last() == allAccounts) {
            accounts.add(accounts.lastIndex, account)
            writeAccounts(localFileDir, accounts.dropLast(1))
            setAccount(account)
        }
        else if (accounts[0] == noAccountMessage || accounts.isEmpty()) {
            accounts.clear()
            accounts.add(account)
            writeAccounts(localFileDir, accounts)
            accounts.add(allAccounts)
            setAccount(account)
        } else {
            Log.w("Zygos/ZygosViewModel/addAccount", "Accounts wack: $accounts")
        }
    }




}