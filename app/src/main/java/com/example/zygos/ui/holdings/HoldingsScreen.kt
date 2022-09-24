package com.example.zygos.ui.holdings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.zygos.data.Position
import com.example.zygos.ui.components.PieChart
import com.example.zygos.ui.theme.ZygosTheme
import kotlinx.coroutines.launch


@Composable
fun HoldingsScreen(
    positions: List<Position>,
    innerPadding: PaddingValues,
    modifier: Modifier = Modifier,
    accountBar: @Composable () -> Unit = { },
    onPositionClick: (Position) -> Unit = { },
    holdingsListOptionsCallback: () -> Unit = { },
    ) {

    Column(
        modifier = modifier
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
                    PieChart(
                        tickers = positions.map { it.ticker },
                        values = positions.map { it.value },
                        colors = positions.map { it.color },
                        modifier = Modifier
                            .padding(start = 30.dp, end = 30.dp)
                            .fillMaxWidth()
                            .heightIn(0.dp, 300.dp),
                        stroke = 30.dp,
                    )
                }

                item {
                    Row(
                        modifier = Modifier.padding(start = 22.dp, top = 12.dp, bottom = 12.dp)
                    ) {
                        Text(
                            text = "Holdings",
                            style = MaterialTheme.typography.h3,
                            modifier = Modifier.weight(1f),
                        )
                        Button(onClick = { holdingsListOptionsCallback() }) {
                            Text("Click to show sheet")
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
                            gain = -134.13f,
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


fun holdingsListOptionsSheet(
    onOptionSelection: (String) -> Unit = { },
) : (@Composable ColumnScope.() -> Unit) =
    @Composable {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp, top = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Item 1", modifier = Modifier.weight(1f))
                Icon(
                    Icons.Default.Favorite,
                    contentDescription = "Localized description"
                )
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
            positions,
            PaddingValues(0.dp)
        )
    }
}

@Preview(
    widthDp = 300,
    heightDp = 600,
    showBackground = true,
    backgroundColor = 0xFF666666,
)
@Composable
fun PreviewHoldingsListOptionsSheet() {
    ZygosTheme {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Bottom,
        ) {
            Surface {
                holdingsListOptionsSheet()(this)
            }
        }
    }
}