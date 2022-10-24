package com.example.zygos.viewModel

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.example.zygos.data.*
import com.example.zygos.network.TdInstrument
import com.example.zygos.network.TdQuote
import com.example.zygos.ui.components.formatDateInt

/**
 * This file contains data classes that are passed into composables
 */

interface HasName {
    val name: String
}

interface HasValue {
    val value: Float
}

@Immutable
data class NamedValue(
    override val value: Float,
    override val name: String,
): HasName, HasValue


@Immutable
data class TimeSeries(
    override val value: Float,
    val date: Int,
): HasName, HasValue {
    override val name = formatDateInt(date)
}

@Immutable
data class OhlcNamed (
    val open: Float,
    val high: Float,
    val low: Float,
    val close: Float,
    override val name: String,
) : HasName


@Immutable
data class Quote (
    /** This is required since the ticker rows in the watchlist remember the swipeable state. If you
     * use the ticker as the key, and delete a row then add it back, it'll remember the swipe state
     * to be deleted still **/
    val lazyKey: Int,
    val ticker: String,
    val price: Float,
    val change: Float,
    val percentChange: Float,
)

fun List<Quote>.contains(ticker: String) = any { it.ticker == ticker }


@Immutable
data class Fundamental (
    val symbol: String = "",
    val high52: Float = 0f,
    val low52: Float = 0f,
    val dividendAmount: Float = 0f,
    val dividendYield: Float = 0f,
    val dividendDate: String = "",
    val peRatio: Float = 0f,
    val pcfRatio: Float = 0f,
    val netProfitMarginTTM: Float = 0f,
    val operatingMarginTTM: Float = 0f,
    val quickRatio: Float = 0f,
    val currentRatio: Float = 0f,
    val description: String = "",
) {
    companion object Factory {
        operator fun invoke(
            tdInstrument: TdInstrument,
        ): Fundamental {
            return Fundamental(
                symbol = tdInstrument.fundamental.symbol,
                high52 = tdInstrument.fundamental.high52,
                low52 = tdInstrument.fundamental.low52,
                dividendAmount = tdInstrument.fundamental.dividendAmount,
                dividendYield = tdInstrument.fundamental.dividendYield / 100f,
                dividendDate = tdInstrument.fundamental.dividendDate.substringBefore(" "),
                peRatio = tdInstrument.fundamental.peRatio,
                pcfRatio = tdInstrument.fundamental.pcfRatio,
                netProfitMarginTTM = tdInstrument.fundamental.netProfitMarginTTM,
                operatingMarginTTM = tdInstrument.fundamental.operatingMarginTTM,
                quickRatio = tdInstrument.fundamental.quickRatio,
                currentRatio = tdInstrument.fundamental.currentRatio,
                description = tdInstrument.description,
            )
        }
    }
}
