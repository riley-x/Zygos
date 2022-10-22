package com.example.zygos.ui.settingsScreen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.Share
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.zygos.ui.theme.ZygosTheme
import com.example.zygos.viewModel.TestViewModel

@Composable
internal fun BackupDatabaseCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = { },
) {
    Card(
        elevation = 1.dp,
        modifier = modifier,
    ) {
        Column {
            CardTitle(title = "Backup Database") {
                IconButton(onClick = onClick) {
                    Icon(imageVector = Icons.Sharp.Share, contentDescription = null)
                }
            }

//            CardRowDivider(color = MaterialTheme.colors.primary)
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
            BackupDatabaseCard(
            )
        }
    }
}