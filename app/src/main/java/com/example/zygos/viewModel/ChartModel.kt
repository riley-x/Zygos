package com.example.zygos.viewModel

import android.icu.util.Calendar
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.core.math.MathUtils.clamp
import androidx.lifecycle.viewModelScope
import com.example.zygos.network.TdApi
import com.example.zygos.network.TdOhlc
import com.example.zygos.network.tdService
import com.example.zygos.ui.components.*
import com.example.zygos.ui.graphing.TimeSeriesGraphState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt


const val chartGraphYPad = 0.1f
const val chartGraphTickDivisionsX = 6
const val chartGraphTickDivisionsY = 8

val chartRangeValues = ImmutableList(listOf(
    TimeRange.FIVE_DAYS,
    TimeRange.THREE_MONTHS,
    TimeRange.ONE_YEAR,
    TimeRange.FIVE_YEARS,
    TimeRange.TWENTY_YEARS,
))


class ChartModel(private val parent: ZygosViewModel) {
    val ticker = mutableStateOf("")
    val graphState = mutableStateOf(TimeSeriesGraphState<OhlcNamed>())
    val range = mutableStateOf(TimeRange.ONE_YEAR)

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
    fun setRange(newRange: TimeRange) {
        range.value = newRange
        parent.viewModelScope.launch {
            graphState.value = getGraphState(newRange)
        }
    }

    private suspend fun fetchOhlcs(ticker: String) {
        val tdKey = parent.apiKeys[tdService.name]
        if (tdKey.isNullOrBlank()) return

        val fiveDay = parent.viewModelScope.async(Dispatchers.IO) {
            try {
                TdApi.getOhlc5Day(
                    symbol = ticker,
                    apiKey = tdKey,
                )
            } catch (e: Exception) {
                Log.w("Zygos/ChartModel", e.stackTraceToString())
                emptyList()
            }
        }
        val oneYear = parent.viewModelScope.async(Dispatchers.IO) {
            try {
                TdApi.getOhlc1Year(
                    symbol = ticker,
                    apiKey = tdKey,
                )
            } catch (e: Exception) {
                Log.w("Zygos/ChartModel", e.stackTraceToString())
                emptyList()
            }
        }
        val twentyYear = parent.viewModelScope.async(Dispatchers.IO) {
            try {
                TdApi.getOhlc20Year(
                    symbol = ticker,
                    apiKey = tdKey,
                )
            } catch (e: Exception) {
                Log.w("Zygos/ChartModel", e.stackTraceToString())
                emptyList()
            }
        }

        ohlc5day = fiveDay.await()
        ohlc1year = oneYear.await()
        ohlc20year = twentyYear.await()
    }


    private fun getTimeName(timestamp: Long, range: TimeRange): String =
        when (range) {
            TimeRange.FIVE_DAYS -> formatTimeDayOfWeek(timestamp)
            TimeRange.THREE_MONTHS, TimeRange.ONE_YEAR -> formatDateNoYear(timestamp)
            else -> formatDateNoDay(timestamp)
        }

    private fun List<TdOhlc>.named(range: TimeRange): List<OhlcNamed> {
        return map {
            OhlcNamed(
                open = it.open,
                high = it.high,
                low = it.low,
                close = it.close,
                name = getTimeName(it.datetime, range),
            )
        }
    }


    private fun autoXTicks(ohlc: List<TdOhlc>, range: TimeRange): List<NamedValue> {
        /** Special case for 3m, since there's no really good separator to use as labels **/
        if (range == TimeRange.THREE_MONTHS) return IntRange(1, chartGraphTickDivisionsX - 1).map {
            val stepX = (ohlc.lastIndex.toFloat() / chartGraphTickDivisionsX).roundToInt()
            val index = clamp(stepX * it, 0, ohlc.lastIndex)
            NamedValue(
                value = index.toFloat(),
                name = getTimeName(ohlc[index].datetime, range)
            )
        }

        /** For the rest, look for when the time changes a date/month/year **/
        val separatorField = when (range) {
            TimeRange.FIVE_DAYS -> Calendar.DATE
            TimeRange.ONE_YEAR -> Calendar.MONTH
            else -> Calendar.YEAR
        }
        val cal = Calendar.getInstance()
        cal.timeInMillis = ohlc[0].datetime
        var lastFieldValue = cal.get(separatorField)

        val changeIndexes = mutableListOf<Int>()
        for (i in 1..ohlc.lastIndex) {
            cal.timeInMillis = ohlc[i].datetime
            val currentFieldValue = cal.get(separatorField)
            if (currentFieldValue != lastFieldValue) changeIndexes.add(i)
            lastFieldValue = currentFieldValue
        }

        /** Limit to chartGraphTickDivisionsX **/
        val indexes = if (changeIndexes.size >= chartGraphTickDivisionsX) {
            val stepX = (changeIndexes.lastIndex.toFloat() / chartGraphTickDivisionsX).roundToInt()
            val offset = (changeIndexes.lastIndex % stepX + 1) / 2
            changeIndexes.slice(offset..changeIndexes.lastIndex step stepX)
        } else {
            changeIndexes
        }

        return indexes.map {
            NamedValue(
                value = it.toFloat(),
                name = when (range) {
                    TimeRange.FIVE_DAYS -> formatDayOfWeekOnly(ohlc[it].datetime)
                    TimeRange.ONE_YEAR -> formatMonthOnly(ohlc[it].datetime)
                    else -> formatYearOnly(ohlc[it].datetime)
                }
            )
        }
    }




    private suspend fun getGraphState(
        newRange: TimeRange = range.value
    ): TimeSeriesGraphState<OhlcNamed> {
        val ohlc = when (newRange) {
            TimeRange.FIVE_DAYS -> ohlc5day
            TimeRange.THREE_MONTHS -> ohlc1year.takeLast((ohlc1year.size + 3) / 4) // round up
            TimeRange.ONE_YEAR -> ohlc1year
            TimeRange.FIVE_YEARS -> ohlc20year.takeLast((ohlc20year.size + 3) / 4) // round up
            TimeRange.TWENTY_YEARS -> ohlc20year
            else -> throw UnsupportedOperationException("ChartModel::getGraphState() time range: $newRange")
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
            val ticksX = autoXTicks(ohlc, newRange)
            Log.d("Zygos/ChartModel", "ticksX: $ticksX")
            val ticksY = autoYTicks(
                minY,
                maxY,
                chartGraphTickDivisionsY,
                chartGraphTickDivisionsY - 1
            )

            TimeSeriesGraphState(
                values = ohlc.named(newRange),
                minY = minY,
                maxY = maxY,
                ticksX = ticksX,
                ticksY = ticksY,
            )
        }
    }
}