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
 *      pixX = deltaX * (userX - minX)
 *      pixY = startY + deltaY * (userY - minY)
 */
typealias TimeSeriesGrapher<T> =
            (DrawScope, List<T>, Float, Float, Float, Float, Float) -> Unit


/**
 * Plots a graph where the x axis has discrete values. This is an "abstract"
 * class that handles the grid, axes, labels, and callbacks. Users should
 * implement the main graph drawing with the grapher argument:
 *      TimeSeriesLineGraph
 *      TimeSeriesCandlestickGraph
 *
 * @param xRange        The indices from values to be read. Useful for i.e. switching time ranges
 *                      without resetting the whole list. The range also defines the "user"
 *                      coordinates in the x direction.
 * @param minX,minY     The lower and upper bounds of the graph, in user coordinates
 * @param
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
    xRange: IntRange = 0..values.lastIndex, // only read these
    //
    minX: Float = xRange.first.toFloat(), // recall that user x coordinates are simply xRange
    maxX: Float = xRange.last.toFloat(),
    xAxisLoc: Float? = null, // y value of x axis location
    labelYOffset: Dp = 8.dp, // padding left of label
    labelXOffset: Dp = 2.dp, // padding top of label
    grapher: TimeSeriesGrapher<T> = { _, _, _, _, _, _, _ -> },
    onHover: (isHover: Boolean, x: Int, y: Float) -> Unit = { _, _, _ -> },
    onPress: () -> Unit = { }, // On first press. Can be used to clear focus, for example
    // WARNING x, y can be out of bounds! Make sure to catch.
) {
    if (xRange.count() < 2) return

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
    var hoverXUser by remember { mutableStateOf(-1) }
    var hoverYPx by remember { mutableStateOf(-1f) }

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
            if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                onPress()
            }
            if (
                motionEvent.action == MotionEvent.ACTION_MOVE ||
                motionEvent.action == MotionEvent.ACTION_DOWN
            ) {
                disallowIntercept(true)
                val userXFloat = (motionEvent.x / deltaX + minX)
                val userX = clamp((userXFloat / xRange.step).roundToInt() * xRange.step, xRange.first, xRange.last)
                val userY = (motionEvent.y - startY) / deltaY + minY
                hoverXUser = userX
                hoverYPx = motionEvent.y
                onHover(true, userX, userY)
            } else {
                disallowIntercept(false)
                onHover(false,0, 0f)
                hoverXUser = -1
                hoverYPx = -1f
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
                strokeWidth = 2f,
            )
        }

        /** Main Plot **/
        grapher(this, values.slice(xRange), deltaX, deltaY, startY, minX, minY)

        /** Hover **/
        if (hoverYPx > 0 && hoverYPx < startY) {
            drawLine(
                start = Offset(x = 0f, y = hoverYPx),
                end = Offset(x = endX, y = hoverYPx),
                color = hoverColor,
            )
        }
        if (hoverXUser in xRange) {
            // check xRange instead of min/maxX since we don't want to hover over an empty point
            // since hoverXUser is clamped above, could just check if >= 0
            val x = (hoverXUser.toFloat() - minX) * deltaX
            drawLine(
                start = Offset(x = x, y = startY),
                end = Offset(x = x, y = 0f),
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
            xAxisLoc = viewModel.accountStartingValue
        )
    }
}
