package com.example.zygos.ui.settingsScreen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.zygos.data.database.Transaction
import com.example.zygos.ui.theme.ZygosTheme
import com.example.zygos.viewModel.TestViewModel

@Composable
fun TransactionCard(
    transactions: SnapshotStateList<Transaction>,
    tickerColors: SnapshotStateMap<String, Color>,
    modifier: Modifier = Modifier,
    onAddTransaction: () -> Unit = { },
    onTransactionClick: (Transaction) -> Unit = { },
    onTransactionSeeAll: () -> Unit = { },
) {
    Card(
        elevation = 1.dp,
        modifier = modifier
    ) {
        Column {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 10.dp)
            ) {
                Text(
                    text = "Transactions",
                    style = MaterialTheme.typography.h3,
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = onAddTransaction) {
                    Icon(
                        imageVector = Icons.Sharp.Add,
                        contentDescription = null,
                    )
                }
            }


            CardRowDivider(color = MaterialTheme.colors.primary)

            transactions.take(4).forEach { transaction ->
                TransactionTickerRow(
                    transaction = transaction,
                    tickerColors = tickerColors,
                    modifier = Modifier
                        .clickable { onTransactionClick(transaction) }
                        .padding(horizontal = 10.dp, vertical = 2.dp)
                )

                CardRowDivider()
            }

            TextButton(
                onClick = onTransactionSeeAll,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(horizontal = 4.dp)
                    .fillMaxWidth()
            ) {
                Text("SEE ALL")
            }
        }
    }
}


@Preview(
    widthDp = 360,
)
@Composable
fun PreviewTransactionCard() {
    val viewModel = viewModel<TestViewModel>()
    ZygosTheme {
        Surface {
            TransactionCard(
                transactions = viewModel.transactions,
                tickerColors = viewModel.tickerColors,
            )
        }
    }
}