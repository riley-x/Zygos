package com.example.zygos.ui.chart

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.zygos.ui.components.*
import com.example.zygos.ui.graphing.TimeSeriesGraph
import com.example.zygos.ui.graphing.TimeSeriesGraphSelector
import com.example.zygos.ui.graphing.candlestickGraph
import com.example.zygos.ui.theme.ZygosTheme
import com.example.zygos.viewModel.*


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ChartScreen(
    ticker: State<String>,
    chartState: State<ChartState>,
    chartRange: State<String>, // must pass state here for button group to calculate derivedStateOf
    modifier: Modifier = Modifier,
    onChartRangeSelected: (String) -> Unit = { },
    onTickerChanged: (String) -> Unit = { },
    onChangeColor: () -> Unit = { },
    accountSelectionBar: @Composable () -> Unit = { },
) {
    LogCompositions("Zygos", "ChartScreen")

    /** Keyboard focus controls. Clears focus from the TextField in the ticker selector when you
     * tap elsewhere
     */
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    fun onGraphPress() {
        keyboardController?.hide()
        focusManager.clearFocus(true)
    }
    fun onChartRangeTap(selection: String) {
        keyboardController?.hide()
        focusManager.clearFocus(true)
        onChartRangeSelected(selection)
    }

    var hoverTime by remember { mutableStateOf("") }
    var hoverValues by remember { mutableStateOf("") }

    fun onGraphHover(isHover: Boolean, x: Int, y: Float) {
        if (isHover && x >= 0 && x < chartState.value.values.size) {
            hoverTime = chartState.value.values[x].name
            val open = formatDollarNoSymbol(chartState.value.values[x].open)
            val close = formatDollarNoSymbol(chartState.value.values[x].close)
            val high = formatDollarNoSymbol(chartState.value.values[x].high)
            val low = formatDollarNoSymbol(chartState.value.values[x].low)
            val maxLength = maxOf(open.length, close.length, high.length, low.length)
            // can set a flag here to disable the hoverTime if length is too long
            hoverValues = "O: " + open.padStart(maxLength) +
                    "  H: " + high.padStart(maxLength) +
                    "\nC: " + close.padStart(maxLength) +
                    "  L: " + low.padStart(maxLength)
        } else {
            hoverTime = ""
            hoverValues = ""
        }
    }

    /** Nonscrolling column for ticker selection header bar **/
    Column(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { // onTap
                        keyboardController?.hide()
                        focusManager.clearFocus(true)
                    }
                )
            }
    ) {
        accountSelectionBar()

        /** Ticker selection bar, also chart hover text goes here to save space **/
        ChartScreenHeader(
            ticker = ticker.value,
            hoverTime = hoverTime,
            hoverValues = hoverValues,
            onTickerChanged = onTickerChanged,
            modifier = Modifier.padding(start = 12.dp),
        )

        /** Main screen with chart and details **/
        LazyColumn {

            item("graph") {
                TimeSeriesGraph(
                    grapher = candlestickGraph(),
                    state = chartState,
                    onHover = ::onGraphHover,
                    onPress = ::onGraphPress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp)
                        .height(400.dp)
                        .clipToBounds()
                )
            }

            item("divider1") {
                Divider(
                    color = MaterialTheme.colors.onBackground.copy(alpha = 0.2f),
                    thickness = 1.dp,
                    modifier = Modifier
                        .padding(start = 12.dp, end = 12.dp, top = 10.dp, bottom = 2.dp)
                )
            }

            item("graph_selector") {
                TimeSeriesGraphSelector(
                    options = chartRangeOptions,
                    currentSelection = chartRange,
                    onSelection = ::onChartRangeTap,
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .fillMaxWidth()
                )
            }

            item("divider2") {
                Divider(
                    color = MaterialTheme.colors.onBackground.copy(alpha = 0.2f),
                    thickness = 1.dp,
                    modifier = Modifier
                        .padding(start = 12.dp, end = 12.dp, top = 2.dp, bottom = 20.dp)
                )
            }

            item("select color button") {
                TextButton(
                    onClick = onChangeColor,
                    border = BorderStroke(2.dp, MaterialTheme.colors.error),
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colors.error
                    ),
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(vertical = 4.dp)
                ) {
                    Text("Change Color")
                }
            }
        }
    }
}


@Preview(
    widthDp = 360,
    heightDp = 740,
    showBackground = true,
)
@Composable
fun PreviewChartScreen() {
    val viewModel = viewModel<TestViewModel>()
    ZygosTheme {
        Surface {
            ChartScreen(
                ticker = viewModel.chartTicker,
                chartState = viewModel.chartState,
                chartRange = viewModel.accountPerformanceTimeRange,
            )
        }
    }
}