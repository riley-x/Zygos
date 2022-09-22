package com.example.zygos.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import com.example.zygos.data.Position

class ZygosViewModel : ViewModel() {
    private val _accounts = mutableStateListOf<String>("Robinhood", "Arista", "TD Ameritrade", "Alhena")
    val accounts: List<String> = _accounts

    var currentAccount by mutableStateOf(accounts[0])
        private set

    private val _positions = mutableStateListOf<Position>(
        Position("p1", 0.2f, Color(0xFF004940)),
        Position("p2", 0.3f, Color(0xFF005D57)),
        Position("p3", 0.4f, Color(0xFF04B97F)),
        Position("p4", 0.1f, Color(0xFF37EFBA))
    )
    val positions: List<Position> = _positions

    fun setAccount(account: String) {
        if (currentAccount == account) return

        currentAccount = account
    }
}