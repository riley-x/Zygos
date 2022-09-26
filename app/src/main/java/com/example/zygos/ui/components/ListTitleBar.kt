package com.example.zygos.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.zygos.ui.theme.ZygosTheme

/**
 * Creates a title bar for various lists with an options button
 */
@Composable
fun ListTitleBar(
    text: String,
    modifier: Modifier = Modifier,
    onOptionsButtonClick: () -> Unit = { },
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.h3,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = { onOptionsButtonClick() }) {
            Icon(
                imageVector = Icons.Sharp.MoreVert,
                contentDescription = null,
            )
        }
    }
}

@Preview
@Composable
fun PreviewListTittleBar() {
    ZygosTheme {
        Surface {
            ListTitleBar(text = "Hello!")
        }
    }
}