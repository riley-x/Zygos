package com.example.zygos.ui.performance

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.zygos.data.toFloatDollar
import com.example.zygos.ui.components.*
import com.example.zygos.ui.graphing.TimeSeriesGraph
import com.example.zygos.ui.graphing.TimeSeriesGraphSelector
import com.example.zygos.ui.graphing.TimeSeriesGraphState
import com.example.zygos.ui.graphing.lineGraph
import com.example.zygos.ui.theme.ZygosTheme
import com.example.zygos.viewModel.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PerformanceScreen(
    currentEquity: Long,
    currentChange: Long,
    currentChangePercent: Float,
    accountPerformanceState: State<TimeSeriesGraphState<TimeSeries>>,
    accountPerformanceTimeRange: State<String>, // must pass state here for button group to calculate derivedStateOf
    watchlist: SnapshotStateList<Quote>,
    watchlistDisplayOption: String,
    modifier: Modifier = Modifier,
    bottomPadding: Dp = 0.dp,
    onTickerSelected: (String) -> Unit = { },
    onAccountPerformanceRangeSelected: (String) -> Unit = { },
    onWatchlistOptionsClick: () -> Unit = { },
    onWatchlistDelete: (String) -> Unit = { },
    onAddAllHoldingsToWatchlist: () -> Unit = { },
    accountSelectionBar: @Composable () -> Unit = { },
) {
    LogCompositions("Zygos", "PerformanceScreen")

    Column(
        modifier = modifier
            .recomposeHighlighter()
            .padding(bottom = bottomPadding)
            .fillMaxSize(),
    ) {
        accountSelectionBar()

        var hoverX by remember { mutableStateOf("") }
        var hoverY by remember { mutableStateOf("") }

        fun onGraphHover(isHover: Boolean, x: Int, y: Float) {
            if (isHover && x >= 0 && x < accountPerformanceState.value.values.size) {
                hoverX = accountPerformanceState.value.values[x].name
                hoverY = formatDollar(accountPerformanceState.value.values[x].value)
            } else {
                hoverX = ""
                hoverY = ""
            }
        }

        LazyColumn {

            item("equity_title") {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(horizontal = accountHeaderHorizontalPadding)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = formatDollar(currentEquity.toFloatDollar()),
                        style = MaterialTheme.typography.h2,
                        color = MaterialTheme.colors.onSurface, // this is necessary for some reason
                        modifier = Modifier.weight(10f),
                    )
                    Column(
                        horizontalAlignment = Alignment.End,
                    ) {
                        val isPositive by remember { derivedStateOf { currentChange >= 0 } }
                        val color = if (isPositive) MaterialTheme.colors.primary
                        else MaterialTheme.colors.error
                        Text(
                            text = formatDollar(currentChange.toFloatDollar()),
                            color = color
                        )
                        Text(
                            text = formatPercent(currentChangePercent),
                            color = color
                        )
                    }
                }
            }

            item("graph_hover") {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .height(20.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = hoverX,
                        style = MaterialTheme.typography.overline,
                        modifier = Modifier
                            .weight(1f)
                    )
                    Text(
                        text = hoverY,
                        style = MaterialTheme.typography.overline,
                    )
                }
            }

            item("graph") {
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

            item("divider1") {
                Divider(
                    color = MaterialTheme.colors.onBackground.copy(alpha = 0.2f),
                    thickness = 1.dp,
                    modifier = Modifier
                        .padding(start = 12.dp, end = 12.dp, top = 10.dp, bottom = 2.dp)
                        .recomposeHighlighter()
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

            stickyHeader("watchlist_title") {
                ListTitleBar(
                    text = "Watchlist",
                    onOptionsButtonClick = onWatchlistOptionsClick,
                )
            }

            if (watchlist.isEmpty()) {
                item("watchlist_add_all") {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(
                            onClick = onAddAllHoldingsToWatchlist,
                            border = BorderStroke(2.dp, MaterialTheme.colors.error),
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colors.error
                            ),
                            modifier = Modifier
                                .padding(vertical = 4.dp)
                        ) {
                            Text("Add All Holdings")
                        }
                    }
                }
            }

            itemsIndexed(watchlist, key = { _, ticker -> ticker.ticker }) { index, ticker ->
                Column {
                    if (index > 0) TickerListDivider()

                    WatchlistRow(
                        quote = ticker,
                        displayOption = watchlistDisplayOption,
                        modifier = Modifier
                            .clickable { onTickerSelected(ticker.ticker) }
                            // this needs to be here so that the clickable animation covers the full width

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
fun PreviewPerformanceScreen() {
    val viewModel = viewModel<TestViewModel>()
    ZygosTheme {
        Surface {
            PerformanceScreen(
                1000000,
                100000,
                .1f,
                accountPerformanceState = viewModel.accountPerformanceState,
                accountPerformanceTimeRange = viewModel.accountPerformanceTimeRange,
                watchlist = viewModel.watchlist,
                watchlistDisplayOption = viewModel.watchlistDisplayOption,
            )
        }
    }
}