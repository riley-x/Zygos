package com.example.zygos.viewModel

import android.util.Log
import androidx.annotation.MainThread
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.example.zygos.data.database.Names
import com.example.zygos.network.TdQuote
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WatchlistModel(private val parent: ZygosViewModel) {
    /** There should be one quote per ticker. Note the tickers are loaded from the database at
     * startup but the quotes rely on market data. **/
    internal val tickers = mutableSetOf<String>()
    val quotes = mutableStateListOf<Quote>()

    /** This is required since the ticker rows in the watchlist remember the swipeable state. If you
     * use the ticker as the key, and delete a row then add it back, it'll remember the swipe state
     * to be deleted still. This var will survive with the viewModel scope, which should be the same
     * as rememberSaveable **/
    var lastQuoteLazyKey = 0

    /** These variables are merely the ui state of the options selection menu. The actual sorting is
     * called in sortWatchlist() via a callback when the menu is hidden.
     */
    var sortOption by mutableStateOf(watchlistSortOptions.items[0])
        private set
    var sortIsAscending by mutableStateOf(true)
        private set
    val displayOption = mutableStateOf(watchlistDisplayOptions.items[1])

    /** Cached sort options to not re-sort if nothing was changed **/
    private var lastSortOption = ""
    private var lastSortIsAscending = true

    /** Called from composable onClick callbacks. Sets UI state only **/
    @MainThread
    fun setSortMethod(opt: String) {
        if (sortOption == opt) sortIsAscending = !sortIsAscending
        else sortOption = opt
    }

    private fun getSortedList(
        oldList: List<Quote>,
        option: String,
        ascending: Boolean
    ): MutableList<Quote> {
        if (oldList.isEmpty()) return mutableListOf()
        val newList = oldList.toMutableList()

        fun <T : Comparable<T>> sortBy(fn: Quote.() -> T) {
            if (ascending) newList.sortBy(fn)
            else newList.sortByDescending(fn)
        }
        when (option) {
            "Ticker" -> sortBy(Quote::ticker)
            else -> sortBy(Quote::percentChange)
        }

        return newList
    }

    @MainThread
    private suspend fun doSort(
        oldList: List<Quote>,
        option: String = sortOption,
        isAscending: Boolean = sortIsAscending
    ) {
        val newList = withContext(Dispatchers.Default) {
            getSortedList(oldList, option, isAscending)
        }
        quotes.clear()
        quotes.addAll(newList)
    }

    @MainThread
    suspend fun sort() {
        Log.d("Zygos/WatchlistModel/sort", "$sortOption $sortIsAscending, last: $lastSortOption $lastSortIsAscending")
        if (lastSortOption == sortOption) {
            if (lastSortIsAscending != sortIsAscending) {
                quotes.reverse()
            }
        } else {
            doSort(quotes)
        }
        lastSortIsAscending = sortIsAscending
        lastSortOption = sortOption
    }

    @MainThread
    private fun getQuote(ticker: String, tdQuote: TdQuote?): Quote {
        return Quote(
            lazyKey = lastQuoteLazyKey++,
            ticker = ticker,
            price = tdQuote?.lastPrice ?: 0f,
            change = tdQuote?.netChange ?: 0f,
            percentChange = tdQuote?.netPercentChangeInDouble?.div(100f) ?: 0f,
        )
    }

    suspend fun loadTickers() {
        tickers.addAll(
            withContext(Dispatchers.IO) {
                parent.namesDao.getWatchlist().map { it.name }
            }
        )
    }

    @MainThread
    suspend fun loadQuotes(marketQuotes: Map<String, TdQuote>) {
        val option = sortOption
        val isAscending = sortIsAscending
        val newList = withContext(Dispatchers.Default) {
            val quoteList = tickers.map {
                getQuote(
                    ticker = it,
                    tdQuote = marketQuotes[it],
                )
            }
            getSortedList(quoteList, option, isAscending)
        }
        quotes.clear()
        quotes.addAll(newList)
    }

    // TODO this should update the market prices at some point
    fun add(ticker: String) {
        tickers.add(ticker)
        parent.viewModelScope.launch(Dispatchers.IO) {
            parent.namesDao.add(Names(type = "watchlist", name = ticker))
        }

        val newList = quotes.toMutableList()
        newList.add(
            getQuote(
                ticker = ticker,
                tdQuote = parent.market.stockQuotes[ticker],
            )
        )
        parent.viewModelScope.launch {
            doSort(newList)
        }
    }

    fun addAllFromHoldings() {
        val tickers = parent.getAllTickers()
        val newList = tickers.map {
            getQuote(
                ticker = it,
                tdQuote = parent.market.stockQuotes[it],
            )
        }
        parent.viewModelScope.launch {
            doSort(newList)
        }
        parent.viewModelScope.launch(Dispatchers.IO) {
            parent.namesDao.add(tickers.map { Names("watchlist", it) })
        }
    }

    @MainThread
    fun delete(ticker: String) {
        if (tickers.remove(ticker)) {
            quotes.removeIf { it.ticker == ticker }
            parent.viewModelScope.launch(Dispatchers.IO) {
                parent.namesDao.remove(Names("watchlist", ticker))
            }
        }
    }

    @MainThread
    fun contains(ticker: String): Boolean = tickers.contains(ticker)

    @MainThread
    fun toggle(ticker: String) {
        if (contains(ticker)) delete(ticker)
        else add(ticker)
    }
}