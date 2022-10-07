package com.example.zygos.ui.transactions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.zygos.data.database.Transaction
import com.example.zygos.data.database.TransactionType
import com.example.zygos.ui.components.ListTitleBar
import com.example.zygos.ui.components.LogCompositions
import com.example.zygos.ui.theme.ZygosTheme
import com.example.zygos.viewModel.TestViewModel

@Composable
fun TransactionsScreen(
    transactions: SnapshotStateList<Transaction>,
    currentFilterTicker: String,
    currentFilterType: TransactionType,
    modifier: Modifier = Modifier,
    onTransactionClick: (Transaction) -> Unit = { },
    transactionsListOptionsCallback: () -> Unit = { },
) {
    LogCompositions("Zygos", "TransactionsScreen")
    // TODO: Use a floating button here for adding transactions
    // TODO: Click transaction to edit/delete in a separate screen

    Column {
        Row {
            Text("Filters:", modifier = Modifier.weight(10f))
            Box(contentAlignment = Alignment.Center, modifier = Modifier.weight(10f)) {
                Text(currentFilterTicker.ifEmpty { "All Tickers" })
            }
            Box(contentAlignment = Alignment.CenterEnd, modifier = Modifier.weight(10f)) {
                if (currentFilterType != TransactionType.NONE)
                    Text(currentFilterType.displayName)
            }
        }
        ListTitleBar(
            text = "Transactions",
            onOptionsButtonClick = transactionsListOptionsCallback,
            modifier = Modifier.padding(start = 22.dp)
        )

        LazyColumn {

            itemsIndexed(transactions, key = { _, t -> t.transactionId }) {
                index, transaction ->
                Column {
                    if (index > 0) Divider(
                        color = MaterialTheme.colors.onBackground.copy(alpha = 0.2f),
                        thickness = 1.dp,
                        modifier = modifier
                            .clickable { onTransactionClick(transaction) }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )

                    TransactionRow(transaction, modifier = Modifier.padding(horizontal = 4.dp))
                }
            }
        }
    }
}


@Preview(
    widthDp = 360,
    heightDp = 740,
    showBackground = true,
)
@Composable
fun PreviewTransactionsScreen() {
    val viewModel = viewModel<TestViewModel>()
    ZygosTheme {
        TransactionsScreen(
            transactions = viewModel.transactions,
            currentFilterTicker = "MSFT",
            currentFilterType = TransactionType.STOCK,
        )
    }
}