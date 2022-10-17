package com.example.zygos.ui.holdings

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.zygos.data.PositionType
import com.example.zygos.data.toFloatDollar
import com.example.zygos.ui.components.formatDateInt
import com.example.zygos.ui.components.formatDollar
import com.example.zygos.ui.components.formatDollarNoSymbol
import com.example.zygos.ui.components.formatPercent
import com.example.zygos.data.PricedPosition

/**
 * These are the composables that show TickerListRow info
 */


@Composable
fun ColoredDollar(
    value: Float,
    modifier: Modifier = Modifier,
) {
    Text(
        text = formatDollar(value),
        color = if (value >= 0) MaterialTheme.colors.primary else MaterialTheme.colors.error,
        modifier = modifier
    )
}

@Composable
fun ColoredPercent(
    value: Float,
    modifier: Modifier = Modifier,
) {
    if (!value.isNaN()) {
        Text(
            text = formatPercent(value),
            color = if (value >= 0) MaterialTheme.colors.primary else MaterialTheme.colors.error,
            modifier = modifier
        )
    }
}


@Composable
fun PositionRowInfo(
    position: PricedPosition,
    displayOption: HoldingsListOptions,
    modifier: Modifier = Modifier,
) {
    when (displayOption) {
        HoldingsListOptions.EQUITY -> Text(formatDollar(position.equity), modifier)
        HoldingsListOptions.RETURNS -> ColoredDollar(position.returnsTotal, modifier)
        HoldingsListOptions.RETURNS_PERCENT -> ColoredPercent(position.returnsPercent, modifier)
        HoldingsListOptions.RETURNS_TODAY -> ColoredDollar(position.returnsToday, modifier)
        HoldingsListOptions.RETURNS_PERCENT_TODAY -> ColoredPercent(position.returnsTodayPercent, modifier)
        HoldingsListOptions.MARK -> Text(formatDollar(position.mark), modifier)
        else -> Unit
    }
}


@Composable
fun PositionRowSubInfo(
    position: PricedPosition,
    modifier: Modifier = Modifier,
) {
    CompositionLocalProvider(
        LocalContentAlpha provides ContentAlpha.medium,
        LocalTextStyle provides MaterialTheme.typography.subtitle1
    ) {
        if (position.type == PositionType.STOCK) {
            Column(modifier) {
                Text("x${position.shares}")
                Text(formatDollar(position.priceOpen))
            }
        } else if (position.type.isSpread) {
            Row(modifier) {
                Column(Modifier.width(100.dp)) {
                    Text(position.type.toString())
                    Text("x${position.shares}")
                }
                Column {
                    Text(formatDateInt(position.expiration))
//                    Text(position.instrumentName.split(' ').last()) // TODO hardcoded double strike. This is too large to display properly
                }
            }
        } else if (position.type.isOption) {
            Row(modifier) {
                Column(Modifier.width(100.dp)) {
                    Text(position.type.toString())
                    Text(formatDateInt(position.expiration))
                }
                Column {
                    Text("x${position.shares}")
                    Text(formatDollarNoSymbol(position.strike))
                }
            }
        } else if (position.type == PositionType.CASH) {
            Column(modifier) {
                Text(formatDollar(position.shares.toFloatDollar()))
                Text(position.account)
            }
        }
    }
}

