package com.example.zygos.ui.chart

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.zygos.ui.theme.ZygosTheme


@Composable
fun ChartScreen(
    innerPadding: PaddingValues,
) {
    Surface(
        modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {
        Text("Chart Screen")
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
            PaddingValues(0.dp)
        )
    }
}