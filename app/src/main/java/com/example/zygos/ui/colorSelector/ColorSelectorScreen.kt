package com.example.zygos.ui.colorSelector

import android.view.MotionEvent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.BrightnessHigh
import androidx.compose.material.icons.sharp.BrightnessLow
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import androidx.core.math.MathUtils
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.zygos.ui.components.LogCompositions
import com.example.zygos.ui.theme.ZygosTheme
import com.example.zygos.viewModel.TestViewModel
import kotlin.math.*

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ColorSelectorScreen(
    modifier: Modifier = Modifier,
    initialColor: Color = Color.White,
    onSave: (Color) -> Unit = { },
    onCancel: () -> Unit = { },
) {
    LogCompositions("Zygos", "ColorSelectorScreen")

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    fun clearFocus() {
        keyboardController?.hide()
        focusManager.clearFocus(true)
    }

    var boxSize by remember { mutableStateOf(IntSize(10, 10)) } // 960 x 1644

    var currentColor by remember { mutableStateOf(initialColor) }
//    var currentHover by remember { mutableStateOf(colorToPixel(initialColor)) }
    var currentHover by remember(boxSize) { mutableStateOf(Offset(boxSize.width / 2f, boxSize.height / 2f)) }
    var brightness by remember { mutableStateOf(1.0f) }

    fun toPolar(point: Offset, clamp: Boolean = false): Pair<Float, Float> {
        val x = point.x - boxSize.width / 2
        val y = point.y - boxSize.height / 2
        var r = 2 * sqrt(x * x + y * y) / minOf(boxSize.width, boxSize.height)
        if (clamp) r = MathUtils.clamp(r, 0f, 1f)

        var phi = Math.toDegrees(atan2(y, x).toDouble()).toFloat()
        if (phi < 0) phi += 360
        return Pair(r, phi)
    }
    fun fromPolar(r: Float, phi: Float): Offset {
        val minSize = minOf(boxSize.width, boxSize.height) / 2f
        val phiRad = Math.toRadians(phi.toDouble()).toFloat()
        val x = r * cos(phiRad) * minSize + boxSize.width / 2f
        val y = r * sin(phiRad) * minSize + boxSize.height / 2f
        return Offset(x, y)
    }




//    fun colorToPixel(color: Color): Offset {
//
//    }

    fun pixelToColor(point: Offset): Color {
        val (r, phi) = toPolar(point, clamp = true)
        val stopIndex = phi.toInt() / 60

        val hue = ColorUtils.blendARGB(
            colors.items[stopIndex].toArgb(),
            colors.items[stopIndex + 1].toArgb(),
            phi / 60 - stopIndex,
        )
        val sat = ColorUtils.blendARGB(Color.White.toArgb(), hue, r)
        return Color(ColorUtils.blendARGB(Color.Black.toArgb(), sat, brightness))
    }


    var red by remember { mutableStateOf((currentColor.red * 255).roundToInt().toString())}
    var green by remember { mutableStateOf((currentColor.green * 255).roundToInt().toString())}
    var blue by remember { mutableStateOf((currentColor.blue * 255).roundToInt().toString())}


    fun updateSelection(red: String, green: String, blue: String) {
        val r = red.toIntOrNull()
        val g = green.toIntOrNull()
        val b = blue.toIntOrNull()
        if (r == null || g == null || b == null) return
        if (r !in 0..255 || g !in 0..255 || b !in 0..255) return
        currentColor = Color(red.toInt(), green.toInt(), blue.toInt())
//        currentHover = ...
//        brightness =
    }
    fun updateStrings(color: Color) {
        red = (color.red * 255).roundToInt().toString()
        green = (color.green * 255).roundToInt().toString()
        blue = (color.blue * 255).roundToInt().toString()
    }

    val indicatorRadius = with(LocalDensity.current) { 10.dp.toPx() }
    val indicatorWidth = with(LocalDensity.current) { 5.dp.toPx() }
    val indicatorColor = remember { derivedStateOf {
        if (brightness > 0.5) Color.Black else Color.White
    } }

    /** The circle indicator should be moved by dragging instead of tapping. Cache the last position
     * from ACTION_MOVE or ACTION_DOWN **/
    var dragStart by remember { mutableStateOf(currentHover) }


    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures(onPress = { clearFocus() })
            }
    ) {

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .padding(start = 30.dp, end = 30.dp)
                .aspectRatio(1f)
                .fillMaxWidth()
                .onGloballyPositioned {
                    boxSize = it.size
                }
                .pointerInteropFilter { motionEvent ->
                    when (motionEvent.action) {
                        MotionEvent.ACTION_DOWN -> {
                            dragStart = Offset(motionEvent.x, motionEvent.y)
                            clearFocus()
                        }
                        MotionEvent.ACTION_MOVE -> {
                            var newPos =
                                currentHover + Offset(motionEvent.x, motionEvent.y) - dragStart
                            val (r, phi) = toPolar(newPos)
                            if (r > 1f) newPos = fromPolar(1f, phi)

                            currentHover = newPos
                            currentColor = pixelToColor(currentHover)
                            updateStrings(currentColor)
                            dragStart = Offset(motionEvent.x, motionEvent.y)
                        }
                    }
                    true
                }
        ) {
            ColorCircle(brightness = brightness)
            Canvas(Modifier.fillMaxSize()) {
                drawCircle(
                    color = currentColor,
                    radius = indicatorRadius,
                    center = currentHover,
                )
                drawCircle(
                    color = indicatorColor.value,
                    radius = indicatorRadius,
                    center = currentHover,
                    style = Stroke(indicatorWidth),
                )
            }
        }

        BrightnessSelector(
            brightness = brightness,
            onChange = {
                brightness = it
                currentColor = pixelToColor(currentHover)
                updateStrings(currentColor)
                clearFocus()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp, horizontal = 20.dp)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(30.dp),
            modifier = Modifier.padding(horizontal = 20.dp)
        ) {
            val keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                autoCorrect = false,
                imeAction = ImeAction.Next,
            )

            OutlinedTextField(
                value = red,
                label = { Text("Red") },
                onValueChange = {
                    if (it.length <= 3) {
                        red = it
                        updateSelection(red, green, blue)
                    }
                },
                colors = textFieldColors(MaterialTheme.colors.error),
                keyboardOptions = keyboardOptions,
                modifier = Modifier.weight(10f)
            )
            OutlinedTextField(
                value = green,
                label = { Text("Green") },
                onValueChange = {
                    if (it.length <= 3) {
                        green = it
                        updateSelection(red, green, blue)
                    }
                },
                colors = textFieldColors(MaterialTheme.colors.primary),
                keyboardOptions = keyboardOptions,
                modifier = Modifier.weight(10f)
            )
            OutlinedTextField(
                value = blue,
                label = { Text("Blue") },
                onValueChange = {
                    if (it.length <= 3) {
                        blue = it
                        updateSelection(red, green, blue)
                    }
                },
                colors = textFieldColors(Color(83, 117, 218, 255)),
                keyboardOptions = keyboardOptions,
                modifier = Modifier.weight(10f)
            )
        }
    }
}

@Composable
fun textFieldColors(c: Color) = TextFieldDefaults.outlinedTextFieldColors(
    focusedBorderColor = c.copy(alpha = ContentAlpha.high),
    unfocusedBorderColor = c.copy(alpha = ContentAlpha.disabled),
    focusedLabelColor = c.copy(alpha = ContentAlpha.high),
    unfocusedLabelColor = c.copy(alpha = ContentAlpha.medium),
)


@Preview(
    widthDp = 360,
    heightDp = 740,
)
@Composable
fun PreviewColorSelectorScreen() {
    val viewModel = viewModel<TestViewModel>()
    ZygosTheme {
        Surface {
            ColorSelectorScreen(
            )
        }
    }
}