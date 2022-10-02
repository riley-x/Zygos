package com.example.zygos.data.database

import androidx.room.*

/**
 * Values are 1/100th of a cent, and dates are in YYYYMMDD format.
 *
 * WARNING: `realizedOpen` and `realizedClosed` have special use for CASH lots!
 */
@Entity(tableName = "lot",
    foreignKeys = [ForeignKey(
        entity = Transaction::class,
        parentColumns = arrayOf("transactionId"),
        childColumns = arrayOf("openTransactionId")
    )]
)
data class Lot(
    @PrimaryKey(autoGenerate = true) val lotId: Int = 0, // 0 to auto generate a key
    val openTransactionId: Int, // this is needed so that queries can test the Lot's account, etc. easily
    val sharesOpen: Int = 0, // for options, also a multiple of 100
    val realizedOpen: Int = 0, // things like dividends and fees, related to open shares
    val realizedClosed: Int = 0, // sum of realized returns of all closed shares
)


@Entity(primaryKeys = ["transactionId", "lotId"])
data class LotTransactionCrossRef(
    val transactionId: Int,
    val lotId: Int
)


data class LotWithTransactions(
    @Embedded val lot: Lot,
    @Relation(
        parentColumn = "lotId",
        entityColumn = "transactionId",
        associateBy = Junction(LotTransactionCrossRef::class)
    )
    val transactions: List<Transaction>
)


@Dao
interface LotDao {

    @Insert
    fun insert(lot: Lot): Long

    @Insert
    fun insert(lotTransactionCrossRef: LotTransactionCrossRef)

    @Update
    fun update(lot: Lot)

//    @Insert
//    fun insertTransactionWithLot(transaction: Transaction, lot: Lot): List<Long>

    @Query("SELECT * FROM lot")
    fun getAll(): List<LotWithTransactions>

    // fully qualified name to not conflict with the transaction class. whole operation is performed atomically
    @androidx.room.Transaction
    @Query("SELECT * FROM lot " +
            "INNER JOIN transaction_table ON transaction_table.transactionId = lot.openTransactionId " +
            "WHERE transaction_table.account = :account OR account = 'All'")
    fun getAll(account: String): List<LotWithTransactions>

    @androidx.room.Transaction
    @Query("SELECT * FROM lot " +
            "INNER JOIN transaction_table ON transaction_table.transactionId = lot.openTransactionId " +
            "WHERE (transaction_table.account = :account OR account = 'All') AND transaction_table.ticker == :ticker")
    fun getTicker(account: String, ticker: String): List<LotWithTransactions>


    @androidx.room.Transaction
    @Query("SELECT COUNT(*) FROM lot " +
            "INNER JOIN transaction_table ON transaction_table.transactionId = lot.openTransactionId " +
            "WHERE transaction_table.account = :account OR account = 'All'")
    fun count(account: String): Int
}