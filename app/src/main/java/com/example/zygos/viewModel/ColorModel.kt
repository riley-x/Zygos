package com.example.zygos.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.viewModelScope
import com.example.zygos.data.database.ColorSettings
import com.example.zygos.ui.theme.defaultTickerColors
import com.example.zygos.ui.theme.randomColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ColorModel(private val parent: ZygosViewModel) {
    val tickers = SnapshotStateMap<String, Color>()
    var currentEditTicker by mutableStateOf("")
    fun getCurrentEditColor() = tickers.getOrDefault(currentEditTicker, Color.White)
    fun saveEditColor(color: Color) {
        tickers[currentEditTicker] = color
        parent.viewModelScope.launch(Dispatchers.IO) {
            parent.colorDao.add(ColorSettings(currentEditTicker, color.toArgb()))
        }
    }

    fun loadLaunched() {
        parent.viewModelScope.launch {
            val colors = withContext(Dispatchers.IO) {
                parent.colorDao.getMap()
            }
            colors.mapValuesTo(tickers) { Color(it.value) }
        }
    }

    fun insertDefaults(vals: Set<String>) {
        val newColors = mutableMapOf<String, Color>()
        vals.forEach {
            if (it !in tickers) {
                val color = defaultTickerColors.getOrDefault(it, randomColor())
                newColors[it] = color
            }
        }
        tickers.putAll(newColors)
        parent.viewModelScope.launch(Dispatchers.IO) {
            parent.colorDao.add(newColors)
        }
    }
}