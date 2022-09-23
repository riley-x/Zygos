package com.example.zygos.ui.holdings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.zygos.ui.components.PieChart
import com.example.zygos.ui.components.formatDollar
import com.example.zygos.ui.theme.ZygosTheme

@Composable
fun HoldingsRow(
    ticker: String,
    color: Color,
    shares: Float,
    value: Float,
    gain: Float,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
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

        Spacer(Modifier.width(12.dp))

        Column(Modifier) {
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                Text(text = "$shares", style = typography.subtitle1)
                Text(text = "shares", style = typography.subtitle1)
            }
        }

        Spacer(Modifier.weight(1f))

        Column(Modifier, horizontalAlignment = Alignment.End) {
            Text(text = formatDollar(value), style = typography.body1)
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                Text(text = formatDollar(gain),
                    style = typography.subtitle1,
                    color = if (gain >= 0) MaterialTheme.colors.onSurface else MaterialTheme.colors.error
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun HoldingsRowPreview() {
    ZygosTheme {
        Surface() {
            HoldingsRow(
                ticker = "MSFT",
                color = Color(0xFF0000FF),
                shares = 27f,
                value = 4567.32f,
                gain = -1342.01f,
            )
        }
    }
}