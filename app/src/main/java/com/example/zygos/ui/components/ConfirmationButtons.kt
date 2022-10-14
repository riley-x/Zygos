package com.example.zygos.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.zygos.ui.theme.ZygosTheme

@Composable
fun ConfirmationButtons(
    modifier: Modifier = Modifier,
    onCancel: () -> Unit = { },
    onOk: () -> Unit = { },
) {
    Row(
        modifier = modifier
            .padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Button(
            onClick = onCancel,
            colors = ButtonDefaults.buttonColors(
                backgroundColor = MaterialTheme.colors.error,
            )
        ) {
            Text("Cancel")
        }

        Button(
            onClick = onOk,
        ) {
            Text("OK")
        }
    }
}


@Preview
@Composable
fun PreviewConfirmationButtons() {
    ZygosTheme {
        ConfirmationButtons()
    }
}