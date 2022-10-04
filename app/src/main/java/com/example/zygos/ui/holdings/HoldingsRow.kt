package com.example.zygos.ui.holdings

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.ExpandLess
import androidx.compose.material.icons.sharp.ExpandMore
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.zygos.data.Position
import com.example.zygos.ui.components.ComponentIndicatorLine
import com.example.zygos.ui.components.TickerListValueRow
import com.example.zygos.ui.theme.ZygosTheme
import com.example.zygos.viewModel.TestViewModel

@Composable
fun HoldingsRow(
    position: Position,
    color: Color,
    displayOption: String,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(true) }

    Column(modifier = modifier) {
        TickerListValueRow(
            ticker = position.ticker,
            color = color,
            value = position.equity,
            subvalue = if (displayOption == "Returns") position.returns else position.returnsPercent,
            isSubvalueDollar = (displayOption == "Returns"),
            modifier = Modifier
        ) {
            if (position.subPositions.isEmpty()) {
                Column(Modifier) {
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        Text(text = "${position.shares}", style = MaterialTheme.typography.subtitle1)
                        Text(text = "shares", style = MaterialTheme.typography.subtitle1)
                    }
                }
            } else {
                IconButton(onClick = { expanded = !expanded }) {
                    if (expanded) Icon(imageVector = Icons.Sharp.ExpandMore, contentDescription = null)
                    else Icon(imageVector = Icons.Sharp.ExpandLess, contentDescription = null)
                }
            }
        }


        position.subPositions.forEachIndexed { index, pos ->

            Row(
                modifier = Modifier.height(52.dp)
            ) {
                ComponentIndicatorLine(
                    last = index == position.subPositions.lastIndex,
                    modifier = Modifier
                        .padding(start = 26.dp, end = 4.dp)
                        .size(width = 20.dp, height = 52.dp)
                )
                if (pos.type.isOption) {
                    Column {
                        Text(pos.type.toString())
                        Text(pos.shares.toString())
                    }
                    Column {
                        Text(pos.expiration.toString())
                        Text(pos.strike.toString())
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