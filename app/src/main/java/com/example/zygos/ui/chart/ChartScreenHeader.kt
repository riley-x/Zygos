package com.example.zygos.ui.chart

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.AddCircleOutline
import androidx.compose.material.icons.sharp.RemoveCircleOutline
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.zygos.ui.components.TickerSelector
import com.example.zygos.ui.theme.ZygosTheme
import com.example.zygos.viewModel.contains
import com.example.zygos.viewModel.Quote
import com.example.zygos.viewModel.TestViewModel

@Composable
fun ChartScreenHeader(
    ticker: State<String>,
    colors: SnapshotStateMap<String, Color>,
    watchlist: SnapshotStateList<Quote>,
    hoverTime: State<String>,
    hoverValues: State<String>,
    modifier: Modifier = Modifier,
    onTickerChanged: (String) -> Unit = { },
    onToggleWatchlist: (String) -> Unit = { },
    onChangeColor: (String) -> Unit = { },
) {
    // Ticker selection bar, also chart hover text goes here to save space
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .height(43.dp)
            // There seems to be a minimum height that enables the droplet selection.
            // 42.dp doesn't work but 43.dp does
            .fillMaxWidth()
            .padding(start = 12.dp)
    ) {
        // The weights fix the ticker selector to the first 1/3
        TickerSelector(
            ticker = ticker.value,
            onTickerGo = onTickerChanged,
            modifier = Modifier
                .weight(1f)
        )

        // Hover text uses up the last 2/3, aligned bottom-right
        Row(
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(2f)
                .fillMaxHeight()
        ) {
            val showSelectors by remember { derivedStateOf {
                hoverValues.value.isBlank() && ticker.value.isNotBlank()
            } }

            if (showSelectors) {
                val watchlistIcon by remember { derivedStateOf {
                    if (watchlist.contains(ticker.value)) Icons.Sharp.RemoveCircleOutline
                    else Icons.Sharp.AddCircleOutline
                } }
                IconButton(onClick = { onToggleWatchlist(ticker.value) }) {
                    Icon(
                        imageVector = watchlistIcon,
                        contentDescription = null
                    )
                }

                Canvas(modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .size(30.dp)
                    .clickable { onChangeColor(ticker.value) }
                ) {
                    drawRect(color = colors.getOrDefault(ticker.value, Color.White))
                }
            } else {
                Text(
                    text = hoverTime.value,
                    style = MaterialTheme.typography.overline,
                    modifier = modifier.align(Alignment.Bottom)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = hoverValues.value,
                    style = MaterialTheme.typography.overline,
                    modifier = modifier.align(Alignment.Bottom)
                )
            }
        }
    }
}


@Preview
@Composable
private fun PreviewHover() {
    val viewModel = viewModel<TestViewModel>()
    val hoverTime = remember { mutableStateOf("9/27/22") }
    val hoverValues = remember { mutableStateOf("O: 34.23  H: 36.43\nC: 35.02  L: 33.98") }
    ZygosTheme {
        Surface {
            ChartScreenHeader(
                ticker = viewModel.chartTicker,
                colors = viewModel.tickerColors,
                watchlist = viewModel.watchlist,
                hoverTime = hoverTime,
                hoverValues = hoverValues
            )
        }
    }
}

@Preview
@Composable
private fun PreviewNoHover() {
    val viewModel = viewModel<TestViewModel>()
    val hoverTime = remember { mutableStateOf("") }
    val hoverValues = remember { mutableStateOf("") }
    ZygosTheme {
        Surface {
            ChartScreenHeader(
                ticker = viewModel.chartTicker,
                colors = viewModel.tickerColors,
                watchlist = viewModel.watchlist,
                hoverTime = hoverTime,
                hoverValues = hoverValues
            )
        }
    }
}