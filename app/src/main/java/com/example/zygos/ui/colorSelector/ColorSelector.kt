package com.example.zygos.ui.colorSelector

import android.view.MotionEvent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import androidx.core.math.MathUtils.clamp
import com.example.zygos.ui.components.ImmutableList
import com.example.zygos.ui.theme.ZygosTheme
import kotlin.math.atan2
import kotlin.math.sqrt


val colors = ImmutableList(listOf(
    Color.Red, Color.Yellow, Color.Green, Color.Cyan, Color.Blue, Color.Magenta, Color.Red
))


@Composable
fun ColorCircle(
    brightness: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier.aspectRatio(1f).fillMaxSize()) {
        drawCircle(brush = Brush.sweepGradient(colors.items))

        drawCircle(
            brush = Brush.radialGradient(
                listOf(Color.White, Color.Transparent)
            )
        )

        drawCircle(color = Color.Black.copy(alpha = 1 - brightness))
    }
}



@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ColorSelector(
    modifier: Modifier = Modifier,
    initialColor: Color = Color.White,
) {
    var boxSize by remember { mutableStateOf(IntSize(10, 10)) } // 960 x 1644

//    fun colorToPixel(color: Color): Offset {
//
//    }

    fun pixelToColor(point: Offset): Color {
        val x = point.x - boxSize.width / 2
        val y = point.y - boxSize.height / 2

        val r = clamp(sqrt(x * x + y * y), 0f, 1f)
        var phi = Math.toDegrees(atan2(y, x).toDouble()).toFloat()
        if (phi < 0) phi += 360
        val stopIndex = phi.toInt() / 60

        val hue = ColorUtils.blendARGB(
            colors.items[stopIndex].toArgb(),
            colors.items[stopIndex + 1].toArgb(),
            phi / 60 - stopIndex,
        )
        return Color(ColorUtils.blendARGB(Color.White.toArgb(), hue, r))
    }

//    var currentColor by remember { mutableStateOf(initialColor) }
//    var currentHover by remember { mutableStateOf(colorToPixel(initialColor)) }
    var currentHover by remember { mutableStateOf(Offset(0f, 0f)) }
    var brightness by remember { mutableStateOf(1.0f) }


    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        ColorCircle(
            brightness = brightness,
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned {
                    boxSize = it.size
                    currentHover = Offset(boxSize.width / 2f, boxSize.height / 2f)
                }
                .pointerInteropFilter(
//                requestDisallowInterceptTouchEvent = disallowIntercept
            ) { motionEvent ->
                if (motionEvent.action == MotionEvent.ACTION_MOVE ||
                    motionEvent.action == MotionEvent.ACTION_DOWN
                ) {
//                    disallowIntercept(true)
                    currentHover = Offset(motionEvent.x, motionEvent.y)
                } else {
//                    disallowIntercept(false)
                }
                true
            }
        )

        Text("${pixelToColor(currentHover)}", Modifier.padding(start = 20.dp, top = 50.dp))
    }
}


@Preview
@Composable
fun PreviewColorCircle() {
    ZygosTheme {
        Surface(Modifier.size(300.dp)) {
            ColorCircle(brightness = 0.4f)
        }
    }
}

@Preview
@Composable
fun PreviewColorSelector() {
    ZygosTheme {
        Surface {
            ColorSelector(
            )
        }
    }
}