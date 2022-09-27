package com.example.zygos.ui.components

import android.view.MotionEvent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.ContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.RequestDisallowInterceptTouchEvent
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.math.MathUtils.clamp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.zygos.ui.theme.ZygosTheme
import com.example.zygos.viewModel.Named
import com.example.zygos.viewModel.NamedValue
import com.example.zygos.viewModel.TestViewModel
import kotlin.math.roundToInt


/**
 * The floats are:
 *      deltaX
 *      deltaY
 *      startY
 *      minX
 *      minY
 * Where
 *      pixX = userX * deltaX
 *      pixY = startY + deltaY * (userY - minY)
 */
typealias TimeSeriesGrapher<T> =
            (DrawScope, SnapshotStateList<T>, Float, Float, Float, Float, Float) -> Unit


/**
 * Plots a graph where the x axis has discrete values. This is an "abstract"
 * class that handles the grid, axes, labels, and callbacks. Users should
 * implement the main graph drawing with the grapher argument:
 *      TimeSeriesLineGraph
 *      TimeSeriesCandlestickGraph
 */
@OptIn(ExperimentalTextApi::class, ExperimentalComposeUiApi::class)
@Composable
fun <T: Named> TimeSeriesGraph(
    values: SnapshotStateList<T>,
    ticksY: SnapshotStateList<Float>,
    ticksX: SnapshotStateList<Int>, // index into values
    minY: Float,
    maxY: Float,
    modifier: Modifier = Modifier,
    minX: Float = 0f,
    maxX: Float = values.lastIndex.toFloat(),
    xAxisLoc: Float? = null, // y value of x axis location
    labelYOffset: Dp = 8.dp, // padding left of label
    labelXOffset: Dp = 2.dp, // padding top of label
    grapher: TimeSeriesGrapher<T> = { _, _, _, _, _, _, _ -> },
    onHover: (isHover: Boolean, x: Int, y: Float) -> Unit = { _, _, _ -> },
    // WARNING x, y can be out of bounds! Make sure to catch.
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

    /** Other Config Vars **/
    val hoverColor = MaterialTheme.colors.onSurface
    val gridColor = MaterialTheme.colors.onSurface.copy(alpha = 0.3f)
    val axisColor = MaterialTheme.colors.primary
    val gridPathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
    val axisPathEffect = PathEffect.dashPathEffect(floatArrayOf(40f, 20f), 0f)
    val labelYOffsetPx = with(LocalDensity.current) { labelYOffset.toPx() }
    val labelXOffsetPx = with(LocalDensity.current) { labelXOffset.toPx() }

    /** Hover vars **/
    var boxSize by remember { mutableStateOf(IntSize(10, 10)) } // 960 x 1644
    val disallowIntercept = RequestDisallowInterceptTouchEvent()
    var hoverPos by remember { mutableStateOf(Offset(-1f, -1f)) }

    /** User -> pixel conversions
     *      pixelX = 0 + deltaX * (index - minX)
     *      pixelY = startY + deltaY * (valueY - minY)
     */
    val endX = boxSize.width - textSize.width - labelYOffsetPx
    val deltaX = endX / (maxX - minX)
    val startY = boxSize.height - textSize.height - labelXOffsetPx
    val deltaY = -startY / (maxY - minY)

    Canvas(modifier = modifier
        .onGloballyPositioned { boxSize = it.size }
        .pointerInteropFilter(
            requestDisallowInterceptTouchEvent = disallowIntercept
        ) { motionEvent ->
            if (
                motionEvent.action == MotionEvent.ACTION_MOVE ||
                motionEvent.action == MotionEvent.ACTION_DOWN
            ) {
                disallowIntercept(true)
                val userX = clamp((motionEvent.x / deltaX + minX).roundToInt(), 0, values.lastIndex)
                val userY = (motionEvent.y - startY) / deltaY + minY
                val roundedX = (userX - minX) * deltaX
                onHover(true, userX, userY)
                hoverPos = Offset(roundedX, motionEvent.y)
            } else {
                disallowIntercept(false)
                onHover(false,0, 0f)
                hoverPos = Offset(-1f, -1f)
            }
            true
        },
    ) {
        /** Y Gridlines and Axis Labels **/
        for (tick in ticksY) {
            val y = startY + deltaY * (tick - minY)
            drawLine(
                start = Offset(x = 0f, y = y),
                end = Offset(x = endX, y = y),
                color = gridColor,
                pathEffect = gridPathEffect,
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
            val x = (tick.toFloat() - minX) * deltaX
            drawLine(
                start = Offset(x = x, y = startY),
                end = Offset(x = x, y = 0f),
                color = gridColor,
                pathEffect = gridPathEffect,
            )
            val layoutResult: TextLayoutResult =
                textMeasurer.measure(
                    text = AnnotatedString(values[tick].name),
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

        /** X Axis **/
        if (xAxisLoc != null) {
            val y = startY + deltaY * (xAxisLoc - minY)
            drawLine(
                start = Offset(x = 0f, y = y),
                end = Offset(x = endX, y = y),
                color = axisColor,
                pathEffect = axisPathEffect,
                )
        }

        /** Main Plot **/
        grapher(this, values, deltaX, deltaY, startY, minX, minY)

        /** Hover **/
        if (hoverPos.y > 0 && hoverPos.y < startY) {
            drawLine(
                start = Offset(x = 0f, y = hoverPos.y),
                end = Offset(x = endX, y = hoverPos.y),
                color = hoverColor,
            )
        }
        if (hoverPos.x >= 0 && hoverPos.x <= endX) {
            drawLine(
                start = Offset(x = hoverPos.x, y = startY),
                end = Offset(x = hoverPos.x, y = 0f),
                color = hoverColor,
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
    val viewModel = viewModel<TestViewModel>()
    ZygosTheme {
        TimeSeriesGraph(
            values = viewModel.accountPerformance,
            ticksY = viewModel.accountPerformanceTicksY,
            ticksX = viewModel.accountPerformanceTicksX,
            minY = 0f,
            maxY = 25f,
            xAxisLoc = viewModel.accountStartingValue,
            modifier = Modifier.size(300.dp, 400.dp)
        )
    }
}
