package com.example.zygos.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.zygos.data.database.TransactionType
import com.example.zygos.ui.theme.ZygosTheme
import com.example.zygos.ui.transactions.TransactionTypeSelector
import com.example.zygos.viewModel.TestViewModel

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AccountSelector(
    account: MutableState<String>,
    accounts: SnapshotStateList<String>,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            expanded = !expanded
        },
        modifier = modifier
    ) {
        OutlinedTextField(
            readOnly = true,
            value = account.value,
            onValueChange = { account.value = it },
            label = { Text("Account") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded
                )
            },
            modifier = Modifier.fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            }
        ) {
            accounts.forEach {
                DropdownMenuItem(
                    onClick = {
                        account.value = it
                        expanded = false
                    }
                ) {
                    Text(text = it)
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewAccountSelector() {
    val account = remember { mutableStateOf("Robinhood") }
    val viewModel = viewModel<TestViewModel>()
    ZygosTheme {
        Surface {
            AccountSelector(account, accounts = viewModel.accounts)
        }
    }
}