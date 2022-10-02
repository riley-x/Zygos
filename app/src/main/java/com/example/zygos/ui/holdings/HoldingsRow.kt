package com.example.zygos.ui.holdings

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.zygos.ui.components.TickerListValueRow
import com.example.zygos.ui.theme.ZygosTheme
import com.example.zygos.ui.theme.defaultTickerColors

@Composable
fun HoldingsRow(
    ticker: String,
    color: Color,
    shares: Float,
    value: Float,
    subvalue: Float,
    isSubvalueDollar: Boolean,
    modifier: Modifier = Modifier,
) {
    TickerListValueRow(
        ticker = ticker,
        color = color,
        value = value,
        subvalue = subvalue,
        isSubvalueDollar = isSubvalueDollar,
        modifier = modifier,
    ) {
        Column(Modifier) {
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                Text(text = "$shares", style = MaterialTheme.typography.subtitle1)
                Text(text = "shares", style = MaterialTheme.typography.subtitle1)
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun HoldingsRowPreview() {
    ZygosTheme {
        Surface() {
            Column() {
                HoldingsRow(
                    ticker = "MSFT",
                    color = defaultTickerColors.getOrDefault("MSFT", Color.Blue),
                    shares = 27f,
                    value = 4567.32f,
                    subvalue = -1342.01f,
                    isSubvalueDollar = true,
                )

                Spacer(modifier = Modifier.padding(vertical = 12.dp))

                HoldingsRow(
                    ticker = "MSFT",
                    color = defaultTickerColors.getOrDefault("MSFT", Color.Blue),
                    shares = 27f,
                    value = 1357.32f,
                    subvalue = 0.1234f,
                    isSubvalueDollar = false,
                )
            }
        }
    }
}