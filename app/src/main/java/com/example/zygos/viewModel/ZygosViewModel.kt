package com.example.zygos.viewModel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import com.example.zygos.data.Position
import com.example.zygos.ui.holdings.holdingsListDisplayOptions
import com.example.zygos.ui.holdings.holdingsListSortOptions

class ZygosViewModel : ViewModel() {
    private val _accounts = mutableStateListOf<String>("Robinhood", "Arista", "TD Ameritrade", "Alhena", "All Accounts")
    val accounts: List<String> = _accounts

    var currentAccount by mutableStateOf(accounts[0])
        private set

    fun setAccount(account: String) {
        if (currentAccount == account) return

        currentAccount = account
    }

    /** Holdings **/
    private val _positions = mutableStateListOf<Position>(
        Position("p1", 0.2f, Color(0xFF004940)),
        Position("p2", 0.3f, Color(0xFF005D57)),
        Position("p3", 0.4f, Color(0xFF04B97F)),
        Position("p4", 0.1f, Color(0xFF37EFBA)),
        Position("p1", 0.2f, Color(0xFF004940)),
        Position("p2", 0.3f, Color(0xFF005D57)),
        Position("p3", 0.4f, Color(0xFF04B97F)),
        Position("p4", 0.1f, Color(0xFF37EFBA))
    )
    val positions: List<Position> = _positions

    // These variables are merely the ui state of the options selection menu
    // The actual sorting is called in sortHoldingsList() via a callback when
    // the menu is hidden.
    var holdingsSortOption by mutableStateOf(holdingsListSortOptions[0])
        private set
    var holdingsSortIsAscending by mutableStateOf(true)
        private set
    var holdingsDisplayOption by mutableStateOf(holdingsListDisplayOptions[0])

    // Cached sort options to not re-sort if nothing was changed
    private var lastSortOption = ""
    private var lastSortIsAscending = true

    // Called from composable onClick callbacks
    fun setHoldingsSortMethod(opt: String) {
        if (holdingsSortOption == opt) holdingsSortIsAscending = !holdingsSortIsAscending
        else holdingsSortOption = opt
    }

    fun sortHoldingsList() {
        if (lastSortOption == holdingsSortOption) {
            if (lastSortIsAscending != holdingsSortIsAscending) {
                _positions.reverse()
            }
        } else {
            if (holdingsSortIsAscending) {
                when (holdingsSortOption) {
                    "Ticker" -> _positions.sortBy(Position::ticker)
                    else -> _positions.sortBy(Position::value)
                }
            } else {
                when (holdingsSortOption) {
                    "Ticker" -> _positions.sortByDescending(Position::ticker)
                    else -> _positions.sortByDescending(Position::value)
                }
            }
        }
        lastSortIsAscending = holdingsSortIsAscending
        lastSortOption = holdingsSortOption
    }

}