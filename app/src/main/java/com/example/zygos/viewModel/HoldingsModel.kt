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
        "MSFT 10/10/23 125" to 600000,
        "AMD" to 1300000,
    )

    /** Holdings **/
    val longPositions = mutableStateListOf(
        Position.getLongPosition(TickerPosition(
            stock = LotPosition(
                account = "Robinhood",
                ticker = "MSFT",
                type = PositionType.STOCK,
                shares = 5,
                priceOpen = 2000000,
                costBasis = 10000000,
                realizedOpen = 200000,
                realizedClosed = 1000000,
            ),
            longOptions = listOf(
                LotPosition(
                    account = "Robinhood",
                    ticker = "MSFT",
                    type = PositionType.CALL_LONG,
                    shares = 100,
                    priceOpen = 500000,
                    costBasis = 50000000,
                    expiration = "10/10/23",
                    strike = "125"
                )
            )),
            prices = prices
        ),
        Position(lot =  LotPosition(
            account = "Robinhood",
            ticker = "AMD",
            type = PositionType.STOCK,
            shares = 10,
            priceOpen = 1000000,
            costBasis = 10000000,
            realizedOpen = 200000,
            realizedClosed = 0,
        ), prices = prices),
        Position(lot =  LotPosition(
            account = "Robinhood",
            ticker = "CASH",
            type = PositionType.CASH,
            costBasis = 20000000,
            realizedClosed = 670900,
        ), prices = prices),
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
        longPositionsAreLoading = true
        shortPositionsAreLoading = true

        /** Sort - This blocks this routine, but the main UI routine continues since sort() is
         * called from a LaunchedEffect. **/
        val list = parent.viewModelScope.async(Dispatchers.IO) {
            delay(3000)
            doSort()
        }.await()

        /** Update **/
        longPositions.clear()
        longPositions.addAll(list)
        lastSortIsAscending = sortIsAscending
        lastSortOption = sortOption
        longPositionsAreLoading = false
        shortPositionsAreLoading = false
    }
}
