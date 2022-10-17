package com.example.zygos.ui.holdings

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.zygos.data.PricedPosition
import com.example.zygos.ui.components.LogCompositions
import com.example.zygos.ui.components.TickerListDivider
import com.example.zygos.ui.components.tickerListHorizontalPadding
import com.example.zygos.ui.graphing.PieChart
import com.example.zygos.ui.theme.ZygosTheme
import com.example.zygos.viewModel.TestViewModel

/**
 * @param longPositions should have one entry per ticker, and correspond to each pie chart wedge and
 * main list ticker row. Long options will appear as fragments in the pie chart and covered calls
 * will appear as subwedges, and will show up in the watchlist when the ticker row is expanded. CASH
 * should be the last entry.
 * @param shortPositions appear as subwedges under the CASH wedge and in a separate watchlist.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HoldingsScreen(
    longPositionsAreLoading: Boolean,
    shortPositionsAreLoading: Boolean,
    longPositions: SnapshotStateList<PricedPosition>,
    shortPositions: SnapshotStateList<PricedPosition>,
    tickerColors: SnapshotStateMap<String, Color>,
    displayLongOption: HoldingsListDisplayOptions,
    displayShortOption: HoldingsListDisplayOptions,
    modifier: Modifier = Modifier,
    bottomPadding: Dp = 0.dp,
    onPositionClick: (PricedPosition) -> Unit = { },
    holdingsListLongOptionsCallback: () -> Unit = { },
    holdingsListShortOptionsCallback: () -> Unit = { },
    accountSelectionBar: @Composable () -> Unit = { },
) {
    LogCompositions("Zygos/Compositions", "HoldingsScreen")
    val showPercentages = rememberSaveable { mutableStateOf(false) }

    Column(modifier.padding(bottom = bottomPadding)) {
        accountSelectionBar()

        Box(Modifier.height(3.dp)) {
            if (longPositionsAreLoading || shortPositionsAreLoading) {
                LinearProgressIndicator(Modifier.fillMaxSize())
            }
        }

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
                        if (longPositions.isEmpty()) { // don't need a derivedStateOf since this is rare
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

            if (longPositions.isNotEmpty()) { // TODO derivedStateOf?
                stickyHeader("long") {
                    HoldingsListTitle(
                        text = "Long Positions",
                        showPercentages = showPercentages.value,
                        onOptionsButtonClick = holdingsListLongOptionsCallback,
                        onToggleShowPercentages = { showPercentages.value = !showPercentages.value },
                    )
                }
            }

            // This must be keyed, or else the LazyColumn will reuse composables, and the remembered
            // expanded parameter will not be reset.
            itemsIndexed(longPositions, key = { _, pos -> pos.account + pos.ticker } ) { index, pos ->
                Column {
                    if (index > 0) TickerListDivider(
                        modifier = Modifier.padding(horizontal = tickerListHorizontalPadding)
                    )

                    HoldingsRow(
                        position = pos,
                        color = tickerColors.getOrDefault(pos.ticker, Color.Black),
                        displayOption = displayLongOption,
                        showPercentages = showPercentages,
                        onPositionClick = onPositionClick,
                    )
                }
            }

            if (shortPositions.isNotEmpty()) { // TODO derivedStateOf?
                stickyHeader("short") {
                    HoldingsListTitle(
                        text = "Short Positions",
                        showPercentages = showPercentages.value,
                        onOptionsButtonClick = holdingsListShortOptionsCallback,
                        onToggleShowPercentages = { showPercentages.value = !showPercentages.value },
                        modifier = Modifier.padding(top = 20.dp)
                    )
                }
            }

            itemsIndexed(shortPositions) { index, pos ->
                Column {
                    if (index > 0) TickerListDivider(
                        modifier = Modifier.padding(horizontal = tickerListHorizontalPadding)
                    )

                    HoldingsRow(
                        position = pos,
                        color = tickerColors.getOrDefault(pos.ticker, Color.Black),
                        displayOption = displayShortOption,
                        showPercentages = showPercentages,
                        onPositionClick = onPositionClick,
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
                displayLongOption = HoldingsListDisplayOptions.RETURNS_TOTAL,
                displayShortOption = HoldingsListDisplayOptions.RETURNS_TOTAL,
            )
        }
    }
}

