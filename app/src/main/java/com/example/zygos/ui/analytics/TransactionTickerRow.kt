package com.example.zygos.ui.analytics

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.zygos.data.database.Transaction
import com.example.zygos.ui.components.TickerListRow
import com.example.zygos.ui.components.formatDollar
import com.example.zygos.ui.theme.ZygosTheme
import com.example.zygos.viewModel.TestViewModel


/**
 * Display transactions as a ticker row, with color coded ticker bands. This element only shows a
 * subset of the transaction info.
 */
@Composable
fun TransactionTickerRow(
    transaction: Transaction,
    tickerColors: SnapshotStateMap<String, Color>,
    modifier: Modifier = Modifier,
) {
    TickerListRow(
        ticker = transaction.ticker,
        color = tickerColors.getOrDefault(transaction.ticker, Color.Transparent),
        tickerWeight = 10f,
        modifier = modifier
    ) {
        Column(Modifier.weight(15f)) {
            Text(text = transaction.type.name)
            Text(text = transaction.shares.toString())
        }

        Column(Modifier.weight(15f), horizontalAlignment = Alignment.End) {
            Text(text = formatDollar(transaction.value / 10000f))
            Text(
                text = formatDollar(transaction.price / 10000f),
                style = MaterialTheme.typography.subtitle1,
            )
        }
    }
}

@Preview
@Composable
fun PreviewTransactionTickerRow() {
    val viewModel = viewModel<TestViewModel>()
    ZygosTheme {
        Surface {
            TransactionTickerRow(
                transaction = viewModel.transactions.first(),
                tickerColors = viewModel.tickerColors,
            )
        }
    }
}