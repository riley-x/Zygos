package com.example.zygos.viewModel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.example.zygos.data.database.Transaction
import com.example.zygos.ui.components.allAccounts
import com.example.zygos.ui.components.noAccountMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** TransactionScreen
 * Need two separate lists of transactions: one for the latest in the analytics screen and one
 * for the all transactions screen, which can be sorted and filtered.
 */
class TransactionModel(private val parent: ZygosViewModel) {
    val latest = mutableStateListOf<Transaction>()
    val all = mutableStateListOf<Transaction>()
    val focused = mutableStateOf(Transaction()) // Current transaction that we're editing

    fun add(t: Transaction) {
        parent.viewModelScope.launch(Dispatchers.IO) {
            if (focused.value.transactionId > 0) { // we're currently editing a transaction
                parent.transactionDao.update(t)
                // TODO need to update lots
            } else {
                com.example.zygos.data.addTransaction(t, parent.transactionDao, parent.lotDao)
            }
            loadAccount(parent.currentAccount) // refresh transaction state lists
        }
    }
    fun clearFocus() {
        focused.value = Transaction()
    }
    fun setFocus(t: Transaction) {
        focused.value = t
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
}