package com.example.zygos.ui.performance

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.zygos.ui.components.*
import com.example.zygos.ui.theme.ZygosTheme
import com.example.zygos.viewModel.Quote
import com.example.zygos.viewModel.TestViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun WatchlistRow(
    quote: Quote,
    displayOption: String,
    modifier: Modifier = Modifier,
    onDelete: (String) -> Unit = { },
) {
    val swipeableState = rememberSwipeableState(0)
    var sizePx by remember { mutableStateOf(100) }
    val anchors = mapOf(0f to 0, sizePx.toFloat() to 1)

    LaunchedEffect(swipeableState.currentValue) {
        if (swipeableState.currentValue == 1)
            onDelete(quote.ticker)
    }

    Box(
        modifier = modifier
            .onGloballyPositioned { sizePx = it.size.width }
            .swipeable(
                state = swipeableState,
                anchors = anchors,
                thresholds = { _, _ -> FractionalThreshold(0.5f) },
                orientation = Orientation.Horizontal,
                reverseDirection = true,
//                resistance = ResistanceConfig(100f, 0f, 0f), // disable
                velocityThreshold = 5000.dp, // default is 125
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
            modifier = Modifier
                .height(tickerListHeight)
                .fillMaxWidth()
                .background(MaterialTheme.colors.error)
        ) {
            Text(
                text = "Remove",
                modifier = Modifier.padding(end = tickerListHorizontalPadding),
                color = MaterialTheme.colors.onSurface,
                style = MaterialTheme.typography.h6,
            )
        }

        TickerListRow(
            ticker = quote.ticker,
            color = quote.color,
            modifier = Modifier
                .offset { IntOffset(-swipeableState.offset.value.roundToInt(), 0) }
                .background(MaterialTheme.colors.surface)
                .padding(horizontal = tickerListHorizontalPadding)
        ) {
            Spacer(Modifier.weight(10f))
            val color = if (quote.change > 0) MaterialTheme.colors.primary
            else if (quote.change == 0f) MaterialTheme.colors.onSurface
            else MaterialTheme.colors.error

            when (displayOption) {
                "Change" -> Text(
                    formatDollar(quote.change),
                    color = color,
                    style = MaterialTheme.typography.h4,
                )
                "% Change" -> Text(
                    formatPercent(quote.percentChange),
                    color = color,
                    style = MaterialTheme.typography.h4,
                )
                else -> Text(
                    formatDollar(quote.price),
                    color = color,
                    style = MaterialTheme.typography.h4,
                )
            }
        }

    }
}


@Preview
@Composable
fun PreviewWatchlistRow() {
    val viewModel = viewModel<TestViewModel>()
    ZygosTheme {
        Surface {
            Column {
                WatchlistRow(quote = viewModel.watchlist[0], displayOption = "Change")

                Spacer(Modifier.height(30.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier
                        .height(tickerListHeight)
                        .fillMaxWidth()
                        .background(MaterialTheme.colors.error)
                ) {
                    Text(
                        text = "Remove",
                        modifier = Modifier.padding(end = tickerListHorizontalPadding),
                        color = MaterialTheme.colors.onSurface,
                        style = MaterialTheme.typography.h6,
                    )
                }
            }
        }
    }
}