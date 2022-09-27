package com.example.zygos.ui.chart

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.zygos.viewModel.Position
import com.example.zygos.ui.components.LogCompositions
import com.example.zygos.ui.components.TimeSeriesGraph
import com.example.zygos.ui.components.candlestickGraph
import com.example.zygos.ui.components.recomposeHighlighter
import com.example.zygos.ui.theme.ZygosTheme
import com.example.zygos.viewModel.Ohlc
import com.example.zygos.viewModel.TestViewModel


@Composable
fun ChartScreen(
    data: SnapshotStateList<Ohlc>,
    ticksY: SnapshotStateList<Float>,
    ticksX: SnapshotStateList<Int>, // index into accountPerformance
    ticker: State<String>,
) {
    LogCompositions("Zygos", "ChartScreen")

    // TODO: Replace account bar with a ticker selector
    // I think vertical only like Robinhood is good
    // Horizontal doesn't look that nice on narrow phones

    Surface(
        modifier = Modifier
            .recomposeHighlighter()
            .fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            TimeSeriesGraph(
                grapher = candlestickGraph(),
                values = data,
                ticksY = ticksY,
                ticksX = ticksX,
                minX = -1f,
                maxX = data.size.toFloat(),
                minY = 0f,
                maxY = 25f,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
                    .height(300.dp)
            )
        }
    }
}


@Preview(
    widthDp = 360,
    heightDp = 740,
    showBackground = true,
)
@Composable
fun PreviewChartScreen() {
    val viewModel = viewModel<TestViewModel>()
    ZygosTheme {
        ChartScreen(
            ticker = viewModel.chartTicker,
            data = viewModel.ohlc,
            ticksX = viewModel.accountPerformanceTicksX,
            ticksY = viewModel.accountPerformanceTicksY,
        )
    }
}