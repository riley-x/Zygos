package com.example.zygos.ui.holdings

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.ExpandLess
import androidx.compose.material.icons.sharp.ExpandMore
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.zygos.ui.components.*
import com.example.zygos.ui.theme.ZygosTheme
import com.example.zygos.data.PricedPosition
import com.example.zygos.viewModel.TestViewModel



@Composable
fun HoldingsRow(
    position: PricedPosition,
    color: Color,
    displayOption: HoldingsListOptions,
    modifier: Modifier = Modifier,
    horizontalPadding: Dp = tickerListHorizontalPadding,
    onPositionClick: (PricedPosition) -> Unit = { },
    ) {
    var expanded by rememberSaveable(position) { mutableStateOf(false) }
    val hasSubpositions by remember { derivedStateOf {
        position.subPositions.isNotEmpty() && position.instrumentName.isEmpty()
    } }

    Column(modifier = modifier) {
        TickerListRow(
            ticker = position.ticker,
            color = color,
            modifier = Modifier
                .clickable { onPositionClick(position) }
                .padding(horizontal = horizontalPadding)
            // padding needs to be here so that the clickable animation covers the full width
        ) {
            if (hasSubpositions) {
                IconButton(onClick = { expanded = !expanded }) {
                    if (expanded) Icon(imageVector = Icons.Sharp.ExpandLess, contentDescription = null)
                    else Icon(imageVector = Icons.Sharp.ExpandMore, contentDescription = null)
                }
            } else {
                PositionRowSubInfo(position = position)
            }
            Spacer(Modifier.weight(10f))
            PositionRowInfo(position = position, displayOption = displayOption)
        }

        if (hasSubpositions) {
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
                            modifier = Modifier
                                .clickable { onPositionClick(pos) }
                                .padding(horizontal = horizontalPadding)
                            // padding needs to be here so that the clickable animation covers the full width
                        )
                    }
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
                    displayOption = HoldingsListOptions.RETURNS,
                    color = Color(0xff00a1f1),
                )

                Spacer(modifier = Modifier.padding(vertical = 12.dp))

                HoldingsRow(
                    position = viewModel.longPositions[1],
                    displayOption = HoldingsListOptions.RETURNS_PERCENT,
                    color = Color(0xff00a1f1),
                )
            }
        }
    }
}