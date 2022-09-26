package com.example.zygos.ui.chart

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.zygos.data.Position
import com.example.zygos.ui.components.LogCompositions
import com.example.zygos.ui.components.recomposeHighlighter
import com.example.zygos.ui.theme.ZygosTheme


@Composable
fun ChartScreen(
    positions: SnapshotStateList<Position> = mutableStateListOf(),
    testState: String = "",
) {
    LogCompositions("Zygos", "ChartScreen")

    // TODO: Replace account bar with a ticker selector
    // I think vertical only like Robinhood is good
    // Horizontal doesn't look that nice on narrow phones

    Surface(
        modifier = Modifier
            .recomposeHighlighter()
            .fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .requiredSize(width = 200.dp, height = 500.dp)
                .recomposeHighlighter()
        ) {
            Column {
                Text("ChartScreen$testState")
                for (pos in positions) {
                    Text(pos.ticker)
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
fun PreviewChartScreen() {
    ZygosTheme {
        ChartScreen(

        )
    }
}