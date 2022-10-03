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

/**
 * Base composable of any list of tickers and prices.
 * Flush horizontal, height fixed
 */
@Composable
fun TickerListRow(
    ticker: String,
    color: Color,
    modifier: Modifier = Modifier,
    tickerWeight: Float = 0f,
    afterTickerContent: @Composable (RowScope.() -> Unit) = { },
) {
    Row(
        modifier = modifier
            .recomposeHighlighter()
            .height(52.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(tickerWeight),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Spacer(
                Modifier
                    .size(4.dp, 46.dp)
                    .background(color = color)
            )
            Text(
                text = ticker,
                style = MaterialTheme.typography.body1,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }

        afterTickerContent(this)
    }
}

/**
 * Right aligns a value and subvalue field
 */
@Composable
fun TickerListValueRow(
    ticker: String,
    color: Color,
    value: Float,
    subvalue: Float,
    isSubvalueDollar: Boolean,
    modifier: Modifier = Modifier,
    afterTickerContent: @Composable (RowScope.() -> Unit) = { },
) {
    TickerListRow(ticker = ticker, color = color, modifier = modifier) {

        afterTickerContent(this)

        Spacer(Modifier.weight(1f))

        Column(Modifier, horizontalAlignment = Alignment.End) {
            Text(text = formatDollar(value), style = MaterialTheme.typography.body1)
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                Text(text = if (isSubvalueDollar) formatDollar(subvalue) else formatPercent(subvalue),
                    style = MaterialTheme.typography.subtitle1,
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
        modifier = modifier
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
                    color = Color(0xff00a1f1),
                )

                TickerListDivider()

                TickerListValueRow(
                    ticker = "MSFT",
                    color = Color(0xff00a1f1),
                    value = 4567.32f,
                    subvalue = -1342.01f,
                    isSubvalueDollar = true,
                )

                TickerListDivider()

                TickerListValueRow(
                    ticker = "MSFT",
                    color = Color(0xff00a1f1),
                    value = 1357.32f,
                    subvalue = 0.1234f,
                    isSubvalueDollar = false,
                )
            }
        }
    }
}