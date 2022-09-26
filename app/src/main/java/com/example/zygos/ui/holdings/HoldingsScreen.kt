package com.example.zygos.ui.holdings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.zygos.viewModel.Position
import com.example.zygos.ui.components.*
import com.example.zygos.ui.theme.ZygosTheme

val holdingsListSortOptions = ImmutableList(listOf("Ticker", "Equity", "Returns", "% Change"))
val holdingsListDisplayOptions = ImmutableList(listOf("Returns", "% Change"))

@Composable
fun HoldingsScreen(
    positions: SnapshotStateList<Position>,
    displayOption: String,
    modifier: Modifier = Modifier,
    accountBar: @Composable () -> Unit = { },
    onPositionClick: (Position) -> Unit = { },
    holdingsListOptionsCallback: () -> Unit = { },
    ) {
    LogCompositions("Zygos", "HoldingsScreen")
    Column(
        modifier = modifier
            .fillMaxWidth(),
    ) {
        accountBar()

        Surface(
            modifier = Modifier
                .fillMaxSize()
        ) {
            LazyColumn {
                item("pie_chart") {
                    Row( // For some reason couldn't just use Modifier.alignment in PieChart
                        modifier = Modifier
                            .padding(start = 30.dp, end = 30.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        PieChart(
                            tickers = positions.map { it.ticker },
                            values = positions.map { it.value },
                            colors = positions.map { it.color },
                            modifier = Modifier
                                .heightIn(0.dp, 300.dp)
                                .aspectRatio(1f)
                                .fillMaxWidth(),
                            stroke = 30.dp,
                        )
                    }
                }

                item("holdings_title") {
                    ListTitleBar(
                        text = "Holdings",
                        modifier = Modifier.padding(start = 22.dp),
                        onOptionsButtonClick = holdingsListOptionsCallback,
                    )
                }

                itemsIndexed(positions, key = { _, pos -> pos.ticker }) { index, pos ->
                    Column {
                        if (index > 0) TickerListDivider(modifier = Modifier.padding(horizontal = 6.dp))

                        HoldingsRow(
                            ticker = pos.ticker,
                            color = pos.color,
                            value = pos.value,
                            shares = 17f,
                            subvalue = -134.13f,
                            isSubvalueDollar = (displayOption == "Returns"),
                            modifier = Modifier
                                .clickable {
                                    onPositionClick(pos)
                                }
                                .padding(horizontal = 6.dp) // this needs to be here so that the clickable
                                                            // animation covers the full width
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
fun PreviewHoldingsScreen() {
    val positions = remember { mutableStateListOf(
        Position("p1", 0.2f, Color(0xFF004940)),
        Position("p2", 0.3f, Color(0xFF005D57)),
        Position("p3", 0.4f, Color(0xFF04B97F)),
        Position("p4", 0.1f, Color(0xFF37EFBA)),
        Position("p5", 0.2f, Color(0xFF004940)),
        Position("p6", 0.3f, Color(0xFF005D57)),
        Position("p7", 0.4f, Color(0xFF04B97F)),
        Position("p8", 0.1f, Color(0xFF37EFBA)),
    ) }
    ZygosTheme {
        HoldingsScreen(
            positions = positions,
            displayOption = "Returns",
        )
    }
}

