package com.example.zygos.ui.holdings

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
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
private fun ValueOrPercent(
    value: Float,
    percent: Float,
    showPercentages: State<Boolean>,
    modifier: Modifier = Modifier,
    isColor: Boolean = true,
) {
    val color =
        if (!isColor) MaterialTheme.colors.onSurface
        else if ((showPercentages.value && percent >= 0) || (!showPercentages.value && value >= 0)) MaterialTheme.colors.primary
        else MaterialTheme.colors.error

    Text(
        text = if (showPercentages.value) formatPercent(percent) else formatDollar(value),
        color = color,
        modifier = modifier
    )
}


@Composable
private fun ValuePair(
    value: Float,
    percent: Float,
    showPercentages: State<Boolean>,
    subValue: Float,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.End,
        modifier = modifier
    ) {
        ValueOrPercent(value, percent, showPercentages)
        Text(
            text = if (value.isNaN()) "" else formatDollarNoSymbol(subValue),
            color = MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.medium),
            style = MaterialTheme.typography.subtitle1
        )
    }
}


@Composable
fun PositionRowInfo(
    position: PricedPosition,
    displayOption: HoldingsListDisplayOptions,
    showPercentages: State<Boolean>,
    modifier: Modifier = Modifier,
) {
    when (displayOption) {
        HoldingsListDisplayOptions.EQUITY -> ValuePair(
            value = position.equity,
            percent = position.equity,
            showPercentages = showPercentages,
            subValue = position.mark,
            modifier = modifier,
        ) // TODO equity %
        HoldingsListDisplayOptions.RETURNS_TOTAL -> ValueOrPercent(
            value = position.returnsTotal,
            percent = position.returnsPercent,
            showPercentages = showPercentages,
            modifier = modifier
        )
        HoldingsListDisplayOptions.RETURNS_TODAY -> ValueOrPercent(
            value = position.returnsToday,
            percent = position.returnsTodayPercent,
            showPercentages = showPercentages,
            modifier = modifier
        )
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
            Column(modifier) {
                Text("${position.type.displayName} x${position.shares}")
                Text("${formatDateInt(position.expiration)} - ${formatDollarNoSymbol(position.strike)}")
            }
        } else if (position.type == PositionType.CASH) {
            Column(modifier) {
                Text(formatDollar(position.shares.toFloatDollar()))
                Text(position.account)
            }
        }
    }
}

