package com.example.zygos.ui.chart

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.zygos.ui.components.LogCompositions
import com.example.zygos.ui.components.recomposeHighlighter
import com.example.zygos.ui.theme.ZygosTheme


@Composable
fun ChartScreen(
    testState: String = "",
) {
    LogCompositions("Zygos", "ChartScreen")

    Surface(
        modifier = Modifier
            .recomposeHighlighter()
            .fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.requiredSize(200.dp).recomposeHighlighter()
        ) {
            Text("Chart Screen$testState")
        }
    }
}

@Preview(
    widthDp = 300,
    heightDp = 600,
    showBackground = true,
)
@Composable
fun PreviewChartScreen() {
    ZygosTheme {
        ChartScreen(

        )
    }
}