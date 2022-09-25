package com.example.zygos.ui.performance

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.zygos.data.Position
import com.example.zygos.ui.components.LogCompositions
import com.example.zygos.ui.components.TimeSeriesGraph
import com.example.zygos.ui.components.TimeSeriesTickX
import com.example.zygos.ui.components.recomposeHighlighter
import com.example.zygos.ui.theme.ZygosTheme

@Composable
fun PerformanceScreen(
    modifier: Modifier = Modifier,
    onTickerClick: (String) -> Unit = { },
    accountBar: @Composable () -> Unit = { },
) {
    val values =
        remember { List(20) { it * if (it % 2 == 0) 1.2f else 0.8f }.toMutableStateList() }
    val ticksY = remember { mutableStateListOf(5f, 10f, 15f, 20f) }
    val ticksX = remember {
        mutableStateListOf(
            TimeSeriesTickX(5, "test"),
            TimeSeriesTickX(10, "9/12/23"),
            TimeSeriesTickX(15, "10/31/21"),
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth(),
    ) {
        LogCompositions("Zygos", "PerformanceScreen")

        accountBar()

        Surface(
            modifier = Modifier
                .fillMaxSize(),
            color = MaterialTheme.colors.background
        ) {
            LazyColumn {
                item {
                    TimeSeriesGraph(
                        values = values,
                        ticksY = ticksY,
                        ticksX = ticksX,
                        minY = 0f,
                        maxY = 25f,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp)
                            .height(300.dp)
                    )
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
fun PreviewPerformanceScreen() {
    ZygosTheme {
        PerformanceScreen(
        )
    }
}