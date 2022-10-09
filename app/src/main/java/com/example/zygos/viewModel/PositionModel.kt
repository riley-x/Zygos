package com.example.zygos.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.example.zygos.data.Position
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class PositionModel(private val parent: ZygosViewModel) {

    val list = mutableStateListOf<PricedPosition>()

    var isLoading by mutableStateOf(false)
    var displayOption by mutableStateOf(holdingsListDisplayOptions.items[0])

    // These variables are merely the ui state of the options selection menu.
    // The actual sorting is called in sort() via a callback when the menu is hidden.
    var sortOption by mutableStateOf("Equity")
        private set
    var sortIsAscending by mutableStateOf(false)
        private set

    // Cached sort options to not re-sort if nothing was changed
    private var lastSortOption = ""
    private var lastSortIsAscending = true

    // Called from composable onClick callbacks
    fun setSortMethod(opt: String) {
        if (sortOption == opt) sortIsAscending = !sortIsAscending
        else sortOption = opt
    }

    private fun getSortedList(oldList: List<PricedPosition>, option: String, ascending: Boolean): MutableList<PricedPosition> {
        if (oldList.isEmpty()) return mutableListOf()
        val newList = oldList.toMutableList()

        /** Cash position is always last **/
        var cash: PricedPosition? = null
        if (newList.last().ticker == "CASH") {
            cash = newList.removeLast()
        }

        fun <T: Comparable<T>> sortBy(fn: PricedPosition.() -> T) {
            if (ascending) newList.sortBy(fn)
            else newList.sortByDescending(fn)
        }
        when (option) {
            "Ticker" -> sortBy(PricedPosition::ticker)
            "Equity" -> sortBy(PricedPosition::equity)
            "Returns" -> sortBy(PricedPosition::returnsOpen)
            "% Change" -> sortBy(PricedPosition::returnsPercent)
        }

        /** Add back cash **/
        if (cash != null) newList.add(cash)
        return newList
    }

    private fun getReversedList(oldList: List<PricedPosition>): MutableList<PricedPosition> {
        if (oldList.isEmpty()) return mutableListOf()
        val newList = oldList.toMutableList()

        /** Cash position is always last **/
        var cash: PricedPosition? = null
        if (newList.last().ticker == "CASH") {
            cash = newList.removeLast()
        }

        newList.reverse()

        /** Add back cash **/
        if (cash != null) newList.add(cash)
        return newList
    }


    suspend fun sort(force: Boolean = false) {
        if (list.isEmpty()) return
        if (!force && sortOption == lastSortOption && sortIsAscending == lastSortIsAscending) return
//        isLoading = true

        /** Sort - this blocks this routine, but the main UI routine continues since sort() is called
         * from a LaunchedEffect. **/
        val newList = withContext(Dispatchers.IO) {
            if (!force && lastSortOption == sortOption) {
                getReversedList(list)
            } else {
                getSortedList(list, sortOption, sortIsAscending)
            }
        }

        /** Update. This happens in the main thread when called from LaunchedEffect with no dispatcher **/
        list.clear()
        list.addAll(newList)
        lastSortIsAscending = sortIsAscending
        lastSortOption = sortOption
//        isLoading = false
    }

    fun loadLaunched(positions: List<Position>, prices: Map<String, Long>) {
        if (positions.isEmpty()) {
            list.clear()
            return
        }

        parent.viewModelScope.launch {
            val newList = withContext(Dispatchers.IO) {
                val newList = mutableListOf<PricedPosition>()
                positions.forEach {
                    newList.add(PricedPosition(lot = it, prices = prices))
                }
                getSortedList(newList, sortOption, sortIsAscending)
            }

            /** These happen in the main thread **/
            list.clear()
            list.addAll(newList)
        }
    }


}
