package com.example.zygos.data

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.example.zygos.data.database.TransactionType

/**
 * Summarizes the current holdings and returns of a position, whether the entire account or a single
 * lot.
 *
 * @param costBasis Used for % return calculation, the amount of cash needed to open the position
 * @param taxBasis Actual basis used for taxes. Usually the same but could be adjusted for wash sales.
 * @param realizedOpen Realized returns from open positions, i.e. dividends. Note STO proceeds are included in [unrealized]. These returns are included in % return calculations.
 * @param realizedClosed Realized returns from closed positions. These returns are not included in % return calculations.
 * @param unrealized Includes time value from open options positions.
 * @param averageCost Cost per share on purchase. For stocks it's just [costBasis] / [shares], but can be different for options.
 * @param equity Liquidating value of open positions. For stocks it's usually just [costBasis] + [unrealized], but can be different for options.
 * @param cashEffect Effect of this position on net cash. For stocks it's just realized - [costBasis]
 * @param subPositions Constituent positions, if any. For example a stock position can consist of many lot positions.
 * @param collateral For short positions, amount of cash collateral. Not used for covered calls. This is usually [costBasis] + STO proceeds
 */
@Immutable
data class Position(
    val ticker: String,
    val type: TransactionType = TransactionType.NONE,
    val shares: Int = 0,
    val costBasis: Float = 0f,
    val taxBasis: Float = 0f,
    val realizedOpen: Float = 0f,
    val realizedClosed: Float = 0f,
    val unrealized: Float = 0f,
    val averageCost: Float = if (shares > 0) costBasis / shares else 0f,
    val equity: Float = costBasis + unrealized,
    val cashEffect: Float = realizedClosed + realizedOpen - costBasis,
    val subPositions: List<Position> = emptyList(),
    /** Options **/
    val expiration: Int = 0,
    val strike: Float = 0f,
    val collateral: Float = 0f,
) {
    val realized = realizedOpen + realizedClosed
    val returns = realized + unrealized
    val returnsPercent = returns / costBasis
}