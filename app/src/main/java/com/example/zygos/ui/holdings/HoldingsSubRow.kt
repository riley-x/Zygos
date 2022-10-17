package com.example.zygos.ui.holdings

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.zygos.ui.components.*
import com.example.zygos.ui.theme.ZygosTheme
import com.example.zygos.data.PricedPosition
import com.example.zygos.viewModel.TestViewModel

@Composable
fun HoldingsSubRow(
    position: PricedPosition,
    color: Color,
    displayOption: HoldingsListDisplayOptions,
    showPercentages: State<Boolean>,
    last: Boolean,
    modifier: Modifier = Modifier,
) {
    val dividerColor = MaterialTheme.colors.onBackground.copy(alpha = 0.2f)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .height(tickerListHeight)
            .drawBehind { // Can't use Divider because that adds a space between the ComponentIndicatorLines
                val strokeWidth = 1.dp.value * density
                val y = strokeWidth / 2
                drawLine(
                    color = dividerColor,
                    start = Offset(50.dp.value * density, y),
                    end = Offset(size.width, y),
                    strokeWidth = strokeWidth
                )
            }
    ) {
        ComponentIndicatorLine(
            last = last,
            color = color.copy(alpha = 0.5f),
            modifier = Modifier
                .padding(start = 26.dp, end = 4.dp)
                .size(width = 22.dp, height = 60.dp)
        )
        PositionRowSubInfo(position = position, Modifier.weight(10f))
        PositionRowInfo(position = position, displayOption = displayOption, showPercentages = showPercentages)
    }
}


@Preview(showBackground = true)
@Composable
fun HoldingsSubRowPreview() {
    val viewModel = viewModel<TestViewModel>()
    val trueState = remember { mutableStateOf(true) }
    val falseState = remember { mutableStateOf(false) }
    ZygosTheme {
        Surface() {
            Column() {
                HoldingsSubRow(
                    position = viewModel.longPositions[0].subPositions[0],
                    displayOption = HoldingsListDisplayOptions.RETURNS_TOTAL,
                    last = false,
                    color = Color(0xff00a1f1),
                    showPercentages = trueState,
                )
                HoldingsSubRow(
                    position = viewModel.longPositions[0].subPositions[1],
                    displayOption = HoldingsListDisplayOptions.RETURNS_TOTAL,
                    last = true,
                    color = Color(0xff00a1f1),
                    showPercentages = falseState,
                )
            }
        }
    }
}