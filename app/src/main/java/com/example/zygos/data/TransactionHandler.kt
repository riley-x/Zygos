package com.example.zygos.data

import android.util.Log
import com.example.zygos.data.database.*
import kotlin.math.roundToLong


fun recalculateAll(
    transactionDao: TransactionDao,
    lotDao: LotDao,
) {
    lotDao.clear()
    val transactions = transactionDao.getAll()
    transactions.forEach {
        addTransaction(it, transactionDao, lotDao)
    }
}

fun addTransaction(
    transaction: Transaction,
    transactionDao: TransactionDao,
    lotDao: LotDao,
) {
    if (transaction.ticker == "CASH") {
        addCashTransaction(transaction, transactionDao, lotDao)
    } else if (transaction.type == TransactionType.RENAME) {
        // TODO
    } else if (transaction.type == TransactionType.SPINOFF) {
        // TODO
    } else if (transaction.type == TransactionType.SPLIT) {
        // TODO
    } else if (transaction.type == TransactionType.DIVIDEND) {
        addDividend(transaction, transactionDao, lotDao)
    } else if (transaction.shares > 0) {
        createLot(transaction, transactionDao, lotDao)
    } else if (transaction.shares < 0) {
        closeLots(transaction, transactionDao, lotDao)
    } else {
        throw RuntimeException("Unable to handle transaction $transaction")
    }
}

/** A transaction might have its id set already if being called from [recalculateAll] **/
private fun getIdOrInsert(t: Transaction, transactionDao: TransactionDao): Long {
    if (t.transactionId == 0L) return transactionDao.insert(t)
    return t.transactionId
}

private fun updateLotWithTransaction(lot: Lot, t: Transaction, transactionDao: TransactionDao, lotDao: LotDao) {
    val transactionId = getIdOrInsert(t, transactionDao)
    lotDao.update(lot)
    lotDao.insert(LotTransactionCrossRef(
        lotId = lot.lotId,
        transactionId = transactionId
    ))
}

private fun updateLotsWithTransaction(lots: List<Lot>, t: Transaction, transactionDao: TransactionDao, lotDao: LotDao) {
    val transactionId = getIdOrInsert(t, transactionDao)
    lots.forEach {
        lotDao.update(it)
        lotDao.insert(LotTransactionCrossRef(
            lotId = it.lotId,
            transactionId = transactionId
        ))
    }
}

private fun createLot(t: Transaction, transactionDao: TransactionDao, lotDao: LotDao) {
    val lot = Lot(
        account = t.account,
        ticker = t.ticker,
        sharesOpen = if (t.ticker == "CASH") t.value else t.shares,
        feesAndRounding = t.feesAndRounding,
        dividendsPerShare = 0,
        realizedClosed = 0,
    )
    val transactionId = getIdOrInsert(t, transactionDao)
    val lotId = lotDao.insert(lot)
    lotDao.insert(LotTransactionCrossRef(
        lotId = lotId,
        transactionId = transactionId
    ))
}

/** Handles all CASH transactions **/
private fun addCashTransaction(
    t: Transaction,
    transactionDao: TransactionDao,
    lotDao: LotDao
) {
    val lots = lotDao.getTicker(t.account, t.ticker)
    if (lots.size > 1) throw RuntimeException("Found ${lots.size} cash lots in account ${t.account}")
    if (lots.isEmpty()) {
        /** Initial deposit **/
        if (t.type != TransactionType.TRANSFER || t.value < 0) throw RuntimeException("No lots found in account ${t.account}")
        createLot(t, transactionDao, lotDao)
    } else {
        /** Update existing lot **/
        val lotOld = lots[0].lot
        val lot = when (t.type) {
            TransactionType.TRANSFER -> lotOld.copy(sharesOpen = lotOld.sharesOpen + t.value)
            else -> lotOld.copy(realizedClosed = lotOld.realizedClosed + t.value)
        }
        updateLotWithTransaction(lot, t, transactionDao, lotDao)
    }
}

