package com.example.zygos.viewModel

import android.util.Log
import com.example.zygos.data.*
import com.example.zygos.data.database.LotWithTransactions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LotModel(private val parent: ZygosViewModel) {
    var tickerLots = mutableMapOf<String, Pair<Long, List<LotWithTransactions>>>()
    var cashPosition: Position? = null
    val longPositions = mutableListOf<Position>()
    val shortPositions = mutableListOf<Position>()
    val exitedPositions = mutableListOf<Position>()

    /** This function will block until the above lists are loaded. The actual loading is dispatched
     * though so the main thread (which runs the UI coroutine) isn't blocked too. Note everything
     * can run in a dispatched thread since these aren't state lists **/
    suspend fun loadBlocking(account: String) {
        withContext(Dispatchers.IO) {
            tickerLots = parent.lotDao.getOpenAndRealized(account)
            createLotPositions(account, tickerLots)
        }
    }

    private fun createLotPositions(
        account: String,
        tickerLots: Map<String, Pair<Long, List<LotWithTransactions>>>
    ) {
        cashPosition = null
        longPositions.clear()
        shortPositions.clear()
        exitedPositions.clear()
        tickerLots.forEach { (ticker, pair) ->
            if (ticker == "CASH") {
                cashPosition = getCashPosition(pair.second)
            } else {
                val realizedClosed = pair.first
                val lotPositions = getTickerPositions(pair.second)

                // long positions are collected into a single ticker position that is displayed in watchlist/pie chart
                val tickerLongPositions = mutableListOf<Position>()
                lotPositions.forEach {
                    if (it.type == PositionType.COVERED_CALL || !it.type.isShort) {
                        tickerLongPositions.add(it)
                    } else {
                        shortPositions.add(it)
                    }
                }

                if (tickerLongPositions.isNotEmpty()) {
                    longPositions.add(AggregatePosition(
                        realizedClosedExtra = realizedClosed,
                        subPositions = tickerLongPositions,
                    ))
                } else if (realizedClosed != 0L) {
                    exitedPositions.add(
                        LotPosition(
                            account = account,
                            ticker = ticker,
                            type = PositionType.NONE,
                            realizedClosed = realizedClosed
                        )
                    )
                }
            }
        }
        val cashEffect = longPositions.sumOf(Position::cashEffect) + shortPositions.sumOf(Position::cashEffect) + exitedPositions.sumOf(Position::cashEffect)
        if (cashPosition is LotPosition) {
            cashPosition = (cashPosition as LotPosition).copy(realizedClosed = cashEffect)
        } else if (cashPosition is AggregatePosition) {
            cashPosition = (cashPosition as AggregatePosition).copy(realizedClosedExtra = cashEffect)
        } // null can happen when data is empty
    }

    internal fun logPositions() {
        val n = System.getProperty("line.separator")
        Log.i("Zygos/LotModel/createLotPositions", "$n $cashPosition")
        longPositions.forEach { Log.i("Zygos/LotModel/createLotPositions", "$n $it") }
    }
}