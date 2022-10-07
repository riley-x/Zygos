package com.example.zygos.ui.holdings

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.ExpandLess
import androidx.compose.material.icons.sharp.ExpandMore
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.zygos.data.PositionType
import com.example.zygos.ui.components.*
import com.example.zygos.ui.theme.ZygosTheme
import com.example.zygos.viewModel.Position
import com.example.zygos.viewModel.TestViewModel

@Composable
fun HoldingsRow(
    position: Position,
    color: Color,
    displayOption: String,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        TickerListValueRow(
            ticker = position.ticker,
            color = color,
            value = position.equity,
            subvalue = if (displayOption == "Returns") position.returnsOpen else position.returnsPercent,
            isSubvalueDollar = (displayOption == "Returns"),
            modifier = Modifier
        ) {
            if (position.subPositions.isNotEmpty() && position.type == PositionType.NONE) { // NONE is used for compound positions
                IconButton(onClick = { expanded = !expanded }) {
                    if (expanded) Icon(imageVector = Icons.Sharp.ExpandLess, contentDescription = null)
                    else Icon(imageVector = Icons.Sharp.ExpandMore, contentDescription = null)
                }
            } else if (position.ticker != "CASH") {
                Column(Modifier) {
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        Text(
                            text = "${position.shares}",
                            style = MaterialTheme.typography.subtitle1
                        )
                        Text(text = "shares", style = MaterialTheme.typography.subtitle1)
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(initialAlpha = 0.3f),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column {
                (position.subPositions).forEachIndexed { index, pos ->
                    HoldingsSubRow(
                        position = pos,
                        color = color,
                        displayOption = displayOption,
                        last = index == position.subPositions.lastIndex,
                    )
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun HoldingsRowPreview() {
    val viewModel = viewModel<TestViewModel>()
    ZygosTheme {
        Surface() {
            Column() {
                HoldingsRow(
                    position = viewModel.longPositions[0],
                    displayOption = "Returns",
                    color = Color(0xff00a1f1),
                )

                Spacer(modifier = Modifier.padding(vertical = 12.dp))

                HoldingsRow(
                    position = viewModel.longPositions[1],
                    displayOption = "% Change",
                    color = Color(0xff00a1f1),
                )
            }
        }
    }
}