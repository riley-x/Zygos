package com.example.zygos.ui.settingsScreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.zygos.data.database.Transaction
import com.example.zygos.data.toFloatDollar
import com.example.zygos.ui.components.TickerListRow
import com.example.zygos.ui.components.formatDateInt
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
        modifier = modifier
    ) {
        Spacer(Modifier.weight(5f))

        Column(Modifier.weight(15f)) {
            Text(text = transaction.type.toString())
            Text(text = formatDateInt(transaction.date))
        }

        Column(Modifier.weight(15f), horizontalAlignment = Alignment.End) {
            Text(
                text = formatDollar(transaction.value.toFloatDollar()),
                style = MaterialTheme.typography.h4,
            )
            Text(
                text = formatDollar(transaction.price.toFloatDollar()),
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