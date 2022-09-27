package com.example.zygos.ui.chart

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.LastBaseline
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
    onTickerChanged: (String) -> Unit = { },
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

        Box(
            modifier = modifier
                .fillMaxSize(),
        ) {
            var hoverTime by remember { mutableStateOf("") }
            var hoverValues by remember { mutableStateOf("") }

            val tickerSelectorHeight = 50.dp

            fun onGraphHover(isHover: Boolean, x: Int, y: Float) {
                if (isHover && x >= 0 && x < data.size) {
                    hoverTime = data[x].name
                    val open = formatDollarNoSymbol(data[x].open)
                    val close = formatDollarNoSymbol(data[x].close)
                    val high = formatDollarNoSymbol(data[x].high)
                    val low = formatDollarNoSymbol(data[x].low)
                    val maxLength = maxOf(open.length, close.length, high.length, low.length)
                    // can set a flag here to disable the hoverTime if length is too long
                    hoverValues = "O: " + open.padStart(maxLength) +
                            "  H: " + high.padStart(maxLength) +
                            "\nC: " + close.padStart(maxLength) +
                            "  L: " + low.padStart(maxLength)
                } else {
                    hoverTime = ""
                    hoverValues = ""
                }
            }

            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier
                    .height(tickerSelectorHeight)
                    .align(Alignment.TopStart)
                    .fillMaxWidth()
            ) {
                // The weights fix the ticker selector to the first 1/3
                TickerSelector(
                    ticker = ticker.value,
                    onTickerGo = onTickerChanged,
                    modifier = Modifier
//                        .requiredHeight(80.dp)
//                        .height(36.dp) // compress the text onto the indicator line
                        .weight(1f)
                )

                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier
                        .weight(2f)
                ) {
                    Text(
                        text = hoverTime,
                        style = MaterialTheme.typography.subtitle2,
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = hoverValues,
                        style = MaterialTheme.typography.subtitle2,
                    )
                }

            }

            LazyColumn(
                modifier = Modifier
                    .padding(top = tickerSelectorHeight)
                    .align(Alignment.TopStart)
            ) {

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