package com.example.zygos.ui.transactions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.zygos.data.Transaction
import com.example.zygos.ui.components.ListTitleBar
import com.example.zygos.ui.components.LogCompositions
import com.example.zygos.ui.components.TickerListDivider
import com.example.zygos.ui.components.recomposeHighlighter
import com.example.zygos.ui.holdings.HoldingsRow
import com.example.zygos.ui.theme.ZygosTheme
import com.example.zygos.viewModel.Position
import com.example.zygos.viewModel.TestViewModel

@Composable
fun TransactionsScreen(
    transactions: SnapshotStateList<Transaction>,
    modifier: Modifier = Modifier,
    accountBar: @Composable () -> Unit = { },
    onTransactionClick: (Position) -> Unit = { },
    transactionListOptionsCallback: () -> Unit = { },
) {
    LogCompositions("Zygos", "TransactionsScreen")
    // TODO: Use a floating button here for adding transactions?
    // And click transaction to edit/delete in a separate screen?
    // Sort by most recent first

    Column(
        modifier = modifier
            .fillMaxWidth(),
    ) {
        accountBar()

        Surface(
            modifier = Modifier
                .fillMaxSize()
        ) {
            LazyColumn {

                item("title") {
                    ListTitleBar(
                        text = "Transactions",
                        onOptionsButtonClick = transactionListOptionsCallback,
                        modifier = Modifier.padding(start = 22.dp)
                    )
                }

                itemsIndexed(transactions, key = { _, t -> t.id }) { // TODO is this key ok?
                    index, transaction ->
                    Column {
                        if (index > 0) Divider(
                            color = MaterialTheme.colors.onBackground.copy(alpha = 0.2f),
                            thickness = 1.dp,
                            modifier = modifier
                                .padding(2.dp)
                        )

                        TransactionRow(transaction)
                    }
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
            transactions = viewModel.transactions
        )
    }
}