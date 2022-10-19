package com.example.zygos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.Percent
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.zygos.ui.theme.ZygosTheme

@Composable
fun ListTitleBarPercent(
    text: String,
    showPercentages: Boolean,
    modifier: Modifier = Modifier,
    onOptionsButtonClick: () -> Unit = { },
    onToggleShowPercentages: () -> Unit = { },
) {
    ListTitleBar(
        text = text,
        onOptionsButtonClick = onOptionsButtonClick,
        modifier = modifier
            .background(MaterialTheme.colors.surface)
            .padding(start = tickerListHorizontalPadding)
    ) {
        IconButton(onClick = onToggleShowPercentages) {
            Icon(
                imageVector = Icons.Sharp.Percent,
                contentDescription = null,
                tint = if (showPercentages) MaterialTheme.colors.primary
                else MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.disabled)
            )
        }
    }
}


@Preview
@Composable
fun PreviewListTitleBarPercent() {
    ZygosTheme {
        Surface {
            Column {
                ListTitleBarPercent(text = "Positions", showPercentages = true)
                ListTitleBarPercent(text = "Positions", showPercentages = false)
            }
        }
    }
}