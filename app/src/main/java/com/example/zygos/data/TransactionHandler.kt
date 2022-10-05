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
        openLot(transaction, transactionDao, lotDao)
    } else if (transaction.shares < 0) {
        closeLot(transaction, transactionDao, lotDao)
    } else {
        throw RuntimeException("Unable to handle transaction $transaction")
    }
}

fun createLot(t: Transaction, transactionDao: TransactionDao, lotDao: LotDao) {
    val transactionId = transactionDao.insert(t)
    val lot = Lot(
        account = t.account,
        ticker = t.ticker,
        sharesOpen = if (t.ticker == "CASH") t.value else t.shares,
        realizedClosed = 0,
    )
    val lotId = lotDao.insert(lot)
    lotDao.insert(LotTransactionCrossRef(
        lotId = lotId,
        transactionId = transactionId
    ))
}

/**
 * Handles all CASH transactions
 */
fun addCashTransaction(
    t: Transaction,
    transactionDao: TransactionDao,
    lotDao: LotDao
) {
    val lots = lotDao.getTicker(t.account, t.ticker)
    if (lots.size > 1) throw RuntimeException("Found ${lots.size} cash lots in account ${t.account}")
    if (lots.isEmpty()) {
        if (t.type != TransactionType.TRANSFER || t.value < 0) throw RuntimeException("No lots found in account ${t.account}")
        createLot(t, transactionDao, lotDao)
    } else {
        /** Update current lot **/
        val lotOld = lots[0].lot
        val lot = when (t.type) {
            TransactionType.TRANSFER -> lotOld.copy(sharesOpen = lotOld.sharesOpen + t.value)
            else -> lotOld.copy(realizedClosed = lotOld.realizedClosed + t.value)
        }

        /** Update tables **/
        val transactionId = transactionDao.insert(t)
        lotDao.insert(LotTransactionCrossRef(
            lotId = lots[0].lot.lotId,
            transactionId = transactionId.toLong()
        ))
        lotDao.update(lot)
    }
}

fun addDividend(transaction: Transaction, transactionDao: TransactionDao, lotDao: LotDao) {

}



fun openLot(transaction: Transaction, transactionDao: TransactionDao, lotDao: LotDao) {

}

fun closeLot(transaction: Transaction, transactionDao: TransactionDao, lotDao: LotDao) {

}
