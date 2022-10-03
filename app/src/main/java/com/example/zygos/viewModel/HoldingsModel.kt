package com.example.zygos.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class HoldingsModel(private val parent: ZygosViewModel) {
    val positions = mutableStateListOf<Position>()

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

    // This happens asynchronously! Make sure that all other state is ok with the positions list being modified
    fun sort() {
        if (lastSortOption == sortOption) {
            if (lastSortIsAscending != sortIsAscending) {
                positions.reverse()
            }
        } else {
            if (sortIsAscending) {
                when (sortOption) {
                    "Ticker" -> positions.sortBy(Position::ticker)
                    else -> positions.sortBy(Position::value)
                }
            } else {
                when (sortOption) {
                    "Ticker" -> positions.sortByDescending(Position::ticker)
                    else -> positions.sortByDescending(Position::value)
                }
            }
        }
        lastSortIsAscending = sortIsAscending
        lastSortOption = sortOption
    }


}
