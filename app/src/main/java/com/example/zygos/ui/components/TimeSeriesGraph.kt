package com.example.zygos.ui.components

import android.util.Log
import android.view.MotionEvent
import androidx.compose.foundation.Canvas
import androidx.compose.material.ContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
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
import com.example.zygos.viewModel.HasName
import com.example.zygos.viewModel.TestViewModel
import kotlin.math.roundToInt


/**
 * Where
 *      pixX = deltaX * (userX - minX)
 *      pixY = startY + deltaY * (userY - minY)
 */
typealias TimeSeriesGrapher<T> = (
    drawScope: DrawScope,
    values: List<T>,
    deltaX: Float,
    deltaY: Float,
    startY: Float,
    minX: Float,
    minY: Float
) -> Unit


/**
 * Plots a graph where the x axis has discrete values. This is an "abstract" class that handles the
 * grid, axes, labels, hover, and callbacks. Users should implement the main graph drawing with one
 * of the following in the grapher argument:
 *      TimeSeriesLineGraph
 *      TimeSeriesCandlestickGraph
 *
 * @param grapher           The main graphing function. It should call draw functions on the
 *                          passed drawScope parameter.
 * @param minY,maxY         The lower and upper y bounds of the graph, in user coordinates
 * @param padX              The amount of padding at each left/right, as fraction of distance between points
 * @param xAxisLoc          User y location to draw the x axis, or null for no axis
 * @param labelYStartPad    Padding to the left of the y tick labels
 * @param labelXTopPad      Padding above the x tick labels
 *
 * @param onPress           Callback on first press down. Can be used to clear focus, for example
 * @param onHover           Callback for when the hover position changes. Returns the original index
 *                          into values for x and the dollar value for y. WARNING x, y can be out of
 *                          bounds! Make sure to catch.
 */
data class TimeSeriesGraphState<T>(
    val startingValue: Float = 0f,
    val values: List<T> = emptyList(),
    val ticksY: List<Float> = emptyList(),
    val ticksX: List<Int> = emptyList(),
    val minY: Float = 0f,
    val maxY: Float = 100f,
    val padX: Float = 0f,
    val xAxisLoc: Float? = null, // y value of x axis location
)

@OptIn(ExperimentalTextApi::class, ExperimentalComposeUiApi::class)
@Composable
fun <T: HasName> TimeSeriesGraph(
    state: TimeSeriesGraphState<T>,
    modifier: Modifier = Modifier,
    labelYStartPad: Dp = 8.dp, // padding left of label
    labelXTopPad: Dp = 2.dp, // padding top of label
    grapher: TimeSeriesGrapher<T> = { _, _, _, _, _, _, _ -> },
    onHover: (isHover: Boolean, x: Int, y: Float) -> Unit = { _, _, _ -> },
    onPress: () -> Unit = { },
) {
    if (state.values.size < 2) return

    /** Cache some text variables (note MaterialTheme is not accessible in DrawScope) **/
    val textMeasurer = rememberTextMeasurer()
    val textStyle = MaterialTheme.typography.subtitle2
    val textColor = MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.medium)
    val labelYWidth = if (state.ticksY.isEmpty()) 0 else {
        val textLayoutResult: TextLayoutResult =
            textMeasurer.measure( // Use the last y tick (~widest value) to estimate text extent
                text = AnnotatedString("${state.ticksY.last().roundToInt()}"),
                style = textStyle,
            )
        textLayoutResult.size.width
    }
    val labelXHeight = if (state.ticksX.isEmpty()) 0 else {
        val textLayoutResult: TextLayoutResult =
            textMeasurer.measure( // Use the first x tick cause no better
                text = AnnotatedString(state.values[state.ticksX.first()].name),
                style = textStyle,
            )
        textLayoutResult.size.height
    }

    /** Other Config Vars **/
    val hoverColor = MaterialTheme.colors.onSurface
    val gridColor = MaterialTheme.colors.onSurface.copy(alpha = 0.3f)
    val axisColor = MaterialTheme.colors.primary
    val gridPathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
    val axisPathEffect = PathEffect.dashPathEffect(floatArrayOf(40f, 20f), 0f)
    val labelYOffsetPx = with(LocalDensity.current) { labelYStartPad.toPx() }
    val labelXOffsetPx = with(LocalDensity.current) { labelXTopPad.toPx() }

    /** Hover vars **/
    var boxSize by remember { mutableStateOf(IntSize(10, 10)) } // 960 x 1644
    val disallowIntercept = RequestDisallowInterceptTouchEvent()
    var hoverXUser by remember { mutableStateOf(-1) }
    var hoverYPx by remember { mutableStateOf(-1f) }

    /** User -> pixel conversions
     *
     * User y coordinates are just the y values. However user x coordinates are 0..n-1, given by
     * xRange. Note however that onHover returns the unsliced index.
     *
     *      pixelX = 0 + deltaX * (index - minX)
     *      pixelY = startY + deltaY * (valueY - minY)
     */
    val endX = boxSize.width - labelYWidth - labelYOffsetPx
    val minX = -state.padX
    val maxX = state.values.lastIndex + state.padX
    val deltaX = endX / (maxX - minX)
    val startY = boxSize.height - labelXHeight - labelXOffsetPx
    val deltaY = -startY / (state.maxY - state.minY)

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
                val userX = clamp((motionEvent.x / deltaX + minX).roundToInt(), 0, state.values.lastIndex)
                val userY = (motionEvent.y - startY) / deltaY + state.minY
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
        for (tick in state.ticksY) {
            val y = startY + deltaY * (tick - state.minY)
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
        for (tick in state.ticksX) {
            val x = (tick.toFloat() - minX) * deltaX
            drawLine(
                start = Offset(x = x, y = startY),
                end = Offset(x = x, y = 0f),
                color = gridColor,
                pathEffect = gridPathEffect,
            )
            val layoutResult: TextLayoutResult =
                textMeasurer.measure(
                    text = AnnotatedString(state.values[tick].name),
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
        if (state.xAxisLoc != null) {
            val y = startY + deltaY * (state.xAxisLoc - state.minY)
            drawLine(
                start = Offset(x = 0f, y = y),
                end = Offset(x = endX, y = y),
                color = axisColor,
                pathEffect = axisPathEffect,
                strokeWidth = 2f,
            )
        }

        /** Main Plot **/
        grapher(this, state.values, deltaX, deltaY, startY, minX, state.minY)

        /** Hover **/
        if (hoverYPx > 0 && hoverYPx < startY) {
            drawLine(
                start = Offset(x = 0f, y = hoverYPx),
                end = Offset(x = endX, y = hoverYPx),
                color = hoverColor,
            )
        }
        if (hoverXUser >= 0 && hoverXUser < state.values.size) {
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
            state = viewModel.accountPerformanceState
        )
    }
}
