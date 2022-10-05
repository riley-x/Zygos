package com.example.zygos.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.zygos.data.LotPosition
import com.example.zygos.data.PositionType
import com.example.zygos.data.TickerPosition

class HoldingsModel(private val parent: ZygosViewModel) {

    val prices = mapOf<String, Long>(
        "MSFT" to 2000000,
        "AMD" to 1000000,
    )

    val longPositions = mutableStateListOf(
        Position(
            lot = LotPosition(
                account = "Robinhood",
                ticker = "MSFT",
                type = PositionType.STOCK,
                shares = 5,
                priceOpen = 2000000,
                costBasis = 10000000,
                realizedOpen = 200000,
                realizedClosed = 1000000,
            ),
            prices = prices,
            subPositions = listOf(Position(
                lot = LotPosition(
                    account = "Robinhood",
                    ticker = "MSFT",
                    type = PositionType.CALL_LONG,
                    shares = 100,
                    costBasis = 50000000,
                    expiration = "10/10/23",
                    strike = "125"
                ),
                prices = prices,
            ))
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
