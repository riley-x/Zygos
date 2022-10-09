package com.example.zygos.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PositionModel(private val parent: ZygosViewModel) {

    val longPositions = mutableStateListOf<PricedPosition>()
    val shortPositions = mutableStateListOf<PricedPosition>()

    var longPositionsAreLoading by mutableStateOf(false)
    var shortPositionsAreLoading by mutableStateOf(false)


    // These variables are merely the ui state of the options selection menu.
    // The actual sorting is called in sort() via a callback when the menu is hidden.
    var sortOption by mutableStateOf(holdingsListSortOptions.items[0])
        private set
    var sortIsAscending by mutableStateOf(true)
        private set
    var displayOption by mutableStateOf(holdingsListDisplayOptions.items[0])

    // Cached sort options to not re-sort if nothing was changed
    private var lastSortOption = ""
    private var lastSortIsAscending = true

    // Called from composable onClick callbacks
    fun setSortMethod(opt: String) {
        if (sortOption == opt) sortIsAscending = !sortIsAscending
        else sortOption = opt
    }

    private fun getSortedList(force: Boolean): List<PricedPosition> {
        val list = longPositions.toMutableList()

        /** Cash position is always last **/
        var cash: PricedPosition? = null
        if (list.last().ticker == "CASH") {
            cash = list.removeLast()
        }

        /** Reverse **/
        if (!force && lastSortOption == sortOption) {
            list.reverse()
        }
        /** Sort **/
        else {
            if (sortIsAscending) {
                when (sortOption) {
                    "Ticker" -> list.sortBy(PricedPosition::ticker)
                    else -> list.sortBy(PricedPosition::equity)
                }
            } else {
                when (sortOption) {
                    "Ticker" -> list.sortByDescending(PricedPosition::ticker)
                    else -> list.sortByDescending(PricedPosition::equity)
                }
            }
        }

        /** Add back cash **/
        if (cash != null) list.add(cash)
        return list
    }

    suspend fun sort(force: Boolean = false) {
        if (longPositions.isEmpty()) return
        if (!force && sortOption == lastSortOption && sortIsAscending == lastSortIsAscending) return
//        longPositionsAreLoading = true
//        shortPositionsAreLoading = true

        /** This blocks this routine, but the main UI routine continues since sort() is called
         * from a LaunchedEffect. **/
        val list = withContext(Dispatchers.IO) {
            getSortedList(force)
        }

        /** Update. This happens in the main thread when called from LaunchedEffect with no dispatcher **/
        longPositions.clear()
        longPositions.addAll(list)

        lastSortIsAscending = sortIsAscending
        lastSortOption = sortOption
//        longPositionsAreLoading = false
//        shortPositionsAreLoading = false
    }


}
