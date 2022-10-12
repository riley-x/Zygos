package com.example.zygos.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.Check
import androidx.compose.material.icons.sharp.North
import androidx.compose.material.icons.sharp.South
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.zygos.ui.theme.ZygosTheme


/**
 * Composables for displaying the options menu on sortable lists
 */

@Composable
fun ListSortOptionRow(
    text: String,
    isActive: Boolean,
    isSortedAscending: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.heightIn(min = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text, modifier = Modifier.weight(1f))
        if (isActive) {
            if (isSortedAscending) {
                Icon(
                    Icons.Sharp.North,
                    contentDescription = null
                )
            } else {
                Icon(
                    Icons.Sharp.South,
                    contentDescription = null
                )
            }
        }
    }
}

@Composable
fun ListDisplayOptionRow(
    text: String,
    isActive: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.heightIn(min = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text, modifier = Modifier.weight(1f))
        if (isActive) {
            Icon(
                Icons.Sharp.Check,
                contentDescription = null
            )
        }
    }
}


@Composable
fun ListOptionDivider() {
    Divider(
        color = MaterialTheme.colors.onBackground.copy(alpha = 0.2f),
        thickness = 1.dp,
        modifier = Modifier
            .padding(horizontal = 20.dp)
    )
}


@Preview
@Composable
fun PreviewListOptionRow() {
    ZygosTheme {
        Surface {
            Column {
                ListSortOptionRow(text = "Sort Up", isActive = true, isSortedAscending = true)
                ListOptionDivider()
                ListSortOptionRow(text = "Sort Down", isActive = true, isSortedAscending = false)
                ListOptionDivider()
                ListSortOptionRow(text = "Sort Off", isActive = false, isSortedAscending = true)

                Spacer(modifier = Modifier.padding(vertical = 20.dp))

                ListDisplayOptionRow(text = "Display On", isActive = true)
                ListOptionDivider()
                ListDisplayOptionRow(text = "Display Off", isActive = false)
            }
        }
    }
}