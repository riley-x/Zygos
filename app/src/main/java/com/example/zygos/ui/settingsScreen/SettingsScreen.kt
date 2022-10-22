package com.example.zygos.ui.settingsScreen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.Composable
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
import com.example.zygos.network.ApiService
import com.example.zygos.network.apiServices
import com.example.zygos.ui.components.*
import com.example.zygos.ui.theme.ZygosTheme
import com.example.zygos.viewModel.TestViewModel

@Composable
fun SettingsScreen(
    transactions: SnapshotStateList<Transaction>,
    tickerColors: SnapshotStateMap<String, Color>,
    apiKeys: SnapshotStateMap<String, String>,
    modifier: Modifier = Modifier,
    bottomPadding: Dp = 0.dp,
    onApiKeyClick: (ApiService) -> Unit = { },
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

            item(key = "api_keys") {
                Card(
                    elevation = 1.dp,
                    modifier = Modifier.padding(10.dp)
                ) {
                    Column() {

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(start = 10.dp)
                                .heightIn(min = 48.dp)
                        ) {
                            Text("API Keys", style = MaterialTheme.typography.h3)
                        }

                        CardRowDivider(color = MaterialTheme.colors.primary)

                        apiServices.forEachIndexed { i, service ->
                            if (i > 0) CardRowDivider()

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .heightIn(min = 40.dp)
                                    .clickable { onApiKeyClick(service) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(service.name, modifier = Modifier.weight(10f))
                                val key = apiKeys.getOrDefault(service.name, "").takeLast(4)
                                if (key.isNotBlank())
                                    Text("••••$key", style = MaterialTheme.typography.subtitle2)
                            }
                        }
                    }

                }
            }

            item(key = "transactions") {
                TransactionCard(
                    transactions = transactions,
                    tickerColors = tickerColors,
                    onAddTransaction = onAddTransaction,
                    onTransactionClick = onTransactionClick,
                    onTransactionSeeAll = onTransactionSeeAll,
                    modifier = Modifier.padding(10.dp),
                )
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
fun PreviewSettingsScreen() {
    val viewModel = viewModel<TestViewModel>()
    ZygosTheme {
        Surface {
            SettingsScreen(
                apiKeys = viewModel.apiKeys,
                transactions = viewModel.transactions,
                tickerColors = viewModel.tickerColors,
            )
        }
    }
}