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
fun TransactionTypeSelector(type: MutableState<TransactionType>, modifier: Modifier = Modifier) {
    var typeExpanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = typeExpanded,
        onExpandedChange = {
            typeExpanded = !typeExpanded
        },
        modifier = modifier
    ) {
        OutlinedTextField(
            readOnly = true,
            value = type.value.name,
            onValueChange = { type.value = TransactionType.valueOf(it) },
            label = { Text("Type") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = typeExpanded
                )
            },
            modifier = Modifier.fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = typeExpanded,
            onDismissRequest = {
                typeExpanded = false
            }
        ) {
            TransactionType.values().forEach {
                DropdownMenuItem(
                    onClick = {
                        type.value = it
                        typeExpanded = false
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
    val type = remember { mutableStateOf(TransactionType.TRANSFER) }
    ZygosTheme {
        Surface {
            TransactionTypeSelector(type)
        }
    }
}