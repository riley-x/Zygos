package com.example.zygos.ui.components

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
import com.example.zygos.ui.theme.ZygosTheme

/**
 * The time range selector at the bottom of the time series graph
 *
 * TODO: add plot style here too?
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TimeSeriesGraphSelector(
    options: SnapshotStateList<String>,
    currentSelection: State<String>,
    modifier: Modifier = Modifier,
    onSelection: (String) -> Unit = { },
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        for (option in options) {
            val enabled by remember { derivedStateOf { option == currentSelection.value } }
            CustomTextButton(
                text = option,
                enabled = enabled,
                onSelection = onSelection,
            )
        }
    }
}


@Composable
fun CustomTextButton(
    text: String,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    colorBackground: Color = MaterialTheme.colors.primary,
    colorText: Color =  MaterialTheme.colors.onPrimary,
    colorBackgroundDisabled: Color = MaterialTheme.colors.background,
    colorTextDisabled: Color = MaterialTheme.colors.onBackground,
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
            Text(text)
        }
    }
}

@Preview
@Composable
fun PreviewTimeSeriesGraphSelector() {
    val options = remember { mutableStateListOf("1m", "3m", "1y", "5y", "All") }
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