package com.example.zygos.ui.colorSelector

import androidx.compose.foundation.layout.*
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.zygos.data.database.Transaction
import com.example.zygos.ui.components.LogCompositions
import com.example.zygos.ui.theme.ZygosTheme
import com.example.zygos.ui.transactions.TransactionDetailsScreen
import com.example.zygos.viewModel.TestViewModel

@Composable
fun ColorSelectorScreen(
    modifier: Modifier = Modifier,
    initialColor: Color = Color.White,
    onSave: (Color) -> Unit = { },
    onCancel: () -> Unit = { },
) {
    LogCompositions("Zygos", "ColorSelectorScreen")

    ColorSelector(
        initialColor = initialColor,
        modifier = Modifier
            .padding(start = 30.dp, end = 30.dp)
            .fillMaxWidth(),
    )
}

@Preview(
    widthDp = 360,
    heightDp = 740,
)
@Composable
fun PreviewColorSelectorScreen() {
    val viewModel = viewModel<TestViewModel>()
    ZygosTheme {
        Surface {
            ColorSelectorScreen(
            )
        }
    }
}