private fun addDividend(t: Transaction, transactionDao: TransactionDao, lotDao: LotDao) {
    val lots = lotDao.getTicker(t.account, t.ticker)
    // Must get all lots, since a lot could be closed between the ex date and the payout date.
    // These should be ordered by rowId already.

    var unmatchedShares = (t.value.toDouble() / t.price).roundToLong()
    val roundingError = unmatchedShares * t.price - t.value
    val updatedLots = mutableListOf<Lot>()

    /** Match LIFO **/
    for (lot in lots.reversed()) {
        if (lot.openTransaction.date >= t.expiration) continue

        /** Check if there were any shares closed after the ex date **/
        val sharesOpen = lot.lot.sharesOpen
        var sharesClosed = 0L
        lot.transactions.forEach {
            if (it.type == TransactionType.STOCK && it.shares < 0 && it.date >= t.expiration) {
                sharesClosed -= it.shares
            }
        }
        Log.d("Zygos/Data/TransactionHandler", "sharesOpen=${sharesOpen} sharesClosed=${sharesClosed}, lot=${lot.lot}")
        if (sharesOpen + sharesClosed == 0L) continue

        /** Get updated lot. Include rounding errors if exhausted **/
        unmatchedShares -= sharesOpen + sharesClosed
        updatedLots.add(lot.lot.copy(
            dividendsPerShare = lot.lot.dividendsPerShare + t.price,
            realizedClosed = lot.lot.realizedClosed + t.price * sharesClosed,
            feesAndRounding = lot.lot.feesAndRounding + if (unmatchedShares == 0L) roundingError else 0
        ))
    }
    if (unmatchedShares != 0L) throw RuntimeException("Zygos/TransactionHandler::addDividend() unmatchedShares=$unmatchedShares, from $t")

    /** Update the database **/
    updateLotsWithTransaction(updatedLots, t, transactionDao, lotDao)
}


private fun closeShares(shares: Long, lot: LotWithTransactions, t: Transaction, includeFees: Boolean): Lot {
    if (shares > lot.lot.sharesOpen)
        throw RuntimeException("TransactionHandler::closeLots() trying to close $shares shares from a lot with only ${lot.lot.sharesOpen} shares")

    val stockRealized = shares * (t.price - lot.openTransaction.price) * if (t.type.isShort) -1 else 1
    val dividendRealized = shares * lot.lot.dividendsPerShare
    return lot.lot.copy(
        sharesOpen = lot.lot.sharesOpen - shares,
        feesAndRounding = lot.lot.feesAndRounding + if (includeFees) t.feesAndRounding else 0,
        realizedClosed = lot.lot.realizedClosed + stockRealized + dividendRealized,
    )
}


private fun closeLots(t: Transaction, transactionDao: TransactionDao, lotDao: LotDao) {
    val lots = lotDao.getOpen(t.account, t.ticker) // these should be ordered by rowId already
    if (t.closeLot < 0L) {
        /** Match lots FIFO **/
        var unmatchedShares = -t.shares // t.shares is negative on close
        val updatedLots = mutableListOf<Lot>()
        for (lot in lots) {
            val shares = minOf(unmatchedShares, lot.lot.sharesOpen)
            updatedLots.add(closeShares(shares, lot, t, shares == unmatchedShares))
            unmatchedShares -= shares
            if (unmatchedShares <= 0L) break
        }
        if (unmatchedShares != 0L) throw RuntimeException("TransactionHandler::closeLots() unable to close $t")

        /** Update the database **/
        updateLotsWithTransaction(updatedLots, t, transactionDao, lotDao)
    } else {
        /** Close a specific lot **/
        if (t.closeLot > lots.lastIndex)
            throw RuntimeException("TransactionHandler::closeLots() trying to close lot ${t.closeLot} out of ${lots.lastIndex} lots")
        val updatedLot = closeShares(-t.shares, lots[t.closeLot], t, true) // t.shares is negative on close
        updateLotWithTransaction(updatedLot, t, transactionDao, lotDao)
    }
}
