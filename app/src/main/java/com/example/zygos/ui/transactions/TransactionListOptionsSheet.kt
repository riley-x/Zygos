package com.example.zygos.ui.transactions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

fun transactionsListOptionsSheet(
    currentSortOption: String,
    tickerFilter: MutableState<String>,
    typeFilter: MutableState<TransactionType>,
    isSortedAscending: Boolean,
    sortOptions: ImmutableList<String>,
    onSortOptionSelected: (String) -> Unit = { },
    onTickerChange: (String) -> Unit = { },
    onTypeChange: (TransactionType) -> Unit = { },
) : (@Composable ColumnScope.() -> Unit) =
    @Composable {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp)
        ) {
            Text(
                text = "Filter",
                style = MaterialTheme.typography.h5,
                modifier = Modifier
                    .padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = tickerFilter.value,
                onValueChange = onTickerChange,
                label = { Text("Ticker") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters,
                    autoCorrect = false,
                    imeAction = ImeAction.Done,
                ),
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth()
            )
            TransactionTypeSelector(
                type = typeFilter.value,
                onSelection = onTypeChange,
                modifier = Modifier
                    .padding(vertical = 8.dp, horizontal = 20.dp)
                    .fillMaxWidth()
            )

            Text(
                text = "Sorting",
                style = MaterialTheme.typography.h5,
                modifier = Modifier
                    .padding(bottom = 8.dp, top = 8.dp)
            )
            for ((index, opt) in sortOptions.items.withIndex()) {
                if (index > 0) ListOptionDivider()

                ListSortOptionRow(
                    text = opt,
                    isActive = (opt == currentSortOption),
                    isSortedAscending = isSortedAscending,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clickable { onSortOptionSelected(opt) }
                        .padding(horizontal = 20.dp)
                )
            }
        }
    }


@Preview(
    widthDp = 330,
    heightDp = 740,
    showBackground = true,
    backgroundColor = 0xFF666666,
)
@Composable
fun TransactionListOptionsSheet() {
    val viewModel = viewModel<TestViewModel>()
    ZygosTheme {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Bottom,
        ) {
            Surface {
                transactionsListOptionsSheet(
                    currentSortOption = transactionSortOptions.items[0],
                    isSortedAscending = true,
                    sortOptions = transactionSortOptions,
                    tickerFilter = viewModel.filterTicker,
                    typeFilter = viewModel.filterType,
                )(this)
            }
        }
    }
}