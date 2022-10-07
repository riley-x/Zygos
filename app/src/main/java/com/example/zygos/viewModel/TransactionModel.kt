package com.example.zygos.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.example.zygos.data.addTransaction
import com.example.zygos.data.database.Transaction
import com.example.zygos.data.database.TransactionType
import com.example.zygos.ui.components.allAccounts
import com.example.zygos.ui.components.noAccountMessage
import kotlinx.coroutines.*

/** TransactionScreen
 * Need two separate lists of transactions: one for the analytics screen, which always shows the
 * latest transactions for a given account, and one for the all transactions screen, which can be
 * sorted and filtered.
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
    var sortIsAscending by mutableStateOf(false)
        private set
    var currentFilterTicker by mutableStateOf("")
        private set
    var currentFilterType by mutableStateOf(TransactionType.NONE)
        private set


    // Called from composable onClick callbacks. This only adjusts the UI state, not the actual sort
    fun setSortMethod(opt: String) {
        if (sortOption == opt) sortIsAscending = !sortIsAscending
        else sortOption = opt
    }


    /** Database Functions **/
    private fun addToDatabase(t: Transaction) {
        if (t.transactionId > 0) { // we're currently editing a transaction
            parent.transactionDao.update(t)
            // TODO recalculate all lots
        } else {
            addTransaction(t, parent.transactionDao, parent.lotDao)
        }
    }

    fun add(t: Transaction) {
        parent.viewModelScope.launch {
            withContext(Dispatchers.IO) {
                addToDatabase(t)
            }
            // Need to refresh everything, including transaction state lists and holdings
            parent.loadAccount(parent.currentAccount)
        }
    }

    private fun getLatest(account: String): List<Transaction> {
        return if (account != allAccounts && account != noAccountMessage) {
            parent.transactionDao.getLast(account);
        } else {
            parent.transactionDao.getLast();
        }
    }

    /** This function doesn't block, and merely launches the loading of the transaction lists **/
    fun loadLaunched(account: String): List<Job> {
        /** The launches have no dispatcher, so they run in the main UI thread (no race conditions
         * on update of the SnapshotStateLists). However, they are coroutines so the main coroutine
         * (i.e. the UI loop) can continue without being blocked. The launch allow the addAlls
         * to happen as they arrive.
         */
        val job1 = parent.viewModelScope.launch {
            val newLatest = withContext(Dispatchers.IO) {
                getLatest(account)
            }
            latest.clear()
            latest.addAll(newLatest)
        }
        val job2 = parent.viewModelScope.launch {
            val newAll = withContext(Dispatchers.IO) {
                getAllWithFilter("", TransactionType.NONE, account)
            }
            all.clear()
            all.addAll(newAll)
        }
        return listOf(job1, job2)
    }

    private fun getAllWithFilter(ticker: String, type: TransactionType, account: String): List<Transaction> {
        return parent.transactionDao.get(
            account = account,
            ticker = ticker,
            type = type,
            sort = sortOption.lowercase(),
            ascending = sortIsAscending,
        )
    }

    fun filterLaunch(ticker: String, type: TransactionType) {
        parent.viewModelScope.launch {
            val newAll = withContext(Dispatchers.IO) {
                getAllWithFilter(ticker, type, parent.currentAccount)
            }
            all.clear()
            all.addAll(newAll)
            currentFilterTicker = ticker
            currentFilterType = type
        }
    }
}