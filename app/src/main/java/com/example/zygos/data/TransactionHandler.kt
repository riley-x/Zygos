package com.example.zygos.data

import com.example.zygos.data.database.*

fun addTransaction(
    transaction: Transaction,
    transactionDao: TransactionDao,
    lotDao: LotDao,
) {
    if (transaction.ticker == "CASH") {
        addCashTransaction(transaction, transactionDao, lotDao)
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

private fun updateLot(lot: Lot, t: Transaction, transactionDao: TransactionDao, lotDao: LotDao) {
    val transactionId = transactionDao.insert(t)
    lotDao.update(lot)
    lotDao.insert(LotTransactionCrossRef(
        lotId = lot.lotId,
        transactionId = transactionId
    ))
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
    val transactionId = transactionDao.insert(t)
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
        updateLot(lot, t, transactionDao, lotDao)
    }
}

private fun addDividend(transaction: Transaction, transactionDao: TransactionDao, lotDao: LotDao) {

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
        val transactionId = transactionDao.insert(t)
        updatedLots.forEach {
            lotDao.update(it)
            lotDao.insert(LotTransactionCrossRef(
                lotId = it.lotId,
                transactionId = transactionId
            ))
        }
    } else {
        /** Close a specific lot **/
        if (t.closeLot > lots.lastIndex)
            throw RuntimeException("TransactionHandler::closeLots() trying to close lot ${t.closeLot} out of ${lots.lastIndex} lots")
        val updatedLot = closeShares(-t.shares, lots[t.closeLot], t, true) // t.shares is negative on close
        updateLot(updatedLot, t, transactionDao, lotDao)
    }
}
