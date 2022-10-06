package com.example.zygos.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.example.zygos.data.LotPosition
import com.example.zygos.data.PositionType
import com.example.zygos.data.TickerPosition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay

class HoldingsModel(private val parent: ZygosViewModel) {

    var longPositionsAreLoading by mutableStateOf(false)
    var shortPositionsAreLoading by mutableStateOf(false)

    val prices = mapOf<String, Long>(
        "MSFT" to 2500000,
        "MSFT Call 20231010 1250000" to 600000, // $60
        "AMD" to 1300000, // $130
    )
    val lots = mutableListOf<LotPosition>(
        LotPosition(
            account = "Robinhood",
            ticker = "MSFT",
            type = PositionType.STOCK,
            shares = 5,
            priceOpen = 2000000,
            realizedOpen = 200000,
            realizedClosed = 1000000,
        ),
        LotPosition(
            account = "Robinhood",
            ticker = "MSFT",
            type = PositionType.CALL_LONG,
            shares = 100,
            priceOpen = 500000,
            expiration = 20231010,
            strike = 1250000,
        ),
        LotPosition(
            account = "Robinhood",
            ticker = "AMD",
            type = PositionType.STOCK,
            shares = 10,
            priceOpen = 1000000, // $100
            realizedOpen = 200000, // $20
            realizedClosed = 0,
        ),
        LotPosition(
            account = "Robinhood",
            ticker = "CASH",
            type = PositionType.CASH,
            shares = 20000000,
            priceOpen = -1,
            realizedClosed = 670900,
        ),
    )
    val longPositions = mutableStateListOf(
        Position(lot = lots[0] + lots[1], prices = prices),
        Position(lot = lots[2], prices = prices),
        Position(lot = lots[3], prices = prices),
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

    private fun doSort(): List<Position> {
        val list = longPositions.toMutableList()

        /** Cash position is always last **/
        var cash: Position? = null
        if (list.last().ticker == "CASH") {
            cash = list.removeLast()
        }

        /** Reverse **/
        if (lastSortOption == sortOption) {
            list.reverse()
        }
        /** Sort **/
        else {
            if (sortIsAscending) {
                when (sortOption) {
                    "Ticker" -> list.sortBy(Position::ticker)
                    else -> list.sortBy(Position::equity)
                }
            } else {
                when (sortOption) {
                    "Ticker" -> list.sortByDescending(Position::ticker)
                    else -> list.sortByDescending(Position::equity)
                }
            }
        }

        /** Add back cash **/
        if (cash != null) list.add(cash)
        return list
    }

    suspend fun sort() {
        /** Guards **/
        if (longPositions.isEmpty()) return
        if (sortOption == lastSortOption && sortIsAscending == lastSortIsAscending) return
//        longPositionsAreLoading = true
//        shortPositionsAreLoading = true

        /** Sort - This blocks this routine, but the main UI routine continues since sort() is
         * called from a LaunchedEffect. **/
        val list = parent.viewModelScope.async(Dispatchers.IO) {
            doSort()
        }.await()

        /** Update **/
        longPositions.clear()
        longPositions.addAll(list)
        lastSortIsAscending = sortIsAscending
        lastSortOption = sortOption
//        longPositionsAreLoading = false
//        shortPositionsAreLoading = false
    }
}
