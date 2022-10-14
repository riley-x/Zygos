package com.example.zygos.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.example.zygos.data.Position
import com.example.zygos.ui.holdings.HoldingsListSortOptions
import com.example.zygos.ui.holdings.holdingsListDisplayOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class PositionModel(private val parent: ZygosViewModel) {

    val list = mutableStateListOf<PricedPosition>()

    var isLoading by mutableStateOf(false)
    var displayOption by mutableStateOf(HoldingsListSortOptions.EQUITY)
    var sortOption by mutableStateOf(HoldingsListSortOptions.EQUITY)
    var sortIsAscending by mutableStateOf(false)

    // Called from composable onClick callbacks
    fun setSortAndDisplay(isCancel: Boolean, newDisplay: HoldingsListSortOptions, newSort: HoldingsListSortOptions, newIsAscending: Boolean) {
        if (isCancel) return
        parent.viewModelScope.launch {
            displayOption = newDisplay
            sort(newSort, newIsAscending)
        }
    }

    private fun getSortedList(oldList: List<PricedPosition>, option: HoldingsListSortOptions, ascending: Boolean): MutableList<PricedPosition> {
        if (oldList.isEmpty()) return mutableListOf()
        val newList = oldList.toMutableList()

        /** Cash position is always last **/
        var cash: PricedPosition? = null
        if (newList.last().ticker == "CASH") {
            cash = newList.removeLast()
        }

        /** Do sort **/
        fun <T: Comparable<T>> sortBy(fn: PricedPosition.() -> T) {
            if (ascending) newList.sortBy(fn)
            else newList.sortByDescending(fn)
        }
        when (option) {
            HoldingsListSortOptions.TICKER -> sortBy(PricedPosition::ticker)
            HoldingsListSortOptions.EQUITY -> sortBy(PricedPosition::equity)
            HoldingsListSortOptions.RETURNS -> sortBy(PricedPosition::returnsOpen)
            HoldingsListSortOptions.RETURNS_PERCENT -> sortBy(PricedPosition::returnsPercent)
            else -> Unit // TODO
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


    suspend fun sort(newSortOption: HoldingsListSortOptions, newSortIsAscending: Boolean, force: Boolean = false) {
        if (list.isEmpty()) return
        if (!force && newSortOption == sortOption && newSortIsAscending == sortIsAscending) return
        isLoading = true

        /** Sort - this blocks this routine, but the main UI routine continues since sort() is called
         * from a LaunchedEffect. **/
        val newList = withContext(Dispatchers.IO) {
            if (!force && sortOption == newSortOption) {
                getReversedList(list)
            } else {
                getSortedList(list, newSortOption, newSortIsAscending)
            }
        }

        /** Update. This happens in the main thread when called from LaunchedEffect with no dispatcher **/
        list.clear()
        list.addAll(newList)
        sortIsAscending = newSortIsAscending
        sortOption = newSortOption
        isLoading = false
    }

    fun loadLaunched(positions: List<Position>, prices: Map<String, Long>) {

        if (positions.isEmpty()) {
            list.clear()
            isLoading = false // this could be set from some other source
            return
        }

        isLoading = true // this can be called earlier too
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
            isLoading = false
        }
    }
}
