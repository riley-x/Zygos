package com.example.zygos.viewModel

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.zygos.ZygosApplication
import com.example.zygos.data.*
import com.example.zygos.network.apiServices
import com.example.zygos.ui.components.allAccounts
import com.example.zygos.ui.components.noAccountMessage
import com.example.zygos.ui.graphing.TimeSeriesGraphState
import kotlinx.coroutines.*


const val performanceGraphYPad = 0.1f // fractional padding for each top/bottom
const val performanceGraphTickDivisionsX = 4 // the number of ticks shown is this - 1
const val performanceGraphTickDivisionsY = 5 // the number of ticks shown is this - 1


class ZygosViewModelFactory(
    private val application: ZygosApplication
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ZygosViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ZygosViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


typealias ChartState = TimeSeriesGraphState<OhlcNamed>



const val CASH_TICKER = "CASH" // TODO change to something not an actual ticker



class ZygosViewModel(private val application: ZygosApplication) : ViewModel() {

    /** Preferences **/
    internal val preferences: SharedPreferences? = application.getSharedPreferences(
        PREFERENCE_FILE_KEY,
        Context.MODE_PRIVATE,
    )
    val apiKeys = mutableStateMapOf<String, String>()
    var currentEditApiKey by mutableStateOf(apiServices[0])
    fun saveApiKey(newKey: String) {
        apiKeys[currentEditApiKey.name] = newKey
        if (preferences != null) {
            with(preferences.edit()) {
                putString(currentEditApiKey.preferenceKey, newKey)
                apply()
            }
        }
    }

    /** DAOs **/
    internal val transactionDao = application.database.transactionDao()
    internal val equityHistoryDao = application.database.equityHistoryDao()
    internal val lotDao = application.database.lotDao()
    internal val ohlcDao = application.database.ohlcDao()
    internal val colorDao = application.database.colorDao()

    /** Account state **/
    val accounts = mutableStateListOf(noAccountMessage)
    //val accounts: List<String> = _accounts // Warning: these backing vals seem to ruin smart recomposition

    var currentAccount by mutableStateOf(accounts[0])
        private set

    /** Account Performance **/

    /** Watchlist **/
    // TODO move into subclass
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

    /** Lots **/
    internal val lots = mutableMapOf<String, LotModel>()
    /** Prices **/
    val market = MarketModel(this)
    /** Performance Screen **/
    val equityHistory = EquityHistoryModel(this)
    /** Priced Positions **/
    val longPositions = PositionModel(this)
    val shortPositions = PositionModel(this)
    val detailedPosition = mutableStateOf(PricedPosition()) // Position in focus after selecting from the holdings screen
    /** Color Selection Screen **/
    val colors = ColorModel(this)
    /** TransactionScreen **/
    val transactions = TransactionModel(this)


    /** ChartScreen **/
    // TODO move into subclass
    val chartTicker = mutableStateOf("")
    val chartState = mutableStateOf(ChartState())
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
    suspend fun startup() {
        val localFileDir = application.filesDir ?: return
        Log.d("Zygos/ZygosViewModel/startup", localFileDir.absolutePath)

        // TODO place into a async?
        val accs = readAccounts(localFileDir)
        if (accs.isEmpty()) {
            return // Initial values are set for empty data already
        }
        accounts.clear()
        accounts.addAll(accs)
        accounts.add(allAccounts)

        apiServices.forEach {
            val key = preferences?.getString(it.preferenceKey, "") ?: ""
            if (key.isNotBlank()) {
                apiKeys[it.name] = key
            }
        }

        colors.loadLaunched()


        val jobs = mutableListOf<Job>()
        for (acc in accounts) {
            val model = lots.getOrPut(acc) { LotModel(this) }
            jobs.add(model.loadLaunched(acc))
        }

//        Log.i("Zygos/ZygosViewModel/startup", application.getDatabasePath("app_database").absolutePath)
        // /data/user/0/com.example.zygos/databases/app_database

        jobs.joinAll() // need to block here before setAccount, which doesn't reload lots
        setAccount(allAccounts)
    }

    fun addAccount(account: String) {
        val localFileDir = application.filesDir ?: return
        if (accounts.last() == allAccounts) {
            accounts.add(accounts.lastIndex, account)
            writeAccounts(localFileDir, accounts.dropLast(1))
        }
        else if (accounts[0] == noAccountMessage || accounts.isEmpty()) {
            accounts.clear()
            accounts.add(account)
            writeAccounts(localFileDir, accounts)
            accounts.add(allAccounts)
        } else {
            throw RuntimeException("Accounts wack: $accounts")
        }
        setAccount(account)
    }

    fun setAccount(account: String) {
        if (currentAccount == account) return
        currentAccount = account

        viewModelScope.launch {
            loadAccount(account)
        }
    }


    private fun loadPricedData(lotModel: LotModel, isDummy: Boolean = false) {
        /** This check, the load launch, and the reset happen on the main thread, so there's no
         * way they can conflict. However, need to copy the lists since both the loads below and
         * the loads in lotModel happen on dispatched threads
         */
        if (!lotModel.isLoading) {
            Log.i("Zygos/ZygosViewModel/loadAccount", "ticker lots: ${lotModel.tickerLots.size}")
            Log.i("Zygos/ZygosViewModel/loadAccount", "long lots: ${lotModel.longPositions.size}")
            lotModel.logPositions()

            val long = if (lotModel.cashPosition != null) lotModel.longPositions + listOf(lotModel.cashPosition!!) else lotModel.longPositions.toList()
            val short = lotModel.shortPositions.toList()

            equityHistory.setCurrent(long + short, market)

            longPositions.loadLaunched(long, market.markPrices, market.closePrices, market.percentChanges, equityHistory.current)
            shortPositions.loadLaunched(short, market.markPrices, market.closePrices, market.percentChanges, equityHistory.current)

            if (!isDummy) updateHistory()
        }
    }


    /** Sets the current account to display, loading the data elements into the ui state variables.
     * This should be run on the main thread, but in a coroutine. **/
    internal suspend fun loadAccount(account: String) {
        /** Guards **/
        longPositions.isLoading = true // these are reset by the respective loads
        shortPositions.isLoading = true // they need to be here because the lots load blocks

        transactions.loadLaunched(account)
        val lotModel = lots.getOrPut(account) { LotModel(this) }
        colors.insertDefaults(lotModel.tickerLots.keys)

        val tickers = lotModel.tickerLots.keys // TODO replace with all tickers
        tickers.remove(CASH_TICKER)

        equityHistory.initialContributions = lotModel.cashPosition?.shares ?: 0L
        equityHistory.loadLaunched(account)

        loadPricedData(lotModel, true) // dummy data assuming 0 unrealized gains

        // TODO place this into a timer
        if (market.updatePrices(tickers, lotModel.optionNames())) {
            loadPricedData(lotModel)
        }

        /** Logs **/
        Log.i("Zygos/ZygosViewModel/loadAccount", "transactions (possibly stale): ${transactions.all.size}") // since the transactions are launched, this could be stale


        // Don't actually need to block, the state list update is scheduled in a coroutine already
//        jobs.joinAll()
    }

    fun sortList(whichList: String) {
        when(whichList) {
            "watchlist" -> sortWatchlist()
        }
    }

    fun recalculateAllLots() {
        viewModelScope.launch {
            longPositions.isLoading = true // these are reset by the respective loads
            shortPositions.isLoading = true // they need to be here because the recalculate blocks below

            withContext(Dispatchers.IO) {
                recalculateAll(transactionDao, lotDao)
            }
            reloadLots(allAccounts)
            loadAccount(currentAccount)
        }
    }

    suspend fun reloadLots(account: String) {
        val jobs = mutableListOf<Job>()
        lots.forEach { (acc, lotModel) ->
            if (acc == allAccounts || account == allAccounts || acc == account) {
                jobs.add(lotModel.loadLaunched(acc))
            }
        }
        jobs.joinAll()
    }


    fun updateHistory() {
        viewModelScope.launch {
            try {
                val positions = lots.mapValues { (_, lotModel) ->
                    lotModel.longPositions + lotModel.shortPositions +
                            (lotModel.cashPosition?.let { listOf(it) } ?: emptyList())
                }
                equityHistory.updateEquityHistory(
                    positions = positions,
                    stockQuotes = market.stockQuotes,
                    optionQuotes = market.optionQuotes,
                )
            } catch (e: Exception) {
                Log.w("Zygos/ZygosViewModel", "Failure: ${e::class} ${e.message}")
            }
        }
    }
}