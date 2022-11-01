package com.example.zygos.ui.settingsScreen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.zygos.network.ApiService
import com.example.zygos.network.apiServices
import com.example.zygos.ui.components.allAccounts
import com.example.zygos.ui.components.noAccountMessage
import com.example.zygos.ui.theme.ZygosTheme
import com.example.zygos.viewModel.TestViewModel

@Composable
internal fun AccountsCard(
    accounts: SnapshotStateList<String>,
    modifier: Modifier = Modifier,
    onAddAccount: () -> Unit = { },
) {
    Card(
        elevation = 1.dp,
        modifier = modifier,
    ) {
        Column {
            CardTitle(title = "Accounts") {
                IconButton(onClick = onAddAccount) {
                    Icon(
                        imageVector = Icons.Sharp.Add,
                        contentDescription = null,
                    )
                }
            }

            CardRowDivider(color = MaterialTheme.colors.primary)

            accounts.filter { it != allAccounts && it != noAccountMessage }
                .forEachIndexed { i, account ->
                if (i > 0) CardRowDivider()

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .heightIn(min = 40.dp)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(account)
                }
            }
        }
    }
}


@Preview(
    widthDp = 360,
)
@Composable
private fun Preview() {
    val viewModel = viewModel<TestViewModel>()
    ZygosTheme {
        Surface {
            AccountsCard(
                accounts = viewModel.accounts
            )
        }
    }
}