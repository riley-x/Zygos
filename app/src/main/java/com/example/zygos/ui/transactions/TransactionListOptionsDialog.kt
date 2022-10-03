package com.example.zygos.ui.transactions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.zygos.data.database.TransactionType
import com.example.zygos.ui.components.ImmutableList
import com.example.zygos.ui.components.ListOptionDivider
import com.example.zygos.ui.components.ListSortOptionRow
import com.example.zygos.ui.theme.ZygosTheme
import com.example.zygos.viewModel.TestViewModel
import com.example.zygos.viewModel.transactionSortOptions

@Composable
fun TransactionsListOptionsDialog(
    currentSortOption: String,
    isSortedAscending: Boolean,
    sortOptions: ImmutableList<String>,
    modifier: Modifier = Modifier,
    onSortOptionSelected: (String) -> Unit = { },
    onDismiss: (isCancel: Boolean, String, TransactionType) -> Unit = { _, _, _ -> },
) {
    val focusManager = LocalFocusManager.current

    var ticker by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(TransactionType.NONE) }

    AlertDialog(
        onDismissRequest = { onDismiss(true, "", TransactionType.NONE) },
        // don't use the title argument, it really messes with the layouts
        text = {
            Column {
                Text(
                    text = "Filter",
                    style = MaterialTheme.typography.h5,
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = ticker,
                    onValueChange = { ticker = it },
                    label = { Text("Ticker") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters,
                        autoCorrect = false,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
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
                for ((index, opt) in sortOptions.items.withIndex()) {
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
            Row(
                modifier = Modifier
                    .padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { onDismiss(true, "", TransactionType.NONE) },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = MaterialTheme.colors.error,
                    )
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = { onDismiss(false, ticker, type) },
                ) {
                    Text("OK")
                }
            }
        },
        modifier = modifier,
    )

}


@Preview(
    widthDp = 330,
)
@Composable
fun PreviewTransactionListOptionsDialog() {
    val viewModel = viewModel<TestViewModel>()
    ZygosTheme {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Bottom,
        ) {
            Surface {
                TransactionsListOptionsDialog(
                    currentSortOption = transactionSortOptions.items[0],
                    isSortedAscending = true,
                    sortOptions = transactionSortOptions,
                )
            }
        }
    }
}