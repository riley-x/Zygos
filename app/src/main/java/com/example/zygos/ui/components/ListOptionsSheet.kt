package com.example.zygos.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.zygos.ui.holdings.holdingsListDisplayOptions
import com.example.zygos.ui.holdings.holdingsListSortOptions
import com.example.zygos.ui.theme.ZygosTheme

fun listOptionsSheet(
    currentSortOption: String,
    currentDisplayOption: String,
    isSortedAscending: Boolean,
    displayOptions: List<String>,
    sortOptions: List<String>,
    onDisplayOptionSelected: (String) -> Unit = { },
    onSortOptionSelected: (String) -> Unit = { },
) : (@Composable ColumnScope.() -> Unit) =
    @Composable {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp)
        ) {
            Text(
                text = "Display",
                style = MaterialTheme.typography.h5,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 8.dp)
            )
            for ((index, opt) in displayOptions.withIndex()) {
                if (index > 0) ListOptionDivider()

                ListDisplayOptionRow(
                    text = opt,
                    isActive = (opt == currentDisplayOption),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clickable { onDisplayOptionSelected(opt) }
                        .padding(horizontal = 20.dp)
                )
            }

            Text(
                text = "Sorting",
                style = MaterialTheme.typography.h5,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 8.dp, top = 8.dp)
            )
            for ((index, opt) in sortOptions.withIndex()) {
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
fun PreviewHoldingsListOptionsSheet() {
    ZygosTheme {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Bottom,
        ) {
            Surface {
                listOptionsSheet(
                    currentSortOption = holdingsListSortOptions[0],
                    currentDisplayOption = holdingsListDisplayOptions[0],
                    isSortedAscending = true,
                    sortOptions = holdingsListSortOptions,
                    displayOptions = holdingsListDisplayOptions,
                )(this)
            }
        }
    }
}