package com.example.zygos.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.AddCircle
import androidx.compose.material.icons.sharp.AddCircleOutline
import androidx.compose.material.icons.sharp.PlusOne
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.zygos.ui.theme.ZygosTheme


const val noAccountMessage = "No Accounts"
const val allAccounts = "All Accounts"

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AccountSelection(
    currentAccount: String,
    accounts: SnapshotStateList<String>,
    modifier: Modifier = Modifier,
    onAccountSelected: (String) -> Unit = { },
    onAddAccount: () -> Unit = { },
) {
    var expanded by remember { mutableStateOf(false) }

    val noAccounts by remember { derivedStateOf {
        accounts.isEmpty() || accounts[0] == noAccountMessage
    } }
    fun onExpandedChange(newExpanded: Boolean) {
        if (!noAccounts) expanded = !expanded
    }

    Surface(
        modifier = modifier.fillMaxWidth()
    ) {
        Row (
            verticalAlignment = Alignment.CenterVertically
        ) {
            // We want to react on tap/press on TextField to show menu
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = ::onExpandedChange,
                modifier = Modifier.weight(1f)
            ) {

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        currentAccount,
                        style = MaterialTheme.typography.h2,
                        color = if (noAccounts) MaterialTheme.colors.onSurface.copy(ContentAlpha.disabled)
                        else MaterialTheme.colors.onSurface,
                        modifier = Modifier
                        //    .padding(start = 10.dp),
                    )
                    if (!noAccounts) {
                        ExposedDropdownMenuDefaults.TrailingIcon(
                            expanded = expanded,
                        )
                    }
                }

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = {
                        expanded = false
                    }
                ) {
                    accounts.forEach { account ->
                        if (account == allAccounts) {
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

            IconButton(onClick = onAddAccount) {
                Icon(
                    imageVector = Icons.Sharp.AddCircleOutline,
                    contentDescription = null,
                )
            }
        }
    }
}



@Preview
@Composable
fun AccountSelectionPreview() {
    val accounts = remember { mutableStateListOf(
        "Robinhood", "Arista", "TD Ameritrade", "Alhena", "All Accounts"
    ) }
    val accounts2 = remember { mutableStateListOf(
        "No Accounts"
    ) }
    ZygosTheme {
        Column {
            AccountSelection(
                accounts = accounts,
                currentAccount = accounts[0],
            )
            Spacer(modifier = Modifier.height(20.dp))
            AccountSelection(
                accounts = accounts2,
                currentAccount = accounts2[0],
            )
        }
    }
}