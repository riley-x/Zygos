package com.example.zygos.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.zygos.ui.theme.CandleGreen
import com.example.zygos.ui.theme.CandleRed
import com.example.zygos.ui.theme.ZygosTheme
import com.example.zygos.viewModel.Ohlc
import com.example.zygos.viewModel.TestViewModel
import kotlin.math.abs

@Composable
fun candlestickGraph(
    width: Float = 0.8f, // as fraction of x separation
    upColor: Color = CandleGreen,
    downColor: Color = CandleRed,
    lineColor: Color = MaterialTheme.colors.onSurface,
): TimeSeriesGrapher<Ohlc> {
    return fun(
        drawScope: DrawScope,
        values: List<Ohlc>,
        deltaX: Float,
        deltaY: Float,
        startY: Float,
        minX: Float,
        minY: Float,
    ) {
        val widthPx = width * deltaX
        for ((i, ohlc) in values.withIndex()) {
            val yHigh = startY + deltaY * (ohlc.high - minY)
            val yLow = startY + deltaY * (ohlc.low - minY)
            val yOpen = startY + deltaY * (ohlc.open - minY)
            val yClose = startY + deltaY * (ohlc.close - minY)
            val xCenter = deltaX * (i - minX)

            drawScope.drawLine(
                start = Offset(x = xCenter, y = yLow),
                end = Offset(x = xCenter, y = yHigh),
                color = lineColor,
            )

            if (ohlc.open != ohlc.close) {
                drawScope.drawRect(
                    color = if (ohlc.close > ohlc.open) upColor else downColor,
                    topLeft = Offset(xCenter - widthPx / 2f, minOf(yOpen, yClose)),
                    size = Size(widthPx, abs(yOpen - yClose)),
                )
            }
        }
    }
}


@Preview(
    widthDp = 360,
    heightDp = 400,
)
@Composable
fun TimeSeriesCandlestickGraphPreview() {
    val viewModel = viewModel<TestViewModel>()
    ZygosTheme {
        TimeSeriesGraph(
            grapher = candlestickGraph(),
            state = viewModel.chartState,
            modifier = Modifier.size(300.dp, 400.dp)
        )
    }
}