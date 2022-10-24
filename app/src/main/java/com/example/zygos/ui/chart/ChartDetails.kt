package com.example.zygos.ui.chart

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.zygos.network.TdFundamental
import com.example.zygos.ui.components.TitleValue
import com.example.zygos.ui.components.allAccounts
import com.example.zygos.ui.components.formatDollar
import com.example.zygos.ui.components.formatPercent
import com.example.zygos.ui.holdings.PositionDetailsScreen
import com.example.zygos.ui.theme.ZygosTheme
import com.example.zygos.viewModel.Fundamental
import com.example.zygos.viewModel.TestViewModel


/** No multiple receivers in Kotlin yet, so must use factory pattern **/
private fun RowScope.defaultMod() = Modifier
    .weight(10f)
    .padding(bottom = 20.dp)


@Composable
fun ChartDetails(
    data: Fundamental,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        Row {
            TitleValue("Description", data.description, defaultMod())
        }
        Row {
            TitleValue("Dividend Yield", formatPercent(data.dividendYield), defaultMod())
            TitleValue("Dividend Date", data.dividendDate, defaultMod())
        }
        Row {
            TitleValue("P/E", "${data.peRatio}", defaultMod())
            TitleValue("P/CF", "${data.pcfRatio}", defaultMod())
        }
        Row {
            TitleValue("Profit Margin", formatDollar(data.netProfitMarginTTM), defaultMod())
            TitleValue("Operating Margin", formatDollar(data.operatingMarginTTM), defaultMod())
        }
        Row {
            TitleValue("Quick Ratio", "${data.quickRatio}", defaultMod())
            TitleValue("Current Ratio", "${data.currentRatio}", defaultMod())
        }
    }
}


@Preview(
    widthDp = 360,
    heightDp = 740,
    showBackground = true,
)
@Composable
fun PreviewPositionDetailsScreen() {
    val viewModel = viewModel<TestViewModel>()
    ZygosTheme {
        Surface {
            ChartDetails(data = viewModel.chartFundamental.value)
        }
    }
}