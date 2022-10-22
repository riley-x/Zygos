package com.example.zygos.ui.settingsScreen

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.Share
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.zygos.ui.theme.ZygosTheme
import com.example.zygos.viewModel.TestViewModel

/** This applies a weight of 10 to the title text **/
@Composable
fun CardTitle(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit = { },
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .padding(start = 10.dp)
            .heightIn(min = 48.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.h3,
            modifier = Modifier.weight(10f)
        )

        content()
    }
}


@Preview(widthDp = 360)
@Composable
private fun Preview() {
    ZygosTheme {
        Surface {
            CardTitle(title = "Card Title")
        }
    }
}