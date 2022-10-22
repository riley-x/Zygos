package com.example.zygos.ui.settingsScreen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.zygos.network.ApiService
import com.example.zygos.network.apiServices
import com.example.zygos.ui.theme.ZygosTheme
import com.example.zygos.viewModel.TestViewModel

@Composable
internal fun ApiKeysCard(
    apiKeys: SnapshotStateMap<String, String>,
    modifier: Modifier = Modifier,
    onApiKeyClick: (ApiService) -> Unit = { },
) {
    Card(
        elevation = 1.dp,
        modifier = modifier,
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(start = 10.dp)
                    .heightIn(min = 48.dp)
            ) {
                Text("API Keys", style = MaterialTheme.typography.h3)
            }

            CardRowDivider(color = MaterialTheme.colors.primary)

            apiServices.forEachIndexed { i, service ->
                if (i > 0) CardRowDivider()

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .heightIn(min = 40.dp)
                        .clickable { onApiKeyClick(service) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(service.name, modifier = Modifier.weight(10f))
                    val key = apiKeys.getOrDefault(service.name, "").takeLast(4)
                    if (key.isNotBlank())
                        Text("••••$key", style = MaterialTheme.typography.subtitle2)
                }
            }
        }
    }
}


@Preview(
    widthDp = 360,
)
@Composable
private fun Preview() {
    val viewModel = viewModel<TestViewModel>()
    ZygosTheme {
        Surface {
            ApiKeysCard(
                apiKeys = viewModel.apiKeys
            )
        }
    }
}