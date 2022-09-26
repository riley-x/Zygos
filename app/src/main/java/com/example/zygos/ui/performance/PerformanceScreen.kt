package com.example.zygos.ui.performance

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.MoreVert
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.zygos.data.Position
import com.example.zygos.data.Quote
import com.example.zygos.ui.components.*
import com.example.zygos.ui.holdings.HoldingsRow
import com.example.zygos.ui.theme.ZygosTheme

val watchlistSortOptions = listOf("Ticker", "% Change")
val watchlistDisplayOptions = listOf("Change", "% Change")

@Composable
fun PerformanceScreen(
    watchlist: SnapshotStateList<Quote>,
    displayOption: String,
    modifier: Modifier = Modifier,
    onTickerClick: (String) -> Unit = { },
    onWatchlistOptionsClick: () -> Unit = { },
    accountBar: @Composable () -> Unit = { },
) {
    val values =
        remember { List(20) { it * if (it % 2 == 0) 1.2f else 0.8f }.toMutableStateList() }
    val ticksY = remember { mutableStateListOf(5f, 10f, 15f, 20f) }
    val ticksX = remember {
        mutableStateListOf(
            TimeSeriesTickX(5, "test"),
            TimeSeriesTickX(10, "9/12/23"),
            TimeSeriesTickX(15, "10/31/21"),
        )
    }
    val options = remember { mutableStateListOf("1m", "3m", "1y", "5y", "All") }
    var currentSelection = remember { mutableStateOf("1y") }
    fun onOptionsSelection(selection: String) { currentSelection.value = selection }

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
                item {
                    TimeSeriesGraph(
                        values = values,
                        ticksY = ticksY,
                        ticksX = ticksX,
                        minY = 0f,
                        maxY = 25f,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp)
                            .height(300.dp)
                    )
                }

                item {
                    Divider(
                        color = MaterialTheme.colors.onBackground.copy(alpha = 0.2f),
                        thickness = 1.dp,
                        modifier = Modifier
                            .padding(start = 12.dp, end = 12.dp, top = 10.dp, bottom = 2.dp)
                    )
                }

                item {
                    TimeSeriesGraphSelector(
                        options = options,
                        currentSelection = currentSelection,
                        onSelection = ::onOptionsSelection,
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                            .fillMaxWidth()
                    )
                }

                item {
                    Divider(
                        color = MaterialTheme.colors.onBackground.copy(alpha = 0.2f),
                        thickness = 1.dp,
                        modifier = Modifier
                            .padding(start = 12.dp, end = 12.dp, top = 2.dp, bottom = 20.dp)
                    )
                }

                item {
                    ListTitleBar(
                        text = "Watchlist",
                        onOptionsButtonClick = onWatchlistOptionsClick,
                        modifier = Modifier.padding(start = 22.dp)
                    )
                }

                itemsIndexed(watchlist) { index, ticker ->
                    Column {
                        if (index > 0) TickerListDivider(modifier = Modifier.padding(horizontal = 6.dp))

                        TickerListRow(
                            ticker = ticker.ticker,
                            color = ticker.color,
                            value = ticker.price,
                            subvalue = when (displayOption) {
                                "% Change" -> ticker.percentChange
                                else -> ticker.change
                            },
                            isSubvalueDollar = (displayOption != "% Change"),
                            modifier = Modifier
                                .clickable {
                                    onTickerClick(ticker.ticker)
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
    val q1 = Quote("asdf", Color.Blue,  123.23f,  21.20f, 0.123f)
    val q2 = Quote("af",   Color.Black, 1263.23f, 3.02f,  -0.123f)
    val q3 = Quote("afdf", Color.Green, 1923.23f, 120.69f,0.263f)
    val q4 = Quote("lkj",  Color.Cyan,  1423.23f, 0.59f,  1.23f)
    val watchlist = remember { mutableStateListOf(q1, q2, q3, q4, q1, q2, q3, q4) }
    ZygosTheme {
        PerformanceScreen(
            watchlist = watchlist,
            displayOption = "% Change",
        )
    }
}