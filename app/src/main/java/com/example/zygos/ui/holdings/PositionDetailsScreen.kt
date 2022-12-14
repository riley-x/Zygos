package com.example.zygos.ui.holdings

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.CandlestickChart
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.zygos.data.PositionType
import com.example.zygos.data.toFloatDollar
import com.example.zygos.ui.components.TitleValue
import com.example.zygos.ui.components.allAccounts
import com.example.zygos.ui.components.formatDateInt
import com.example.zygos.ui.components.formatDollar
import com.example.zygos.ui.theme.ZygosTheme
import com.example.zygos.data.PricedPosition
import com.example.zygos.viewModel.TestViewModel


/** No multiple receivers in Kotlin yet, so must use factory pattern **/
private fun RowScope.defaultMod() = Modifier
    .weight(10f)
    .padding(bottom = 20.dp)

/** This functions draws the details for the top-level position only **/
// TODO why does this not have a Column? but it works still?
@Composable
private fun PositionDetails(
    position: PricedPosition
) {
    Row {
        TitleValue("Type", position.type.displayName, defaultMod())
        TitleValue("Account", position.account.ifEmpty { allAccounts }, defaultMod())
    }
    if (position.type.isOption) {
        Row {
            TitleValue("Expiration", formatDateInt(position.expiration), defaultMod())
            TitleValue("Strike", formatDollar(position.strike), defaultMod())
        }
    }
    if (position.type == PositionType.CASH) {
        Row {
            TitleValue("Contributions", formatDollar(position.shares.toFloatDollar()), defaultMod())
            TitleValue("First Funded", formatDateInt(position.date), defaultMod())
        }
        Row {
            TitleValue("Available", formatDollar(position.equity), defaultMod())
            TitleValue("Interest Earned", formatDollar(position.realizedOpen), defaultMod())
        }
    } else {
        Row {
            TitleValue("Shares", position.shares.toString(), defaultMod())
            TitleValue("Date", formatDateInt(position.date), defaultMod())
        }
        Row {
            TitleValue("Price Open", formatDollar(position.priceOpen), defaultMod())
            if (position.type.isOption) {
                TitleValue("Underlying Open", formatDollar(position.priceUnderlyingOpen), defaultMod())
            } else {
                TitleValue("Equity", formatDollar(position.equity), defaultMod())
            }
        }
        Row {
            if (position.type.isOption) {
                TitleValue("Equity", formatDollar(position.equity), defaultMod())
                TitleValue("Unrealized", formatDollar(position.unrealized), defaultMod())
            } else {
                TitleValue("Unrealized", formatDollar(position.unrealized), defaultMod())
                TitleValue("Dividends", formatDollar(position.realizedOpen), defaultMod())
            }
        }
    }
}


@Composable
fun PositionDetailsScreen(
    position: PricedPosition,
    colors: SnapshotStateMap<String, Color>,
    bottomPadding: Dp = 0.dp,
    onChangeColor: (String) -> Unit = { },
    toChart: (String) -> Unit = { },
) {
    Column(
        modifier = Modifier
            .padding(bottom = bottomPadding, top = 20.dp)
            .fillMaxSize(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(bottom = 10.dp)
                .padding(horizontal = 20.dp)
        ) {
            Text(position.ticker, style = MaterialTheme.typography.h2, modifier = Modifier.weight(10f))

            IconButton(
                onClick = { toChart(position.ticker) },
                modifier = Modifier.padding(end = 10.dp)
            ) {
                Icon(Icons.Sharp.CandlestickChart, "", Modifier.size(30.dp))
            }

            Canvas(modifier = Modifier
                .size(30.dp)
                .clickable { onChangeColor(position.ticker) }
            ) {
                drawRect(color = colors.getOrDefault(position.ticker, Color.White))
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 20.dp)
        ) {
            if (position.subPositions.isNotEmpty()) {
                itemsIndexed(position.subPositions) { i, pos ->
                    if (i > 0) {
                        Divider(
                            color = MaterialTheme.colors.onBackground.copy(alpha = ContentAlpha.medium),
                            thickness = 1.dp,
                            modifier = Modifier
                                .padding(bottom = 20.dp)
                        )
                    }
                    PositionDetails(pos)
                }
            } else {
                item {
                    PositionDetails(position)
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
fun PreviewPositionDetailsScreen() {
    val viewModel = viewModel<TestViewModel>()
    ZygosTheme {
        Surface {
            PositionDetailsScreen(
                position = viewModel.longPositions[0],
                colors = viewModel.tickerColors
            )
        }
    }
}