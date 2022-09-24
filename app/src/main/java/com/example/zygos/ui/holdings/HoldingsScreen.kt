package com.example.zygos.ui.holdings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.zygos.data.Position
import com.example.zygos.ui.components.PieChart
import com.example.zygos.ui.components.recomposeHighlighter
import com.example.zygos.ui.theme.ZygosTheme


@Composable
fun HoldingsScreen(
    innerPadding: PaddingValues,
    positions: List<Position>,
    displayOption: String,
    modifier: Modifier = Modifier,
    accountBar: @Composable () -> Unit = { },
    onPositionClick: (Position) -> Unit = { },
    holdingsListOptionsCallback: () -> Unit = { },
    ) {

    Column(
        modifier = modifier
            .recomposeHighlighter()
            //.padding(innerPadding)
            // For some reason, innerPadding.bottom is non-zero (equal to the size of the bottom bar?)
            // But it's not needed, because this column doesn't overlap it anyways?
            .fillMaxWidth(),
    ) {
        accountBar()

        Surface(
            modifier = Modifier
                .fillMaxWidth()
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

                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(
                            start = 22.dp,
                            top = 0.dp,
                            bottom = 0.dp
                        )
                    ) {
                        Text(
                            text = "Holdings",
                            style = MaterialTheme.typography.h3,
                            modifier = Modifier.weight(1f),
                        )
                        IconButton(onClick = { holdingsListOptionsCallback() }) {
                            Icon(
                                imageVector = Icons.Sharp.MoreVert,
                                contentDescription = null,
                            )
                        }
                    }
                }

                itemsIndexed(positions) { index, pos ->
                    Column {
                        if (index > 0) Divider(
                            color = MaterialTheme.colors.onBackground.copy(alpha = 0.2f),
                            thickness = 1.dp,
                            modifier = Modifier
                                .padding(start = 12.dp, top = 2.dp, bottom = 2.dp)
                        )
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
                                .padding(horizontal = 6.dp)
                        )
                    }
                }
            }
        }
    }
}


@Preview(
    widthDp = 300,
    heightDp = 600,
    showBackground = true,
)
@Composable
fun PreviewHoldingsScreen() {
    val p1 = Position("p1", 0.2f, Color(0xFF004940))
    val p2 = Position("p2", 0.3f, Color(0xFF005D57))
    val p3 = Position("p3", 0.4f, Color(0xFF04B97F))
    val p4 = Position("p4", 0.1f, Color(0xFF37EFBA))
    val positions = listOf(p1, p2, p3, p4, p1, p2, p3, p4)
    ZygosTheme {
        HoldingsScreen(
            positions = positions,
            displayOption = "Returns",
            innerPadding = PaddingValues(0.dp),
        )
    }
}

