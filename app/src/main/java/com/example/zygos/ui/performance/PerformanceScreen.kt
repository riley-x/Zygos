package com.example.zygos.ui.performance

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.zygos.data.Position
import com.example.zygos.ui.components.recomposeHighlighter
import com.example.zygos.ui.theme.ZygosTheme

@Composable
fun PerformanceScreen(
    innerPadding: PaddingValues,
    modifier: Modifier = Modifier,
    onTickerClick: (String) -> Unit = { },
    accountBar: @Composable () -> Unit = { },
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
                .recomposeHighlighter()
                .fillMaxSize(),
            color = MaterialTheme.colors.background
        ) {
            Text("Performance Screen")
        }
    }
}


@Preview(
    widthDp = 300,
    heightDp = 600,
    showBackground = true,
)
@Composable
fun PreviewPerformanceScreen() {
    ZygosTheme {
        PerformanceScreen(
            PaddingValues(0.dp)
        )
    }
}