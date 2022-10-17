package com.example.zygos.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.zygos.ui.holdings.HoldingsListSortOptions
import com.example.zygos.ui.holdings.holdingsListSortOptions
import com.example.zygos.ui.theme.ZygosTheme

/**
 * Wrapper class for a read-only ExposedDropdownMenu
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun <T> DropdownSelectorHelper(
    toString: (T) -> String,
    currentValue: T,
    allValues: ImmutableList<T>,
    modifier: Modifier = Modifier,
    label: String = "",
    onSelection: (T) -> Unit = { },
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            readOnly = true,
            value = toString(currentValue),
            onValueChange = {  },
            label = { if (label.isNotBlank()) Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded
                )
            },
            modifier = Modifier.fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            allValues.items.forEach {
                DropdownMenuItem(
                    onClick = {
                        onSelection(it)
                        expanded = false
                    }
                ) {
                    Text(text = toString(it))
                }
            }
        }
    }
}


@Composable
fun <T: HasDisplayName> DropdownSelector(
    currentValue: T,
    allValues: ImmutableList<T>,
    modifier: Modifier = Modifier,
    label: String = "",
    onSelection: (T) -> Unit = { },
) {
    DropdownSelectorHelper(
        toString = { it.displayName },
        currentValue = currentValue,
        allValues = allValues,
        modifier = modifier,
        label = label,
        onSelection = onSelection,
    )
}

@Composable
fun DropdownSelector(
    currentValue: String,
    allValues: ImmutableList<String>,
    modifier: Modifier = Modifier,
    label: String = "",
    onSelection: (String) -> Unit = { },
) {
    DropdownSelectorHelper(
        toString = { it },
        currentValue = currentValue,
        allValues = allValues,
        modifier = modifier,
        label = label,
        onSelection = onSelection,
    )
}





@Preview
@Composable
fun PreviewDropdownSelector() {
    ZygosTheme {
        Surface {
            DropdownSelector(
                currentValue = HoldingsListSortOptions.EQUITY,
                allValues = holdingsListSortOptions,
            )
        }
    }
}