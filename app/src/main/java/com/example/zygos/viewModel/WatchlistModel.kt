package com.example.zygos.viewModel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WatchlistModel(private val parent: ZygosViewModel) {
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
    var sortOption by mutableStateOf(watchlistSortOptions.items[0])
        private set
    var sortIsAscending by mutableStateOf(true)
        private set
    var displayOption by mutableStateOf(watchlistDisplayOptions.items[0])

    // Cached sort options to not re-sort if nothing was changed
    private var lastSortOption = ""
    private var lastSortIsAscending = true

    /** Called from composable onClick callbacks. Sets UI state only **/
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

        /** Do sort **/
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

    suspend fun sort() {
        Log.d("Zygos/WatchlistModel/sort", "$sortOption $sortIsAscending, last: $lastSortOption $lastSortIsAscending")
        if (lastSortOption == sortOption) {
            if (lastSortIsAscending != sortIsAscending) {
                watchlist.reverse()
            }
        } else {
            /** Sort - this blocks this routine, but the main UI routine continues since sort() is called
             * from a LaunchedEffect. **/
            val newList = withContext(Dispatchers.Default) {
                getSortedList(watchlist, sortOption, sortIsAscending)
            }

            /** Update. This happens in the main thread when called from LaunchedEffect with no dispatcher **/
            watchlist.clear()
            watchlist.addAll(newList)
        }
        lastSortIsAscending = sortIsAscending
        lastSortOption = sortOption
    }
}