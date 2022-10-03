package com.example.zygos.ui.graphing

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.zygos.ui.components.ImmutableList
import com.example.zygos.ui.theme.ZygosTheme

/**
 * The time range selector at the bottom of the time series graph
 *
 * TODO: add plot style here too?
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TimeSeriesGraphSelector(
    options: ImmutableList<String>,
    currentSelection: State<String>, // must pass State here for derivedStateOf below
    modifier: Modifier = Modifier,
    onSelection: (String) -> Unit = { },
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        for (option in options.items) {
            // enabled would be recalculated for each button, but only 2 of them need to recompose
            val enabled by remember { derivedStateOf { option == currentSelection.value } }
            CustomTextButton(
                text = option,
                enabled = enabled,
                onSelection = onSelection,
            )
        }
    }
}

/**
 * The normal TextButton has too much padding.
 * Also, scoping this function helps prevent recomposition (otherwise ternaries
 * like
 *      if (enabled) MaterialTheme.colors.primary else MaterialTheme.colors.background
 * would trigger recomposition
 */
@Composable
fun CustomTextButton(
    text: String,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    colorBackground: Color = MaterialTheme.colors.primary,
    colorText: Color =  MaterialTheme.colors.onPrimary,
    colorBackgroundDisabled: Color = MaterialTheme.colors.background,
    colorTextDisabled: Color = MaterialTheme.colors.primary,
    onSelection: (String) -> Unit = { },
) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = if (enabled) colorBackground else colorBackgroundDisabled,
        contentColor = if (enabled) colorText else colorTextDisabled,
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(color = MaterialTheme.colors.primary),
            ) {
                onSelection(text)
            }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.button,
            )
        }
    }
}

@Preview
@Composable
fun PreviewTimeSeriesGraphSelector() {
    val options = ImmutableList(listOf("1m", "3m", "1y", "5y", "All"))
    val currentSelection = remember { mutableStateOf("1y") }
    ZygosTheme {
        Surface(modifier = Modifier.width(330.dp)) {
            TimeSeriesGraphSelector(
                options = options,
                currentSelection = currentSelection
            )
        }
    }
}