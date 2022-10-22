package com.example.zygos.viewModel

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.FileUtils
import android.util.Log
import androidx.compose.runtime.*
import androidx.core.content.FileProvider.getUriForFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.room.util.FileUtil
import com.example.zygos.ZygosApplication
import com.example.zygos.data.*
import com.example.zygos.network.apiServices
import com.example.zygos.ui.components.allAccounts
import com.example.zygos.ui.components.noAccountMessage
import kotlinx.coroutines.*
import java.io.File


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
    internal val namesDao = application.database.namesDao()

    /** Account state **/
    val accounts = mutableStateListOf(noAccountMessage)
    var currentAccount by mutableStateOf(accounts[0])
        private set
    /** Lots **/
    internal val lots = mutableMapOf<String, LotModel>()
    /** Prices **/
    val market = MarketModel(this)
    /** Performance Screen **/
    val equityHistory = EquityHistoryModel(this)
    val watchlist = WatchlistModel(this)
    /** Priced Positions **/
    val longPositions = PositionModel(this)
    val shortPositions = PositionModel(this)
    val detailedPosition = mutableStateOf(PricedPosition()) // Position in focus after selecting from the holdings screen
    /** ChartScreen **/
    val chart = ChartModel(this)
    /** Color Selection Screen **/
    val colors = ColorModel(this)
    /** TransactionScreen **/
    val transactions = TransactionModel(this)




    /** Main startup sequence that loads all data!
     * This should be called from a LaunchedEffect(Unit). UI will update as each state variable
     * gets updated.
     */
    suspend fun startup() {
        val localFileDir = application.filesDir ?: return
        Log.d("Zygos/ZygosViewModel/startup", localFileDir.absolutePath)

        /** Load accounts **/
        val accs = withContext(Dispatchers.IO) {
            readAccounts(localFileDir)
        }
        if (accs.isEmpty()) return // Initial values are set for empty data already
        accounts.clear()
        accounts.addAll(accs)
        accounts.add(allAccounts)

        /** Load API keys **/
        apiServices.forEach {
            val key = preferences?.getString(it.preferenceKey, "") ?: ""
            if (key.isNotBlank()) {
                apiKeys[it.name] = key
            }
        }

        viewModelScope.launch {
            /** Load colors **/
            colors.load()
            /** Load watchlist **/
            watchlist.load(emptyMap(), colors.tickers)
        }

        /** Load lots **/
        val jobs = mutableListOf<Job>()
        for (acc in accounts) {
            val model = lots.getOrPut(acc) { LotModel(this) }
            jobs.add(model.loadLaunched(acc))
        }
        jobs.joinAll() // need to block here before setAccount, which doesn't reload lots

        /** Set UI state **/
        setAccount(allAccounts)
    }


    fun addAccount(account: String) {
        val localFileDir = application.filesDir ?: return
        if (accounts.last() == allAccounts) {
            accounts.removeAt(accounts.lastIndex)
            accounts.add(account)
        }
        else if (accounts[0] == noAccountMessage || accounts.isEmpty()) {
            accounts.clear()
            accounts.add(account)
        } else {
            throw RuntimeException("Accounts wack: $accounts")
        }

        viewModelScope.launch(Dispatchers.IO) {
            writeAccounts(localFileDir, accounts)
        }
        accounts.add(allAccounts)
        setAccount(account)
    }

    fun setAccount(account: String) {
        if (currentAccount == account) return
        currentAccount = account

        viewModelScope.launch {
            loadAccount(account)
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

        equityHistory.initialContributions = lotModel.cashPosition?.shares ?: 0L
        equityHistory.loadLaunched(account)

        loadPricedData(lotModel, true) // dummy data assuming 0 unrealized gains

        // TODO place this into a timer
        if (market.updatePrices(getAllTickers(), getAllOptionNames())) {
            loadPricedData(lotModel)
        }

        /** Logs **/
        Log.i("Zygos/ZygosViewModel/loadAccount", "transactions (possibly stale): ${transactions.all.size}") // since the transactions are launched, this could be stale


        // Don't actually need to block, the state list update is scheduled in a coroutine already
//        jobs.joinAll()
    }

    private fun loadPricedData(lotModel: LotModel, isDummy: Boolean = false) {

        /** Load watchlist **/
        viewModelScope.launch {
            watchlist.load(market.stockQuotes, colors.tickers)
        }

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

            if (!isDummy) updateEquityHistory()
        }
    }


    suspend fun sortList(whichList: String) {
        when(whichList) {
            "watchlist" -> watchlist.sort()
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


    private fun updateEquityHistory() {
        viewModelScope.launch {
            try {
                val positions = mutableMapOf<String, List<Position>>()
                for ((account, lotModel) in lots) {
                    if (account == allAccounts || account == noAccountMessage) continue
                    positions[account] = lotModel.longPositions + lotModel.shortPositions +
                            (lotModel.cashPosition?.let { listOf(it) } ?: emptyList())
                }
                equityHistory.updateEquityHistory(
                    positions = positions,
                    stockQuotes = market.stockQuotes,
                    optionQuotes = market.optionQuotes,
                )
            } catch (e: Exception) {
                Log.w("Zygos/ZygosViewModel", e.stackTraceToString())
            }
        }
    }

    fun getAllTickers(): MutableSet<String> {
        val tickers = mutableSetOf<String>()
        lots.forEach {
            it.value.tickerLots.forEach { (ticker, realizedAndLots) ->
                if (realizedAndLots.second.isNotEmpty()) tickers.add(ticker)
            }
        }
        tickers.remove(CASH_TICKER)
        return tickers
    }

    fun getAllOptionNames(): MutableSet<String> {
        val names = mutableSetOf<String>()
        lots.forEach {
            names.addAll(it.value.optionNames())
        }
        return names
    }

    fun backupDatabase(context: Context) {
        viewModelScope.launch {

            /** First we copy the database file to the app's private cache directory **/
            val db = application.getDatabasePath("app_database")
            val backup = File(application.cacheDir, "backup_database")
            withContext(Dispatchers.IO) {
                /** Force synchronize the wal file **/
                // https://stackoverflow.com/a/51560124/10988347transactionDao.checkpoint()
                transactionDao.checkpoint()

                // https://stackoverflow.com/a/46344186/10988347
                db.inputStream().use { input ->
                    backup.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            }

            /** Next create an intent to share the file **/
            val backupUri = getUriForFile(application, "com.example.zygos.fileprovider", backup)
            // content://com.example.zygos.fileprovider/cache/backup_database

            // See
            // https://developer.android.com/training/secure-file-sharing/setup-sharing
            // https://developer.android.com/reference/androidx/core/content/FileProvider
            // https://stackoverflow.com/a/62928442/10988347
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "application/sql" // this is the MIME type, see https://developer.mozilla.org/en-US/docs/Web/HTTP/Basics_of_HTTP/MIME_types
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                putExtra(Intent.EXTRA_STREAM, backupUri)
            }
            val shareIntent = Intent.createChooser(sendIntent, "Backup Database")
            context.startActivity(shareIntent)
        }
    }
}