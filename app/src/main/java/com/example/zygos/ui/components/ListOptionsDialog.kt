package com.example.zygos.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.zygos.ui.holdings.holdingsListDisplayOptions
import com.example.zygos.ui.holdings.holdingsListSortOptions
import com.example.zygos.ui.theme.ZygosTheme
import com.example.zygos.viewModel.TestViewModel
import com.example.zygos.viewModel.transactionSortOptions


interface HasDisplayName {
    val displayName: String
}

@Composable
fun <T: HasDisplayName> ListOptionsDialog(
    currentDisplayOption: T,
    currentSortOption: T,
    currentSortIsAscending: Boolean,
    allDisplayOptions: ImmutableList<T>,
    allSortOptions: ImmutableList<T>,
    modifier: Modifier = Modifier,
    onDismiss: (isCancel: Boolean, displayOption: T, sortOption: T, sortIsAscending: Boolean) -> Unit = { _, _, _, _ -> },
) {
    var displayOption by remember { mutableStateOf(currentDisplayOption) }
    var sortOption by remember { mutableStateOf(currentSortOption) }
    var sortIsAscending by remember { mutableStateOf(currentSortIsAscending) }

    fun onCancel() = onDismiss(true, displayOption, sortOption, sortIsAscending)

    AlertDialog(
        onDismissRequest = ::onCancel,
        // don't use the title argument, it really messes with the layouts
        text = {
            Column {
                Text(
                    text = "Display",
                    style = MaterialTheme.typography.h5,
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                )
                DropdownSelector(
                    currentValue = displayOption,
                    allValues = allDisplayOptions,
                    onSelection = { displayOption = it },
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .fillMaxWidth()
                )

                Text(
                    text = "Sorting",
                    style = MaterialTheme.typography.h5,
                    modifier = Modifier
                        .padding(bottom = 8.dp, top = 8.dp)
                )
                DropdownSelector(
                    currentValue = sortOption,
                    allValues = allSortOptions,
                    onSelection = { sortOption = it },
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .fillMaxWidth()
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ascending",
                        style = MaterialTheme.typography.h5,
                        modifier = Modifier.weight(10f)
                    )
                    Switch(
                        checked = sortIsAscending,
                        onCheckedChange = { sortIsAscending = it }
                    )
                }
            }
        },
        buttons = {
            ConfirmationButtons(
                onCancel = ::onCancel,
                onOk = { onDismiss(false, displayOption, sortOption, sortIsAscending) },
            )
        },
        modifier = modifier,
    )

}


@Preview(
    widthDp = 330,
)
@Composable
fun PreviewListOptionsDialog() {
    val viewModel = viewModel<TestViewModel>()
    ZygosTheme {
        Surface {
            ListOptionsDialog(
                currentDisplayOption = holdingsListDisplayOptions.items[0],
                currentSortOption = holdingsListSortOptions.items[0],
                currentSortIsAscending = true,
                allDisplayOptions = holdingsListDisplayOptions,
                allSortOptions = holdingsListSortOptions,
            )
        }
    }
}