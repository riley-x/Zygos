package com.example.zygos.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.zygos.ui.theme.ZygosTheme

@Composable
fun AddAccountDialog(
    modifier: Modifier = Modifier,
    onDismiss: (String) -> Unit = { },
) {
    var text by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { onDismiss("") },
        // don't use the title argument, it really messes with the layouts
        text = {
            Column {
                Text(text = "Add Account", modifier = Modifier.padding(bottom = 6.dp))
                OutlinedTextField(
                    value = text,
                    textStyle = MaterialTheme.typography.h5,
                    onValueChange = { text = it },
                    placeholder = {
                        Box(
                            contentAlignment = Alignment.CenterStart,
                            modifier = Modifier.fillMaxHeight()
                        ) {
                            Text("Account name")
                        }
                    },
                    modifier = Modifier.height(60.dp)
                )
            }
        },
        buttons = {
            Row(
                modifier = Modifier
                    .padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { onDismiss("") },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = MaterialTheme.colors.error,
                    )
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = { onDismiss(text) },
                ) {
                    Text("OK")
                }
            }
        },
        modifier = modifier,
    )
}

@Preview
@Composable
fun PreviewAddAccountDialog() {
    ZygosTheme {
        AddAccountDialog()
    }
}
