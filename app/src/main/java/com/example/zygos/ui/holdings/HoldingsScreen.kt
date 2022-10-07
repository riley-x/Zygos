package com.example.zygos.ui.holdings

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.zygos.ui.components.*
import com.example.zygos.ui.graphing.PieChart
import com.example.zygos.ui.theme.ZygosTheme
import com.example.zygos.viewModel.Position
import com.example.zygos.viewModel.TestViewModel

/**
 * @param positions should have one entry per ticker, and correspond to each pie chart wedge and
 * main list ticker row. Long options will appear as fragments in the pie chart and covered calls
 * will appear as subwedges, and will show up in the watchlist when the ticker row is expanded.
 * Short positions appear as subwedges under the CASH wedge and in a separate watchlist.
 */
@Composable
fun HoldingsScreen(
    longPositionsAreLoading: Boolean,
    shortPositionsAreLoading: Boolean,
    longPositions: SnapshotStateList<Position>,
    shortPositions: SnapshotStateList<Position>,
    tickerColors: SnapshotStateMap<String, Color>,
    displayOption: String,
    modifier: Modifier = Modifier,
    onPositionClick: (String) -> Unit = { },
    holdingsListOptionsCallback: () -> Unit = { },
) {
    LogCompositions("Zygos/Compositions", "HoldingsScreen")

    LazyColumn {

        item("pie_chart") {
            Row(
                modifier = Modifier
                    .padding(start = 30.dp, end = 30.dp)
                    .heightIn(0.dp, 300.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .aspectRatio(1f)
                        .fillMaxWidth(),
                ) {
                    if (longPositionsAreLoading || shortPositionsAreLoading) {
                        CircularProgressIndicator()
                    }
                    else if (longPositions.isEmpty()) { // don't need a derivedStateOf since this is rare
                        Text(
                            text = "No data!",
                            style = MaterialTheme.typography.h5,
                            color = MaterialTheme.colors.error,
                        )
                    } else {
                        PieChart(
                            tickers = longPositions.map { it.ticker },
                            values = longPositions.map { it.equity },
                            colors = longPositions.map {
                                tickerColors.getOrDefault(
                                    it.ticker,
                                    Color.Black
                                )
                            },
                            stroke = 30.dp,
                        )
                    }
                }
            }
        }

        item("long") {
            ListTitleBar(
                text = "Long Positions",
                modifier = Modifier.padding(start = 22.dp),
                onOptionsButtonClick = holdingsListOptionsCallback,
            )
        }

        if (!longPositionsAreLoading) {
            itemsIndexed(longPositions, key = { _, pos -> pos.ticker } ) { index, pos ->
                Column {
                    if (index > 0) TickerListDivider(modifier = Modifier.padding(horizontal = 6.dp))

                    HoldingsRow(
                        position = pos,
                        color = tickerColors.getOrDefault(pos.ticker, Color.Black),
                        displayOption = displayOption,
                        modifier = Modifier
                            .clickable { onPositionClick(pos.ticker) }
                            .padding(horizontal = 6.dp) // this needs to be second so that the clickable
                                                        // animation covers the full width
                    )
                }
            }
        } else {
            item("long spinner") {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .height(60.dp)
                        .fillMaxWidth(),
                ) {
                    CircularProgressIndicator()
                }
            }
        }

        item("short") {
            ListTitleBar(
                text = "Short Positions",
                modifier = Modifier.padding(start = 22.dp, top = 20.dp),
                onOptionsButtonClick = holdingsListOptionsCallback,
            )
        }

        if (!shortPositionsAreLoading) {
            itemsIndexed(shortPositions) { index, pos ->
                Column {
                    if (index > 0) TickerListDivider(modifier = Modifier.padding(horizontal = 6.dp))

                    // TODO
                }
            }
        } else {
            item("short spinner") {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .height(60.dp)
                        .fillMaxWidth(),
                ) {
                    CircularProgressIndicator()
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
fun PreviewHoldingsScreen() {
    val viewModel = viewModel<TestViewModel>()
    ZygosTheme {
        Surface {
            HoldingsScreen(
                longPositionsAreLoading = false,
                shortPositionsAreLoading = false,
                longPositions = viewModel.longPositions,
                shortPositions = viewModel.shortPositions,
                tickerColors = viewModel.tickerColors,
                displayOption = "Returns",
            )
        }
    }
}

