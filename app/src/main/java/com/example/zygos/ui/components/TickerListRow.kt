package com.example.zygos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.zygos.ui.theme.ZygosTheme
import com.example.zygos.ui.theme.tickerColors

/**
 * Base composable of any list of tickers and prices
 *
 * Flush horizontal, height fixed
 */
@Composable
fun TickerListRow(
    ticker: String,
    color: Color,
    value: Float,
    subvalue: Float,
    isSubvalueDollar: Boolean,
    modifier: Modifier = Modifier,
    afterTickerContent: @Composable (padStart: Dp) -> Unit = { },
) {
    Row(
        modifier = modifier
            .recomposeHighlighter()
            .height(52.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val typography = MaterialTheme.typography

        Spacer(
            Modifier
                .size(4.dp, 46.dp)
                .background(color = color)
        )
        Spacer(Modifier.width(12.dp))

        Text(text = ticker, style = typography.body1)

        afterTickerContent(12.dp)

        Spacer(Modifier.weight(1f))

        Column(Modifier, horizontalAlignment = Alignment.End) {
            Text(text = formatDollar(value), style = typography.body1)
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                Text(text = if (isSubvalueDollar) formatDollar(subvalue) else formatPercent(subvalue),
                    style = typography.subtitle1,
                    color = if (subvalue >= 0) MaterialTheme.colors.primary else MaterialTheme.colors.error
                )
            }
        }
    }
}

@Composable
fun TickerListDivider(
    modifier: Modifier = Modifier,
) {
    Divider(
        color = MaterialTheme.colors.onBackground.copy(alpha = 0.2f),
        thickness = 1.dp,
        modifier = Modifier
            .padding(start = 6.dp, top = 2.dp, bottom = 2.dp)
    )
}


@Preview(showBackground = true)
@Composable
fun TickerListRowPreview() {
    ZygosTheme {
        Surface() {
            Column {
                TickerListRow(
                    ticker = "MSFT",
                    color = tickerColors.getOrDefault("MSFT", Color.Blue),
                    value = 4567.32f,
                    subvalue = -1342.01f,
                    isSubvalueDollar = true,
                )

                TickerListDivider()

                TickerListRow(
                    ticker = "MSFT",
                    color = tickerColors.getOrDefault("MSFT", Color.Blue),
                    value = 1357.32f,
                    subvalue = 0.1234f,
                    isSubvalueDollar = false,
                )
            }
        }
    }
}