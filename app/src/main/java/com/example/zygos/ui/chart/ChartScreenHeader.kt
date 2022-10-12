package com.example.zygos.ui.chart

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.zygos.ui.components.TickerSelector
import com.example.zygos.ui.theme.ZygosTheme

@Composable
fun ChartScreenHeader(
    ticker: String,
    hoverTime: String,
    hoverValues: String,
    modifier: Modifier = Modifier,
    onTickerChanged: (String) -> Unit = { },
) {
    // Ticker selection bar, also chart hover text goes here to save space
    Row(
        verticalAlignment = Alignment.Bottom,
        modifier = modifier
            .height(43.dp)
            // There seems to be a minimum height that enables the droplet selection.
            // 42.dp doesn't work but 43.dp does
            .fillMaxWidth()
    ) {
        // The weights fix the ticker selector to the first 1/3
        TickerSelector(
            ticker = ticker,
            onTickerGo = onTickerChanged,
            modifier = Modifier
                .weight(1f)
        )

        // Hover text uses up the last 2/3, aligned bottom-right
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.End,
            modifier = Modifier
                .weight(2f)
        ) {
            Text(
                text = hoverTime,
                style = MaterialTheme.typography.overline,
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = hoverValues,
                style = MaterialTheme.typography.overline,
            )
        }
    }
}


@Preview
@Composable
fun PreviewChartScreenHeader() {
    ZygosTheme {
        Surface {
            ChartScreenHeader(
                ticker = "MSFT",
                hoverTime = "9/27/22",
                hoverValues = "O: 34.23  H: 36.43\nC: 35.02  L: 33.98"
            )
        }
    }
}