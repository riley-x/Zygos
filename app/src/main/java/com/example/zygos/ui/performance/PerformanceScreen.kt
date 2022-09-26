package com.example.zygos.ui.performance

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.zygos.viewModel.Quote
import com.example.zygos.ui.components.*
import com.example.zygos.ui.theme.ZygosTheme
import com.example.zygos.viewModel.TimeSeriesTick
import com.example.zygos.viewModel.accountPerformanceRangeOptions

@Composable
fun PerformanceScreen(
    accountPerformance: SnapshotStateList<Float>,
    accountPerformanceTicksY: SnapshotStateList<Float>,
    accountPerformanceTicksX: SnapshotStateList<TimeSeriesTick>,
    accountPerformanceRange: State<String>, // must pass state here for button group to calculate derivedStateOf
    watchlist: SnapshotStateList<Quote>,
    watchlistDisplayOption: String,
    modifier: Modifier = Modifier,
    onTickerSelected: (String) -> Unit = { },
    onAccountPerformanceRangeSelected: (String) -> Unit = { },
    onWatchlistOptionsClick: () -> Unit = { },
    accountBar: @Composable () -> Unit = { },
) {
    Column(
        modifier = modifier
            .fillMaxWidth(),
    ) {
        LogCompositions("Zygos", "PerformanceScreen")

        accountBar()

        Surface(
            modifier = Modifier
                .recomposeHighlighter()
                .fillMaxSize(),
            color = MaterialTheme.colors.background
        ) {
            LazyColumn {
                item("graph") {
                    TimeSeriesGraph(
                        values = accountPerformance,
                        ticksY = accountPerformanceTicksY,
                        ticksX = accountPerformanceTicksX,
                        minY = 0f,
                        maxY = 25f,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp)
                            .height(300.dp)
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
                        options = accountPerformanceRangeOptions,
                        currentSelection = accountPerformanceRange,
                        onSelection = onAccountPerformanceRangeSelected,
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

                item("watchlist_title") {
                    ListTitleBar(
                        text = "Watchlist",
                        onOptionsButtonClick = onWatchlistOptionsClick,
                        modifier = Modifier.padding(start = 22.dp)
                    )
                }

                itemsIndexed(watchlist, key = { _, ticker -> ticker.ticker }) { index, ticker ->
                    Column {
                        if (index > 0) TickerListDivider(modifier = Modifier.padding(horizontal = 6.dp))

                        TickerListRow(
                            ticker = ticker.ticker,
                            color = ticker.color,
                            value = ticker.price,
                            subvalue = when (watchlistDisplayOption) {
                                "% Change" -> ticker.percentChange
                                else -> ticker.change
                            },
                            isSubvalueDollar = (watchlistDisplayOption != "% Change"),
                            modifier = Modifier
                                .clickable {
                                    onTickerSelected(ticker.ticker)
                                }
                                // this needs to be here so that the clickable animation covers the full width
                                .padding(horizontal = 6.dp)
                        )
                    }
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
fun PreviewPerformanceScreen() {
    val accountPerformance = remember { List(20) { it * if (it % 2 == 0) 1.2f else 0.8f }.toMutableStateList() }
    val accountPerformanceTicksY = remember { mutableStateListOf(5f, 10f, 15f, 20f) }
    val accountPerformanceTicksX = remember { mutableStateListOf(
        TimeSeriesTick(5, "test"),
        TimeSeriesTick(10, "9/12/23"),
        TimeSeriesTick(15, "10/31/21"),
    ) }
    val currentAccountPerformanceRange = remember { mutableStateOf(accountPerformanceRangeOptions.items[0]) }
    val watchlist = remember { mutableStateListOf(
        Quote("t1", Color.Blue,  123.23f,  21.20f, 0.123f),
        Quote("t2", Color.Black, 1263.23f, 3.02f,  -0.123f),
        Quote("t3", Color.Green, 1923.23f, 120.69f,0.263f),
        Quote("t4", Color.Cyan,  1423.23f, 0.59f,  1.23f),
        Quote("t5", Color.Blue,  123.23f,  21.20f, 0.123f),
        Quote("t6", Color.Black, 1263.23f, 3.02f,  -0.123f),
        Quote("t7", Color.Green, 1923.23f, 120.69f,0.263f),
        Quote("t8", Color.Cyan,  1423.23f, 0.59f,  1.23f),
    ) }
    ZygosTheme {
        PerformanceScreen(
            accountPerformance = accountPerformance,
            accountPerformanceTicksX = accountPerformanceTicksX,
            accountPerformanceTicksY = accountPerformanceTicksY,
            accountPerformanceRange = currentAccountPerformanceRange,
            watchlist = watchlist,
            watchlistDisplayOption = "% Change",
        )
    }
}