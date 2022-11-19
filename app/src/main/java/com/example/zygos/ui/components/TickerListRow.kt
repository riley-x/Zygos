package com.example.zygos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.zygos.ui.theme.ZygosTheme

/** Default padding for ticker lists. Can't apply below because padding should be set after clickable
 * so that the ripple covers the whole screen width
 */
val tickerListHorizontalPadding = 20.dp
val tickerListHeight = 60.dp

/**
 * Base composable of any list of tickers and prices.
 * Flush horizontal, height fixed
 */
@Composable
fun TickerListRow(
    ticker: String,
    color: Color,
    modifier: Modifier = Modifier,
    afterTickerContent: @Composable (RowScope.() -> Unit) = { },
) {
    Row(
        modifier = modifier
            .height(tickerListHeight),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.width(90.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Spacer(
                Modifier
                    .size(4.dp, 50.dp)
                    .background(color = color)
            )
            Text(
                text = ticker,
                style = MaterialTheme.typography.body1,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }

        afterTickerContent(this)
    }
}

@Composable
fun ValueAndSubvalue(
    value: Float,
    subvalue: Float,
    modifier: Modifier = Modifier,
    isSubvalueDollar: Boolean = true,
) {
    Column(modifier, horizontalAlignment = Alignment.End) {
        Text(text = formatDollar(value), style = MaterialTheme.typography.body1)
        Text(text = if (isSubvalueDollar) formatDollar(subvalue) else formatPercent(subvalue),
            style = MaterialTheme.typography.subtitle1,
            color = if (subvalue >= 0) MaterialTheme.colors.primary.copy(alpha = ContentAlpha.medium) else MaterialTheme.colors.error
        )
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
    TickerListRow(
        ticker = ticker,
        color = color,
        modifier = modifier,
    ) {

        afterTickerContent(this)

        Spacer(Modifier.weight(10f))

        ValueAndSubvalue(value = value, subvalue = subvalue, isSubvalueDollar = isSubvalueDollar)
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
            .padding(horizontal = tickerListHorizontalPadding)
    )
}


@Preview(showBackground = true)
@Composable
fun TickerListRowPreview() {
    ZygosTheme {
        Surface() {
            Column {
                TickerListRow(
                    ticker = "CMCSA",
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