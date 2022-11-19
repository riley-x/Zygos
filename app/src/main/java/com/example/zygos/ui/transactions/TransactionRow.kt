package com.example.zygos.ui.transactions

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.zygos.data.database.Transaction
import com.example.zygos.ui.components.formatDateInt
import com.example.zygos.ui.components.formatDollar
import com.example.zygos.ui.theme.ZygosTheme
import com.example.zygos.viewModel.TestViewModel

@Composable
fun TransactionRow(
    transaction: Transaction,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
    ) {
        val style = MaterialTheme.typography.body2

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp)
        ) {
            Text(text = formatDateInt(transaction.date), style = style, modifier = Modifier.weight(1f))
            Text(text = transaction.account, style = style, modifier = Modifier.weight(2f))
            Text(text = transaction.type.name, style = style, modifier = Modifier.weight(2f))
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp)
        ) {
            Text(text = transaction.ticker, style = style, modifier = Modifier.weight(1f))
            Text(text = transaction.shares.toString(), style = style, modifier = Modifier.weight(1f))
            Text(text = formatDollar(transaction.price / 10000f), style = style, modifier = Modifier.weight(1.5f))
            Text(text = formatDollar(transaction.value / 10000f), style = style, modifier = Modifier.weight(1.5f))
        }

        if (transaction.expiration > 0 || transaction.strike > 0) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
            ) {
                Text(
                    text = if (transaction.expiration > 0) formatDateInt(transaction.expiration) else "",
                    style = style,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = if (transaction.strike > 0) formatDollar(transaction.strike / 10000f) else "",
                    style = style,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.weight(3f))
            }
        }
    }
}


@Preview(widthDp = 360)
@Composable
fun PreviewTransactionRow() {
    val viewModel = viewModel<TestViewModel>()
    ZygosTheme {
        Surface {
            Column {
                for (transaction in viewModel.transactions.slice(0..2)) {
                    TransactionRow(
                        transaction = transaction,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                }
            }
        }
    }
}