package com.example.zygos.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.example.zygos.data.database.Transaction
import com.example.zygos.data.database.TransactionType
import com.example.zygos.ui.components.allAccounts
import com.example.zygos.ui.components.noAccountMessage
import com.example.zygos.ui.holdings.holdingsListDisplayOptions
import com.example.zygos.ui.holdings.holdingsListSortOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** TransactionScreen
 * Need two separate lists of transactions: one for the latest in the analytics screen and one
 * for the all transactions screen, which can be sorted and filtered.
 */
class TransactionModel(private val parent: ZygosViewModel) {

    /** Transaction objects **/
    val latest = mutableStateListOf<Transaction>()
    val all = mutableStateListOf<Transaction>()
    val focused = mutableStateOf(Transaction()) // Current transaction that we're editing
    fun clearFocus() {
        focused.value = Transaction()
    }
    fun setFocus(t: Transaction) {
        focused.value = t
    }

    /** Sorting
     * These variables are merely the ui state of the options selection menu. The actual sorting is
     * called in sortAll() via a callback when the menu is hidden.
     */
    var sortOption by mutableStateOf(transactionSortOptions.items[0])
        private set
    var sortIsAscending by mutableStateOf(true)
        private set
    val filterTicker = mutableStateOf("") // these are passed directly to the text fields!
    val filterType = mutableStateOf(TransactionType.NONE)
    fun onFilterTickerChange(ticker: String) { filterTicker.value = ticker }
    fun onFilterTypeChange(type: TransactionType) { filterType.value = type }

    // Cached sort options to not re-sort if nothing was changed
    private var lastSortOption = ""
    private var lastSortIsAscending = true

    // Called from composable onClick callbacks. This only adjusts the UI state, not the actual sort
    fun setSortMethod(opt: String) {
        if (sortOption == opt) sortIsAscending = !sortIsAscending
        else sortOption = opt
    }


    /** Database Functions **/
    fun add(t: Transaction) {
        parent.viewModelScope.launch(Dispatchers.IO) {
            if (t.transactionId > 0) { // we're currently editing a transaction
                parent.transactionDao.update(t)
                // TODO need to update lots
            } else {
                com.example.zygos.data.addTransaction(t, parent.transactionDao, parent.lotDao)
            }
            loadAccount(parent.currentAccount) // refresh transaction state lists
        }
    }

    /**
     * MAKE SURE TO CALL FROM A COROUTINE!
     */
    fun loadAccount(currentAccount: String) {
        latest.clear()
        if (currentAccount != allAccounts && currentAccount != noAccountMessage) {
            latest.addAll(parent.transactionDao.getLast(currentAccount))
        } else {
            latest.addAll(parent.transactionDao.getLast())
        }
        // TODO transactionsAll, based on current sort/filter
    }

    fun sortAll() {

    }
}