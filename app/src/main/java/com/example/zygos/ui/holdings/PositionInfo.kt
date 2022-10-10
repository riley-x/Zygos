package com.example.zygos.ui.holdings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import com.example.zygos.data.LotPosition
import com.example.zygos.data.Position
import com.example.zygos.data.PositionType
import com.example.zygos.data.toFloatDollar
import com.example.zygos.ui.components.formatDateInt
import com.example.zygos.ui.components.formatDollar
import com.example.zygos.ui.components.formatDollarNoSymbol
import com.example.zygos.viewModel.PricedPosition

/**
 * These are the composables that show extra TickerList info
 */

@Composable
fun PositionInfo(
    position: PricedPosition,
    modifier: Modifier = Modifier,
) {
    CompositionLocalProvider(
        LocalContentAlpha provides 0.8f,
        LocalTextStyle provides MaterialTheme.typography.subtitle1
    ) {
        if (position.type == PositionType.STOCK) {
            Column(modifier) {
                Text("${position.shares} shares")
                Text(formatDollar(position.priceOpen))
            }
        } else if (position.type.isSpread) {
            Row(modifier) {
                Column(Modifier.weight(10f)) {
                    Text(position.type.toString())
                    Text("x${position.shares}")
                }
                Column(Modifier.weight(10f)) {
                    Text(formatDateInt(position.expiration))
//                    Text(position.instrumentName.split(' ').last()) // TODO hardcoded double strike. This is too large to display properly
                }
            }
        } else if (position.type.isOption) {
            Row(modifier) {
                Column(Modifier.weight(10f)) {
                    Text(position.type.toString())
                    Text("x${position.shares}")
                }
                Column(Modifier.weight(10f)) {
                    Text(formatDateInt(position.expiration))
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