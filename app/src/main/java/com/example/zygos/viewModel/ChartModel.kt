package com.example.zygos.viewModel

import android.icu.util.Calendar
import androidx.compose.runtime.mutableStateOf
import androidx.core.math.MathUtils
import androidx.lifecycle.viewModelScope
import com.example.zygos.data.fromTimestamp
import com.example.zygos.data.toIntDate
import com.example.zygos.network.TdApi
import com.example.zygos.network.TdOhlc
import com.example.zygos.network.tdService
import com.example.zygos.ui.graphing.TimeSeriesGraphState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt


const val chartGraphYPad = 0.1f
const val chartGraphTickDivisionsX = 5
const val chartGraphTickDivisionsY = 5

class ChartModel(private val parent: ZygosViewModel) {
    val ticker = mutableStateOf("")
    val graphState = mutableStateOf(TimeSeriesGraphState<OhlcNamed>())
    val range = mutableStateOf(chartRangeOptions.items.last())

    private var ohlc = listOf<TdOhlc>() // Full data, graph.values are sliced from this

    fun setTicker(newTicker: String) {
        ticker.value = newTicker
        parent.viewModelScope.launch {
            ohlc = fetchOhlc(newTicker)
            graphState.value = getGraphState()
        }
    }
    fun setRange(newRange: String) {
        range.value = newRange
        parent.viewModelScope.launch {
            graphState.value = getGraphState(newRange)
        }
    }


    private suspend fun fetchOhlc(ticker: String): List<TdOhlc> {
        val tdKey = parent.apiKeys[tdService.name]
        if (tdKey.isNullOrBlank()) return emptyList()

        val cal = Calendar.getInstance()
        val currentTime = cal.timeInMillis
        cal.add(Calendar.YEAR, -5)
        val startTime = cal.timeInMillis

        return withContext(Dispatchers.IO) {
            TdApi.tdService.getOhlc(
                symbol = ticker,
                apiKey = tdKey,
                startDate = startTime,
                endDate = currentTime,
            ).candles
        }
    }


    private suspend fun getGraphState(
        newRange: String = range.value
    ): TimeSeriesGraphState<OhlcNamed> {
        if (ohlc.isEmpty()) return TimeSeriesGraphState()

        return withContext(Dispatchers.Default) {
            /** Get the y min/max and xrange of the performance plot **/
            var minY = ohlc.last().low
            var maxY = ohlc.last().high
            var startIndex = 0
            val startDate = if (newRange == "5y") 0 else {
                val cal = Calendar.getInstance()
                when (newRange) {
                    "1m" -> cal.add(Calendar.MONTH, -1)
                    "3m" -> cal.add(Calendar.MONTH, -3)
                    "1y" -> cal.add(Calendar.YEAR, -1)
                }
                cal.toIntDate()
            }
            for (i in ohlc.lastIndex downTo 0) {
                val x = ohlc[i]
                if (fromTimestamp(x.datetime) < startDate) {
                    startIndex = i + 1
                    break
                }
                if (x.low < minY) minY = x.low
                if (x.high > maxY) maxY = x.high
            }
            val pad = (if (maxY == minY) maxY else (maxY - minY)) * chartGraphYPad
            maxY += pad
            minY -= pad

            /** Get the axis positions **/
            val stepX = ((ohlc.lastIndex - startIndex).toFloat() / chartGraphTickDivisionsX).roundToInt()
            val ticksX =
                IntRange(1, chartGraphTickDivisionsX - 1).map {
                    MathUtils.clamp(stepX * it, 0, ohlc.lastIndex)
                }
            val ticksY = autoYTicks(
                minY,
                maxY,
                chartGraphTickDivisionsY,
                chartGraphTickDivisionsY - 1
            )

            TimeSeriesGraphState(
                values = ohlc.slice(startIndex..ohlc.lastIndex),
                minY = minY,
                maxY = maxY,
                ticksX = ticksX,
                ticksY = ticksY,
            )
        }
    }
}