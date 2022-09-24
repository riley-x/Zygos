package com.example.zygos.ui.transactions

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.zygos.ui.theme.ZygosTheme

@Composable
fun TransactionsScreen(
    innerPadding: PaddingValues,
) {
    Surface(
        modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {
        Text("Transactions Screen")
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
            PaddingValues(0.dp)
        )
    }
}