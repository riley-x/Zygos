package com.example.zygos.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.zygos.ui.theme.ZygosTheme

/**
 * The time range selector at the bottom of the time series graph
 *
 * TODO: add plot style here too?
 */
@Composable
fun TimeSeriesGraphSelector(
    options: SnapshotStateList<String>,
    currentSelection: String,
    modifier: Modifier = Modifier,
    onSelection: (String) -> Unit = { },
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        for (option in options) {
            val enabled = option == currentSelection
            TextButton(
                enabled = enabled,
                onClick = { onSelection(option) },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = MaterialTheme.colors.primary,
                    contentColor = MaterialTheme.colors.onPrimary,
                    disabledBackgroundColor = MaterialTheme.colors.background,
                    disabledContentColor = MaterialTheme.colors.onBackground,
                )
            ) {
                Text(option)
            }
        }
    }
}

@Preview
@Composable
fun PreviewTimeSeriesGraphSelector() {
    val options = remember { mutableStateListOf("1m", "3m", "1y", "5y", "All") }
    val currentSelection = "1y"
    ZygosTheme {
        Surface {
            TimeSeriesGraphSelector(
                options = options,
                currentSelection = currentSelection
            )
        }
    }
}