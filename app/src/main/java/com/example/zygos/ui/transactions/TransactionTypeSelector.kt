package com.example.zygos.ui.transactions

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.zygos.data.database.TransactionType
import com.example.zygos.ui.theme.ZygosTheme

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TransactionTypeSelector(
    type: TransactionType,
    modifier: Modifier = Modifier,
    onSelection: (TransactionType) -> Unit = { },
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
            value = type.name,
            onValueChange = {  },
            label = { Text("Type") },
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
            TransactionType.values().forEach {
                DropdownMenuItem(
                    onClick = {
                        onSelection(it)
                        expanded = false
                    }
                ) {
                    Text(text = it.name)
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewTransactionTypeSelector() {
    ZygosTheme {
        Surface {
            TransactionTypeSelector(TransactionType.TRANSFER)
        }
    }
}