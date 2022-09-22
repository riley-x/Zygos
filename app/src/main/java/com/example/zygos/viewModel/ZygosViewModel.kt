package com.example.zygos.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class ZygosViewModel : ViewModel() {
    private val _accounts = mutableStateListOf<String>("Robinhood", "Arista", "TD Ameritrade", "Alhena")
    val accounts: List<String> = _accounts

    var currentAccount by mutableStateOf(accounts[0])
        private set

    fun setAccount(account: String) {
        if (currentAccount != account) {
            currentAccount = account
        }
    }
}