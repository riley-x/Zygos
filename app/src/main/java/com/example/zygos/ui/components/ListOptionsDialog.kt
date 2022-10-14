package com.example.zygos.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.zygos.data.database.TransactionType
import com.example.zygos.ui.theme.ZygosTheme
import com.example.zygos.ui.transactions.TransactionTypeSelector
import com.example.zygos.viewModel.TestViewModel
import com.example.zygos.viewModel.transactionSortOptions


interface HasDisplayName {
    val displayName: String
}


@Composable
fun <T: HasDisplayName> HoldingsListOptionsDialog(
    currentDisplayOption: T,
    currentSortOption: T,
    currentSortIsAscending: Boolean,
    allDisplayOptions: ImmutableList<T>,
    allSortOptions: ImmutableList<T>,
    modifier: Modifier = Modifier,
    onDismiss: (isCancel: Boolean, displayOption: T, sortOption: T, sortIsAscending: Boolean ) -> Unit = { _, _, _, _ -> },
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
                TransactionTypeSelector(
                    type = type,
                    onSelection = { type = it },
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
                for ((index, opt) in allSortOptions.items.withIndex()) {
                    if (index > 0) Divider(
                        color = MaterialTheme.colors.onBackground.copy(alpha = 0.2f),
                        thickness = 1.dp,
                    )

                    ListSortOptionRow(
                        text = opt,
                        isActive = (opt == currentSortOption),
                        isSortedAscending = isSortedAscending,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clickable { onSortOptionSelected(opt) }
                    )
                }
            }
        },
        buttons = {
            ConfirmationButtons(
                onCancel = { onDismiss(true, "", TransactionType.NONE) },
                onOk = { onDismiss(false, ticker, type) },
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
                currentSortOption = transactionSortOptions.items[0],
                isSortedAscending = true,
                sortOptions = transactionSortOptions,
            )
        }
    }
}