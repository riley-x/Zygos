package com.example.zygos.ui.analytics

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.Add
import androidx.compose.material.icons.sharp.AddCircleOutline
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.zygos.data.database.Transaction
import com.example.zygos.ui.components.*
import com.example.zygos.ui.theme.ZygosTheme
import com.example.zygos.viewModel.TestViewModel
import kotlinx.coroutines.channels.ticker

@Composable
fun AnalyticsScreen(
    transactions: SnapshotStateList<Transaction>,
    tickerColors: SnapshotStateMap<String, Color>,
    modifier: Modifier = Modifier,
    bottomPadding: Dp = 0.dp,
    onAddTransaction: () -> Unit = { },
    onTransactionClick: (Transaction) -> Unit = { },
    onTransactionSeeAll: () -> Unit = { },
    accountSelectionBar: @Composable () -> Unit = { },
) {
    LogCompositions("Zygos", "AnalyticsScreen")
    // TODO: Maybe this screen is a good place for dividend and option summaries

    Column(modifier.padding(bottomPadding)) {
        accountSelectionBar()

        LazyColumn {

            item(key = "transactions") {
                Card(
                    elevation = Dp(0.5f),
                    modifier = Modifier.padding(6.dp)
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


                        Divider(
                            color = MaterialTheme.colors.primary,
                            thickness = 1.dp,
                            modifier = Modifier
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )

                        transactions.take(4).forEach { transaction ->
                            TransactionTickerRow(
                                transaction = transaction,
                                tickerColors = tickerColors,
                                modifier = Modifier
                                    .clickable { onTransactionClick(transaction) }
                                    .padding(horizontal = 10.dp, vertical = 2.dp)
                            )

                            Divider(
                                color = MaterialTheme.colors.onBackground.copy(alpha = 0.2f),
                                thickness = 1.dp,
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                            )
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
        }
    }
}


@Preview(
    widthDp = 360,
    heightDp = 740,
    showBackground = true,
)
@Composable
fun PreviewAnalyticsScreen() {
    val viewModel = viewModel<TestViewModel>()
    ZygosTheme {
        Surface {
            AnalyticsScreen(
                transactions = viewModel.transactions,
                tickerColors = viewModel.tickerColors,
            )
        }
    }
}