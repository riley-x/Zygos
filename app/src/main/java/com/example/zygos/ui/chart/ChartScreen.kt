package com.example.zygos.ui.chart

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.zygos.ui.components.*
import com.example.zygos.ui.theme.ZygosTheme
import com.example.zygos.viewModel.*


@Composable
fun ChartScreen(
    ticker: State<String>,
    data: SnapshotStateList<Ohlc>,
    ticksY: SnapshotStateList<Float>,
    ticksX: SnapshotStateList<Int>, // index into accountPerformance
    chartRange: State<String>, // must pass state here for button group to calculate derivedStateOf
    modifier: Modifier = Modifier,
    onChartRangeSelected: (String) -> Unit = { },
) {
    LogCompositions("Zygos", "ChartScreen")

    // TODO: Replace account bar with a ticker selector
    // I think vertical only like Robinhood is good
    // Horizontal doesn't look that nice on narrow phones

    Column(
        modifier = modifier
            .fillMaxWidth(),
    ) {
        // Ticker selection

        Surface(
            modifier = Modifier
                .recomposeHighlighter()
                .fillMaxSize(),
            color = MaterialTheme.colors.background
        ) {
            var hoverTime by remember { mutableStateOf("") }
            var hoverValues by remember { mutableStateOf("") }

            fun onGraphHover(isHover: Boolean, x: Int, y: Float) {
                if (isHover && x >= 0 && x < data.size) {
                    hoverTime = data[x].name
                    val open = formatDollarNoSymbol(data[x].open)
                    val close = formatDollarNoSymbol(data[x].open)
                    val high = formatDollarNoSymbol(data[x].open)
                    val low = formatDollarNoSymbol(data[x].open)
                    val maxLength = maxOf(open.length, close.length, high.length, low.length)
                    hoverValues = "O: " + open.padStart(maxLength) +
                            "  H: " + high.padStart(maxLength) +
                            "\nC: " + close.padStart(maxLength) +
                            "  L: " + low.padStart(maxLength)
                } else {
                    hoverTime = ""
                    hoverValues = ""
                }
            }

            LazyColumn {
                item("graph_hover") {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .height(40.dp)
                            .padding(bottom = 2.dp)
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = hoverTime,
                            style = MaterialTheme.typography.subtitle2,
                        )
                        Text(
                            text = hoverValues,
                            style = MaterialTheme.typography.subtitle2,
                        )
                    }
                }

                item("graph") {
                    val grapher = candlestickGraph()
                    TimeSeriesGraph(
                        grapher = grapher,
                        values = data,
                        ticksY = ticksY,
                        ticksX = ticksX,
                        minX = -1f,
                        maxX = data.size.toFloat(),
                        minY = 0f,
                        maxY = 25f,
                        onHover = ::onGraphHover,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp)
                            .height(300.dp)
                            .clipToBounds()
                    )
                }

                item("divider1") {
                    Divider(
                        color = MaterialTheme.colors.onBackground.copy(alpha = 0.2f),
                        thickness = 1.dp,
                        modifier = Modifier
                            .padding(start = 12.dp, end = 12.dp, top = 10.dp, bottom = 2.dp)
                    )
                }

                item("graph_selector") {
                    TimeSeriesGraphSelector(
                        options = chartRangeOptions,
                        currentSelection = chartRange,
                        onSelection = onChartRangeSelected,
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                            .fillMaxWidth()
                    )
                }

                item("divider2") {
                    Divider(
                        color = MaterialTheme.colors.onBackground.copy(alpha = 0.2f),
                        thickness = 1.dp,
                        modifier = Modifier
                            .padding(start = 12.dp, end = 12.dp, top = 2.dp, bottom = 20.dp)
                    )
                }
            }
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
            data = viewModel.chartData,
            ticksX = viewModel.accountPerformanceTicksX,
            ticksY = viewModel.accountPerformanceTicksY,
            chartRange = viewModel.accountPerformanceRange,
        )
    }
}