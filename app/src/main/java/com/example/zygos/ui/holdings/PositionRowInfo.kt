package com.example.zygos.ui.holdings

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
private fun ValuePair(
    value: Float,
    subValue: Float,
    modifier: Modifier = Modifier,
    isReturns: Boolean = true,
) {
    val valueColor =
        if (!isReturns) MaterialTheme.colors.onSurface
        else if (value >= 0) MaterialTheme.colors.primary
        else MaterialTheme.colors.error
    val subValueColor = valueColor.copy(alpha = ContentAlpha.medium)

    Column(
        horizontalAlignment = Alignment.End,
        modifier = modifier
    ) {
        Text(
            text = formatDollar(value),
            color = valueColor,
        )
        Text(
            text = if (value.isNaN()) "" else if (isReturns) formatPercent(subValue) else formatDollarNoSymbol(subValue),
            color = subValueColor,
            style = MaterialTheme.typography.subtitle1
        )
    }
}



@Composable
fun PositionRowInfo(
    position: PricedPosition,
    displayOption: HoldingsListDisplayOptions,
    modifier: Modifier = Modifier,
) {
    when (displayOption) {
        HoldingsListDisplayOptions.EQUITY -> ValuePair(position.equity, position.mark, modifier, isReturns = false)
        HoldingsListDisplayOptions.RETURNS_TOTAL -> ValuePair(position.returnsTotal, position.returnsPercent, modifier)
        HoldingsListDisplayOptions.RETURNS_TODAY -> ValuePair(position.returnsToday,position.returnsTodayPercent,  modifier)
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

