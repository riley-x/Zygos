package com.example.zygos.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.zygos.ui.theme.ZygosTheme

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AccountSelection(
    currentAccount: String,
    accounts: SnapshotStateList<String>,
    modifier: Modifier = Modifier,
    onAccountSelected: (String) -> Unit = { },
) {
    var expanded by remember { mutableStateOf(false) }

    // We want to react on tap/press on TextField to show menu
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            expanded = !expanded
        },
        modifier = modifier.fillMaxWidth()
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(currentAccount,
                    style = MaterialTheme.typography.h2,
                    modifier = Modifier
                    //    .padding(start = 10.dp),
                )
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded,
                )
            }

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = {
                    expanded = false
                }
            ) {
                accounts.forEach { account ->
                    if (account == "All Accounts") {
                        Divider(
                            color = MaterialTheme.colors.onBackground.copy(alpha = 0.2f),
                            thickness = 1.dp,
                            modifier = Modifier
                                .padding(horizontal = 0.dp)
                        )
                    }

                    DropdownMenuItem(
                        onClick = {
                            onAccountSelected(account)
                            expanded = false
                        }
                    ) {
                        Text(text = account)
                    }
                }
            }
        }
    }
}



@Preview(showBackground = true)
@Composable
fun AccountSelectionPreview() {
    val accounts = remember { mutableStateListOf(
        "Robinhood", "Arista", "TD Ameritrade", "Alhena", "All Accounts"
    ) }
    ZygosTheme {
        AccountSelection(
            accounts = accounts,
            currentAccount = accounts[0],
        )
    }
}