package com.example.zygos.ui.holdings

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.zygos.data.Position
import com.example.zygos.ui.components.PieChart
import com.example.zygos.ui.theme.ZygosTheme


@Composable
fun HoldingsScreen(
    positions: List<Position>,
    innerPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .padding(innerPadding)
            .fillMaxWidth(),
    ) {
        Surface(
            modifier = Modifier.fillMaxSize()
        ) {
            PieChart(
                tickers = positions.map { it.ticker },
                values = positions.map { it.value },
                colors = positions.map { it.color },
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxSize(),
                stroke = 30.dp,
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
    val positions = listOf(p1, p2, p3, p4)
    ZygosTheme {
        HoldingsScreen(
            positions,
            PaddingValues(0.dp)
        )
    }
}