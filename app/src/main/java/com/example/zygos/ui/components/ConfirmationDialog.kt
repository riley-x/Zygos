package com.example.zygos.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.zygos.ui.theme.ZygosTheme

@Composable
fun ConfirmationDialog(
    text: String,
    modifier: Modifier = Modifier,
    onDismiss: (confirmed: Boolean) -> Unit = { },
) {
    AlertDialog(
        onDismissRequest = { onDismiss(false) },
        // don't use the title argument, it really messes with the layouts
        text = {
            Text(text = text, modifier = Modifier.padding(bottom = 6.dp))
        },
        buttons = {
            ConfirmationButtons(
                onCancel = { onDismiss(false) },
                onOk = { onDismiss(true) },
            )
        },
        modifier = modifier,
    )
}

@Preview
@Composable
fun PreviewConfirmationDialog() {
    ZygosTheme {
        ConfirmationDialog(
            "Confirm me!"
        )
    }
}