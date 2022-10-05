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
import com.example.zygos.data.LotPosition
import com.example.zygos.data.toFloatDollar
import com.example.zygos.ui.components.*
import com.example.zygos.ui.theme.ZygosTheme
import com.example.zygos.viewModel.Position
import com.example.zygos.viewModel.TestViewModel

@Composable
fun HoldingsSubRow(
    position: Position,
    color: Color,
    displayOption: String,
    last: Boolean,
    modifier: Modifier = Modifier,
) {
    val dividerColor = MaterialTheme.colors.onBackground.copy(alpha = 0.2f)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .height(52.dp)
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
                .size(width = 20.dp, height = 52.dp)
        )
        if (position.lot.type.isOption) {
            Column(Modifier.weight(10f)) {
                Text(position.lot.type.toString())
                Text("x${position.lot.shares}")
            }
            Column(Modifier.weight(10f)) {
                Text(position.lot.expiration)
                Text(position.lot.strike)
            }

        } else {
            Column(Modifier.weight(10f)) {
                Text("${position.lot.shares} shares")
                Text(formatDollar(position.lot.priceOpen.toFloatDollar()))
            }
        }
        ValueAndSubvalue(
            value = position.equity,
            subvalue = if (displayOption == "Returns") position.returns else position.returnsPercent,
            isSubvalueDollar = (displayOption == "Returns"),
            modifier = Modifier.weight(10f)
        )
    }
}


@Preview(showBackground = true)
@Composable
fun HoldingsSubRowPreview() {
    val viewModel = viewModel<TestViewModel>()
    ZygosTheme {
        Surface() {
            Column() {
                HoldingsSubRow(
                    position = viewModel.longPositions[0],
                    displayOption = "Returns",
                    last = false,
                    color = Color(0xff00a1f1),
                )
                HoldingsSubRow(
                    position = viewModel.longPositions[1],
                    displayOption = "% Change",
                    last = true,
                    color = Color(0xff00a1f1),
                )
            }
        }
    }
}