package com.example.zygos.ui.transactions

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.zygos.ui.components.LogCompositions
import com.example.zygos.ui.components.recomposeHighlighter
import com.example.zygos.ui.theme.ZygosTheme

@Composable
fun TransactionsScreen(
    testState: String = "",
    onClick: () -> Unit = { },
) {
    LogCompositions("Zygos", "TransactionsScreen")

    Surface(
        modifier = Modifier
            .recomposeHighlighter()
            .fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.requiredSize(200.dp).recomposeHighlighter()
        ) {
            TextButton(
                onClick = onClick,
                modifier = Modifier.recomposeHighlighter()
            ) {
                Text("Transactions Screen$testState")
            }
        }
    }

    // TODO: Use a floating button here for adding transactions?
    // And click transaction to edit/delete in a separate screen?
    // Sort by most recent first
}


@Preview(
    widthDp = 300,
    heightDp = 600,
    showBackground = true,
)
@Composable
fun PreviewTransactionsScreen() {
    ZygosTheme {
        TransactionsScreen(
        )
    }
}