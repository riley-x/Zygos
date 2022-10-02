package com.example.zygos.data

import androidx.room.*

/**
 * Values are 1/100th of a cent, and dates are in YYYYMMDD format.
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
    val isOpen: Boolean = true,
    val realizedReturns: Int = 0,
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

    // fully qualified name to not conflict with the transaction class. whole operation is performed atomically
    @androidx.room.Transaction
    @Query("SELECT * FROM lot")
    fun getAll(): List<LotWithTransactions>

    @androidx.room.Transaction
    @Query("SELECT * FROM lot " +
            "INNER JOIN transaction_table ON transaction_table.transactionId = lot.openTransactionId " +
            "WHERE transaction_table.account = :account OR account = 'All'")
    fun getAll(account: String): List<LotWithTransactions>

    @androidx.room.Transaction
    @Query("SELECT COUNT(*) FROM lot " +
            "INNER JOIN transaction_table ON transaction_table.transactionId = lot.openTransactionId " +
            "WHERE transaction_table.account = :account OR account = 'All'")
    fun count(account: String): Int
}