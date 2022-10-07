package com.example.zygos.viewModel

import android.util.Log
import com.example.zygos.data.LotPosition
import com.example.zygos.data.PositionType
import com.example.zygos.data.database.LotWithTransactions
import com.example.zygos.data.getCashPosition
import com.example.zygos.data.getTickerPositions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LotModel(private val parent: ZygosViewModel) {
    var tickerLots = mutableMapOf<String, Pair<Long, List<LotWithTransactions>>>()
    var cashPosition = LotPosition()
    val longPositions = mutableListOf<LotPosition>()
    val shortPositions = mutableListOf<LotPosition>()
    val exitedPositions = mutableListOf<LotPosition>()

    /** This function will block until the above lists are loaded. The actual loading is dispatched
     * though so the main thread (which runs the UI coroutine) isn't blocked too. **/
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
        longPositions.clear()
        shortPositions.clear()
        exitedPositions.clear()
        tickerLots.forEach { (ticker, pair) ->
            if (ticker == "CASH") {
                cashPosition = getCashPosition(pair.second.first().lot)
            } else {
                val realizedClosed = pair.first
                val lotPositions = getTickerPositions(pair.second)

                // long positions are collected into a single ticker position that is displayed in watchlist/pie chart
                var longPosition: LotPosition? = null
                lotPositions.forEach {
                    if (it.type == PositionType.COVERED_CALL || !it.type.isShort) {
                        longPosition = if (longPosition == null) it else longPosition!! + it
                    } else {
                        shortPositions.add(it)
                    }
                }

                if (longPosition != null) {
                    longPosition = longPosition!!.copy(realizedClosed = realizedClosed)
                    longPositions.add(longPosition!!)
                } else {
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
        cashPosition = cashPosition.copy(realizedClosed =
                longPositions.sumOf(LotPosition::cashEffect) +
                shortPositions.sumOf(LotPosition::cashEffect) +
                exitedPositions.sumOf(LotPosition::cashEffect)
        )
    }
}