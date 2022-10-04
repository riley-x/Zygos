package com.example.zygos.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.zygos.data.Position
import com.example.zygos.data.database.TransactionType

class HoldingsModel(private val parent: ZygosViewModel) {
    val longPositions = mutableStateListOf<Position>(
        Position(
            ticker = "MSFT",
            type = TransactionType.NONE,
            costBasis = 6000f,
            realizedOpen = 20f,
            realizedClosed = 100f,
            unrealized = 2500f,
            subPositions = listOf(
                Position(
                    ticker = "MSFT",
                    type = TransactionType.STOCK,
                    shares = 5,
                    costBasis = 1000f,
                    taxBasis = 1000f,
                    realizedOpen = 20f,
                    realizedClosed = 100f,
                    unrealized = 500f,
                ),
                Position(
                    ticker = "MSFT",
                    type = TransactionType.CALL_LONG,
                    shares = 100,
                    costBasis = 5000f,
                    unrealized = 2000f,
                    expiration = 20231010,
                    strike = 200f,
                )
            )
        ),
        Position(
            ticker = "AMD",
            shares = 10,
            costBasis = 1000f,
            taxBasis = 1000f,
            realizedOpen = 20f,
            realizedClosed = 0f,
            unrealized = -500f
        ),
        Position(
            ticker = "CASH",
            costBasis = 2000f,
            realizedClosed = 67.09f,
            equity = 2067.09f,
        ),
    )
    val shortPositions = mutableStateListOf<Position>()

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
        if (longPositions.isEmpty()) return

        /** Cash position is always last **/
        var cash: Position? = null
        if (longPositions.last().ticker == "CASH") {
            cash = longPositions.removeLast()
        }

        /** Reverse **/
        if (lastSortOption == sortOption) {
            if (lastSortIsAscending != sortIsAscending) {
                longPositions.reverse()
            }
        }
        /** Sort **/
        else {
            if (sortIsAscending) {
                when (sortOption) {
                    "Ticker" -> longPositions.sortBy(Position::ticker)
                    else -> longPositions.sortBy(Position::equity)
                }
            } else {
                when (sortOption) {
                    "Ticker" -> longPositions.sortByDescending(Position::ticker)
                    else -> longPositions.sortByDescending(Position::equity)
                }
            }
        }

        /** Add back cash **/
        if (cash != null) longPositions.add(cash)

        /** Update cached sort parameters **/
        lastSortIsAscending = sortIsAscending
        lastSortOption = sortOption
    }
}
