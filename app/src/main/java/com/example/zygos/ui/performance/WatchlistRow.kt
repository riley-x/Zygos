package com.example.zygos.ui.performance

import androidx.compose.foundation.layout.Spacer
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.zygos.ui.components.TickerListRow
import com.example.zygos.ui.components.formatDollar
import com.example.zygos.ui.components.formatPercent
import com.example.zygos.ui.theme.ZygosTheme
import com.example.zygos.viewModel.Quote
import com.example.zygos.viewModel.TestViewModel

@Composable
fun WatchlistRow(
    quote: Quote,
    displayOption: String,
    modifier: Modifier = Modifier,
) {
    TickerListRow(
        ticker = quote.ticker,
        color = quote.color,
        modifier = modifier
    ) {
        Spacer(Modifier.weight(10f))
        val color = if (quote.change > 0) MaterialTheme.colors.primary
        else if (quote.change == 0f) MaterialTheme.colors.onSurface
        else MaterialTheme.colors.error

        when (displayOption) {
            "Change" -> Text(
                formatDollar(quote.change),
                color = color
            )
            "% Change" -> Text(
                formatPercent(quote.percentChange),
                color = color
            )
            else -> Text(
                formatDollar(quote.price),
                color = color
            )
        }
    }
}


@Preview
@Composable
fun PreviewWatchlistRow() {
    val viewModel = viewModel<TestViewModel>()
    ZygosTheme {
        Surface {
            WatchlistRow(quote = viewModel.watchlist[0], displayOption = "Change")
        }
    }
}