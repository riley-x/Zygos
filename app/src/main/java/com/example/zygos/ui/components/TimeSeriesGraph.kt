package com.example.zygos.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.ContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.zygos.ui.theme.ZygosTheme
import kotlin.math.roundToInt


data class TimeSeriesTickX(
    val index: Int,
    val label: String,
)


/**
 * Contains the graph canvas and also the top text for hover info
 */
@OptIn(ExperimentalTextApi::class)
@Composable
fun TimeSeriesGraph(
    values: SnapshotStateList<Float>,
    ticksY: SnapshotStateList<Float>,
    ticksX: SnapshotStateList<TimeSeriesTickX>,
    minY: Float,
    maxY: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colors.onSurface,
    stroke: Dp = 2.dp,
    labelYOffset: Dp = 8.dp, // padding left of label
    labelXOffset: Dp = 2.dp, // padding top of label
    onHover: (x: Int, y: Float) -> Unit = { _, _ -> },
) {
    if (values.size < 2) return

    /** Cache some text variables (note MaterialTheme is not accessible in DrawScope) **/
    val textMeasurer = rememberTextMeasurer()
    val textStyle = MaterialTheme.typography.subtitle2
    val textColor = MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.medium)
    val textLayoutResult: TextLayoutResult =
        textMeasurer.measure( // Use the last y tick (~widest value) to estimate text extent
            text = AnnotatedString("${ticksY.last().roundToInt()}"),
            style = textStyle,
        )
    val textSize = textLayoutResult.size

    /** Other config vars **/
    val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
    val gridColor = color.copy(alpha = 0.3f)
    val strokeWidthPx = with(LocalDensity.current) { stroke.toPx() }
    val labelYOffsetPx = with(LocalDensity.current) { labelYOffset.toPx() }
    val labelXOffsetPx = with(LocalDensity.current) { labelXOffset.toPx() }

    Canvas(modifier = modifier
    ) {
        /** user -> pixel conversions **/
        val endX = size.width - textSize.width - labelYOffsetPx
        val deltaX = endX / (values.size - 1)
        val startY = size.height - textSize.height - labelXOffsetPx
        val deltaY = -startY / (maxY - minY)

        /** Y Gridlines and Axis Labels **/
        for (tick in ticksY) {
            val y = startY + deltaY * (tick - minY)
            drawLine(
                start = Offset(x = 0f, y = y),
                end = Offset(x = endX, y = y),
                color = gridColor,
                pathEffect = pathEffect,
            )
            val layoutResult: TextLayoutResult =
                textMeasurer.measure(
                    text = AnnotatedString("${tick.roundToInt()}"),
                    style = textStyle,
                )
            drawText(
                textLayoutResult = layoutResult,
                color = textColor,
                topLeft = Offset(
                    x = size.width - layoutResult.size.width,
                    y = y - layoutResult.size.height / 2
                )
            )
        }

        /** X Gridlines and Axis Labels **/
        for (tick in ticksX) {
            val x = tick.index.toFloat() * deltaX
            drawLine(
                start = Offset(x = x, y = startY),
                end = Offset(x = x, y = 0f),
                color = gridColor,
                pathEffect = pathEffect,
            )
            val layoutResult: TextLayoutResult =
                textMeasurer.measure(
                    text = AnnotatedString(tick.label),
                    style = textStyle,
                )
            drawText(
                textLayoutResult = layoutResult,
                color = textColor,
                topLeft = Offset(
                    x = x - layoutResult.size.width / 2,
                    y = size.height - layoutResult.size.height
                )
            )
        }

        /** Main line plot **/
        for (i in 1 until values.size) {
            drawLine(
                start = Offset(x = deltaX * (i - 1), y = startY + deltaY * (values[i - 1] - minY)),
                end = Offset(x = deltaX * i, y = startY + deltaY * (values[i] - minY)),
                color = color,
                strokeWidth = strokeWidthPx,
            )
        }
    }
}


@Preview(
    widthDp = 360,
    heightDp = 400,
)
@Composable
fun TimeSeriesGraphPreview() {
    val values = remember { List(20) { it * if (it % 2 == 0) 1.2f else 0.8f }.toMutableStateList() }
    val ticksY = remember { mutableStateListOf(5f, 10f, 15f, 20f) }
    val ticksX = remember { mutableStateListOf(
        TimeSeriesTickX(5, "test"),
        TimeSeriesTickX(10, "9/12/23"),
        TimeSeriesTickX(15, "10/31/21"),
    ) }

    ZygosTheme {
        TimeSeriesGraph(
            values = values,
            ticksY = ticksY,
            ticksX = ticksX,
            minY = 0f,
            maxY = 25f,
            modifier = Modifier.fillMaxSize()
        )
    }
}