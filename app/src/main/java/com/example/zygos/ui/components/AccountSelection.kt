package com.example.zygos.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.zygos.ui.theme.ZygosTheme

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AccountSelection(
    currentAccount: String,
    accounts: List<String>,
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
            modifier = modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(currentAccount,
                    style = MaterialTheme.typography.h4,
                    modifier = Modifier
                    //    .padding(start = 10.dp),
                )
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded,
                )
            }

//        TextField(
//            readOnly = true,
//            value = selectedOptionText,
//            onValueChange = { /* TODO */ },
//            //label = { Text("Account") },
//            trailingIcon = {
//                ExposedDropdownMenuDefaults.TrailingIcon(
//                    expanded = expanded
//                )
//            },
//            colors = ExposedDropdownMenuDefaults.textFieldColors(
//                textColor = MaterialTheme.colors.onBackground,
//                backgroundColor = MaterialTheme.colors.background,
//                unfocusedLabelColor = MaterialTheme.colors.primary,
//            ),
//            modifier = Modifier.fillMaxWidth()
//        )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = {
                    expanded = false
                }
            ) {
                accounts.forEach { selectionOption ->
                    DropdownMenuItem(
                        onClick = {
                            onAccountSelected(selectionOption)
                            expanded = false
                        }
                    ) {
                        Text(text = selectionOption)
                    }
                }
            }
        }
    }
}



@Preview(showBackground = true)
@Composable
fun AccountSelectionPreview() {
    val accounts = listOf("Robinhood", "Arista", "TD Ameritrade", "Alhena")
    ZygosTheme {
        AccountSelection(
            accounts = accounts,
            currentAccount = accounts[0],
        )
    }
}