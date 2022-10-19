package com.example.zygos.network

import androidx.compose.runtime.Immutable
import com.example.zygos.data.fromTimestamp
import com.example.zygos.ui.components.formatDateInt
import com.example.zygos.viewModel.OhlcNamed


data class TdPriceHistory(
    val candles: List<TdOhlc>
)

@Immutable
data class TdOhlc (
    override val open: Float,
    override val high: Float,
    override val low: Float,
    override val close: Float,
    val volume: Long,
    val datetime: Long,
) : OhlcNamed {
    override val name = formatDateInt(fromTimestamp(datetime))
}