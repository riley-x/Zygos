package com.example.zygos.ui.performance

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.zygos.ui.components.*
import com.example.zygos.ui.theme.ZygosTheme
import com.example.zygos.viewModel.*

@Composable
fun PerformanceScreen(
    accountPerformanceState: AccountPerformanceState,
    accountPerformanceTimeRange: State<String>, // must pass state here for button group to calculate derivedStateOf
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
            var hoverX by remember { mutableStateOf("") }
            var hoverY by remember { mutableStateOf("") }

            fun onGraphHover(isHover: Boolean, x: Int, y: Float) {
                if (isHover && x >= 0 && x < accountPerformanceState.values.size) {
                    hoverX = accountPerformanceState.values[x].name
                    hoverY = formatDollar(accountPerformanceState.values[x].value)
                } else {
                    hoverX = ""
                    hoverY = ""
                }
                //hoverY = if (isHover && y > 0f && y < 25f) formatDollar(y) else ""
            }

            LazyColumn {
                item("graph_hover") {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .height(20.dp)
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = hoverX,
                            style = MaterialTheme.typography.subtitle2,
                            modifier = Modifier
                                .weight(1f)
                        )
                        Text(
                            text = hoverY,
                            style = MaterialTheme.typography.subtitle2,
                        )
                    }
                }

                item("graph") {
                    if (accountPerformanceState.values.size < 2) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp)
                                .height(300.dp)
                        ) {
                            Text(
                                text = "No data!",
                                style = MaterialTheme.typography.h5,
                                color = MaterialTheme.colors.error,
                            )
                        }
                    }
                    else {
                        val grapher = lineGraph<TimeSeries>()
                        TimeSeriesGraph(
                            grapher = grapher,
                            state = accountPerformanceState,
                            onHover = ::onGraphHover,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp)
                                .height(300.dp)
                        )
                    }
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
                        currentSelection = accountPerformanceTimeRange,
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
    val viewModel = viewModel<TestViewModel>()
    ZygosTheme {
        PerformanceScreen(
            accountPerformanceState = viewModel.accountPerformanceState,
            accountPerformanceTimeRange = viewModel.accountPerformanceTimeRange,
            watchlist = viewModel.watchlist,
            watchlistDisplayOption = viewModel.watchlistDisplayOption,
        )
    }
}