package com.example.zygos.data

import androidx.compose.runtime.Immutable

@Immutable
data class PricedPosition (
    /** Identifiers **/
    val account: String = "",
    val ticker: String = "",
    val type: PositionType = PositionType.NONE,
    val date: Int = 0,
    val instrumentName: String = "",
    /** Per share **/
    val shares: Long = 0,
    val priceOpen: Float = 0f,
    /** Basis and returns **/
    val costBasis: Float = 0f,
    val realizedOpen: Float = 0f,
    val realizedClosed: Float = 0f,
    /** Options **/
    val expiration: Int = 0,
    val strike: Float = 0f,
    val collateral: Float = 0f,
    val priceUnderlyingOpen: Float = 0f,
    /** Price-dependent **/
    val mark: Float = 0f,
    val unrealized: Float = 0f,
    val returnsOpen: Float = 0f,
    val returnsPercent: Float = 0f,
    val returnsTotal: Float = 0f,
    val returnsToday: Float = 0f,
    val returnsTodayPercent: Float = 0f,
    val equity: Float = 0f,
    /** Sub-positions **/
    val subPositions: List<PricedPosition> = emptyList(),
) {
    companion object Factory {
        operator fun invoke(
            lot: Position,
            markPrices: Map<String, Long>,
            closePrices: Map<String, Long>,
            percentChanges: Map<String, Float>,
        ): PricedPosition {
            val realizedOpen = lot.realizedOpen.toFloatDollar()
            val unrealized = lot.unrealized(markPrices).toFloatDollar()
            return PricedPosition(
                /** Identifiers **/
                account = lot.account,
                ticker = lot.ticker,
                type = lot.type,
                date = lot.date,
                instrumentName = lot.instrumentName,
                /** Per share **/
                shares = lot.shares,
                priceOpen = lot.priceOpen.toFloatDollar(),
                /** Basis and returns **/
                costBasis = lot.costBasis.toFloatDollar(),
                realizedOpen = realizedOpen,
                realizedClosed = lot.realizedClosed.toFloatDollar(),
                /** Options **/
                expiration = lot.expiration,
                strike = lot.strike.toFloatDollar(),
                collateral = lot.collateral.toFloatDollar(),
                priceUnderlyingOpen = lot.priceUnderlyingOpen.toFloatDollar(),
                /** Price-dependent **/
                mark = closePrices[lot.instrumentName.ifBlank { lot.ticker }]?.toFloatDollar() ?: 0f, // for aggregate positions, still show the % change of the ticker, if available
                unrealized = unrealized,
                returnsOpen = realizedOpen + unrealized,
                returnsPercent = lot.returnsPercent(markPrices),
                returnsTotal = lot.returns(markPrices).toFloatDollar(),
                returnsToday = lot.returnsPeriod(closePrices, markPrices).toFloatDollar(),
                returnsTodayPercent = percentChanges[lot.instrumentName.ifBlank { lot.ticker }] ?: 0f, // for aggregate positions, still show the % change of the ticker, if available
                equity = lot.equity(markPrices).toFloatDollar(),
                /** Sub-positions **/
                subPositions = lot.subPositions.map { PricedPosition(it, markPrices, closePrices, percentChanges) },
            )
        }
    }
}