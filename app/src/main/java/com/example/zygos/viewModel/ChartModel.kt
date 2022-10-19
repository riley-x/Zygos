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
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt


const val chartGraphYPad = 0.1f
const val chartGraphTickDivisionsX = 5
const val chartGraphTickDivisionsY = 5

enum class ChartRanges(displayName: String) {
    FIVE_DAYS("5d"),
    THREE_MONTHS("3m"),
    ONE_YEAR("1y"),
    FIVE_YEARS("5y"),
    TWENTY_YEARS("20y")
}


class ChartModel(private val parent: ZygosViewModel) {
    val ticker = mutableStateOf("")
    val graphState = mutableStateOf(TimeSeriesGraphState<OhlcNamed>())
    val range = mutableStateOf(chartRangeOptions.items.last())

    /** Cached time ranges **/
    private var ohlc5day = listOf<TdOhlc>() // 30-min
    private var ohlc1year = listOf<TdOhlc>() // daily
    private var ohlc20year = listOf<TdOhlc>() // monthly

    fun setTicker(newTicker: String) {
        if (ticker.value == newTicker) return
        ticker.value = newTicker
        ohlc5day = emptyList()
        ohlc1year = emptyList()
        ohlc20year = emptyList()
        parent.viewModelScope.launch {
            fetchOhlcs(newTicker)
            graphState.value = getGraphState()
        }
    }
    fun setRange(newRange: String) {
        range.value = newRange
        parent.viewModelScope.launch {
            graphState.value = getGraphState(newRange)
        }
    }

    private suspend fun fetchOhlcs(ticker: String) {
        val tdKey = parent.apiKeys[tdService.name]
        if (tdKey.isNullOrBlank()) return

        val fiveDay = parent.viewModelScope.async(Dispatchers.IO) {
            TdApi.tdService.getOhlc5Day(
                symbol = ticker,
                apiKey = tdKey,
            ).candles
        }
        val oneYear = parent.viewModelScope.async(Dispatchers.IO) {
            TdApi.tdService.getOhlc1Year(
                symbol = ticker,
                apiKey = tdKey,
            ).candles
        }
        val twentyYear = parent.viewModelScope.async(Dispatchers.IO) {
            TdApi.tdService.getOhlc20Year(
                symbol = ticker,
                apiKey = tdKey,
            ).candles
        }

        ohlc5day = fiveDay.await()
        ohlc1year = oneYear.await()
        ohlc20year = twentyYear.await()
    }


    private suspend fun getGraphState(
        newRange: String = range.value
    ): TimeSeriesGraphState<OhlcNamed> {
        val ohlc = when (newRange) {
            "5d" -> ohlc5day
            "3m" -> ohlc1year.takeLast((ohlc1year.size + 3) / 4) // round up
            "1y" -> ohlc1year
            "5y" -> ohlc20year.takeLast((ohlc20year.size + 3) / 4) // round up
            else -> ohlc20year
        }
        if (ohlc.isEmpty()) return TimeSeriesGraphState()

        return withContext(Dispatchers.Default) {
            /** Get the y min/max and xrange of the performance plot **/
            var minY = ohlc.last().low
            var maxY = ohlc.last().high
            for (x in ohlc) {
                if (x.low < minY) minY = x.low
                if (x.high > maxY) maxY = x.high
            }
            val pad = (if (maxY == minY) maxY else (maxY - minY)) * chartGraphYPad
            maxY += pad
            minY -= pad

            /** Get the axis positions **/
            val stepX = (ohlc.lastIndex.toFloat() / chartGraphTickDivisionsX).roundToInt()
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
                values = ohlc,
                minY = minY,
                maxY = maxY,
                ticksX = ticksX,
                ticksY = ticksY,
            )
        }
    }
}