package com.example.zygos.ui.performance

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.Check
import androidx.compose.material.icons.sharp.North
import androidx.compose.material.icons.sharp.South
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.zygos.ui.theme.ZygosTheme


val watchlistSortOptions = listOf("Ticker", "% Change")
val watchlistDisplayOptions = listOf("Change", "% Change")

//@Composable
//fun HoldingsListSortOptionRow(
//    text: String,
//    isActive: Boolean,
//    isSortedAscending: Boolean,
//    modifier: Modifier = Modifier,
//) {
//    Row(
//        modifier = modifier.heightIn(min = 20.dp),
//        verticalAlignment = Alignment.CenterVertically,
//    ) {
//        Text(text, modifier = Modifier.weight(1f))
//        if (isActive) {
//            if (isSortedAscending) {
//                Icon(
//                    Icons.Sharp.North,
//                    contentDescription = null
//                )
//            } else {
//                Icon(
//                    Icons.Sharp.South,
//                    contentDescription = null
//                )
//            }
//        }
//    }
//}
//
//@Composable
//fun HoldingsListDisplayOptionRow(
//    text: String,
//    isActive: Boolean,
//    modifier: Modifier = Modifier,
//) {
//    Row(
//        modifier = modifier.heightIn(min = 20.dp),
//        verticalAlignment = Alignment.CenterVertically,
//    ) {
//        Text(text, modifier = Modifier.weight(1f))
//        if (isActive) {
//            Icon(
//                Icons.Sharp.Check,
//                contentDescription = null
//            )
//        }
//    }
//}
//
//
//@Composable
//fun HoldingsListOptionsDivider() {
//    Divider(
//        color = MaterialTheme.colors.onBackground.copy(alpha = 0.2f),
//        thickness = 1.dp,
//        modifier = Modifier
//            .padding(horizontal = 20.dp)
//    )
//}
//
//fun holdingsListOptionsSheet(
//    currentSortOption: String,
//    currentDisplayOption: String,
//    isSortedAscending: Boolean,
//    onDisplayOptionSelected: (String) -> Unit = { },
//    onSortOptionSelected: (String) -> Unit = { },
//) : (@Composable ColumnScope.() -> Unit) =
//    @Composable {
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(vertical = 24.dp)
//        ) {
//            Text(
//                text = "Display",
//                style = MaterialTheme.typography.h5,
//                modifier = Modifier
//                    .align(Alignment.CenterHorizontally)
//                    .padding(bottom = 8.dp)
//            )
//            for ((index, opt) in holdingsListDisplayOptions.withIndex()) {
//                if (index > 0) HoldingsListOptionsDivider()
//
//                HoldingsListDisplayOptionRow(
//                    text = opt,
//                    isActive = (opt == currentDisplayOption),
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(48.dp)
//                        .clickable { onDisplayOptionSelected(opt) }
//                        .padding(horizontal = 20.dp)
//                )
//            }
//
//            Text(
//                text = "Sorting",
//                style = MaterialTheme.typography.h5,
//                modifier = Modifier
//                    .align(Alignment.CenterHorizontally)
//                    .padding(bottom = 8.dp, top = 8.dp)
//            )
//            for ((index, opt) in holdingsListSortOptions.withIndex()) {
//                if (index > 0) HoldingsListOptionsDivider()
//
//                HoldingsListSortOptionRow(
//                    text = opt,
//                    isActive = (opt == currentSortOption),
//                    isSortedAscending = isSortedAscending,
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(48.dp)
//                        .clickable { onSortOptionSelected(opt) }
//                        .padding(horizontal = 20.dp)
//                )
//            }
//        }
//    }
//
//@Preview(
//    widthDp = 300,
//    heightDp = 600,
//    showBackground = true,
//    backgroundColor = 0xFF666666,
//)
//@Composable
//fun PreviewHoldingsListOptionsSheet() {
//    ZygosTheme {
//        Column(
//            modifier = Modifier.fillMaxWidth(),
//            verticalArrangement = Arrangement.Bottom,
//        ) {
//            Surface {
//                holdingsListOptionsSheet(
//                    currentSortOption = holdingsListSortOptions[0],
//                    currentDisplayOption = holdingsListDisplayOptions[0],
//                    isSortedAscending = true,
//                )(this)
//            }
//        }
//    }
//}