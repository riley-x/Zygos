package com.example.zygos.ui.settingsScreen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.zygos.data.database.Transaction
import com.example.zygos.network.ApiService
import com.example.zygos.ui.components.*
import com.example.zygos.ui.theme.ZygosTheme
import com.example.zygos.viewModel.TestViewModel

@Composable
fun SettingsScreen(
    apiKeys: SnapshotStateMap<String, String>,
    accounts: SnapshotStateList<String>,
    transactions: SnapshotStateList<Transaction>,
    tickerColors: SnapshotStateMap<String, Color>,
    modifier: Modifier = Modifier,
    bottomPadding: Dp = 0.dp,
    onAddAccount: () -> Unit = { },
    onApiKeyClick: (ApiService) -> Unit = { },
    onAddTransaction: () -> Unit = { },
    onTransactionClick: (Transaction) -> Unit = { },
    onTransactionSeeAll: () -> Unit = { },
    onBackupDatabase: () -> Unit = { },
    accountSelectionBar: @Composable () -> Unit = { },
) {
    LogCompositions("Zygos", "AnalyticsScreen")
    // TODO: Maybe this screen is a good place for dividend and option summaries

    Column(modifier.padding(bottom = bottomPadding)) {
        accountSelectionBar()

        LazyColumn {

            item(key = "api_keys") {
                ApiKeysCard(
                    apiKeys = apiKeys,
                    onApiKeyClick = onApiKeyClick,
                    modifier = Modifier.padding(10.dp)
                )
            }

            item(key = "accounts") {
                AccountsCard(
                    accounts = accounts,
                    onAddAccount = onAddAccount,
                    modifier = Modifier.padding(10.dp)
                )
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

            item(key = "backup database") {
                BackupDatabaseCard(
                    onClick = onBackupDatabase,
                    modifier = Modifier.padding(10.dp)
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
                accounts = viewModel.accounts,
                apiKeys = viewModel.apiKeys,
                transactions = viewModel.transactions,
                tickerColors = viewModel.tickerColors,
            )
        }
    }
}