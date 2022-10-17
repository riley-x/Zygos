package com.example.zygos.ui.holdings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.Percent
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.zygos.ui.components.ListTitleBar
import com.example.zygos.ui.components.tickerListHorizontalPadding
import com.example.zygos.ui.theme.ZygosTheme

@Composable
fun HoldingsListTitle(
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
fun PreviewHoldingsListTitle() {
    ZygosTheme {
        Surface {
            Column {
                HoldingsListTitle(text = "Positions", showPercentages = true)
                HoldingsListTitle(text = "Positions", showPercentages = false)
            }
        }
    }
}