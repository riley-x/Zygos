package com.example.zygos.data.database

import androidx.room.*

/**
 * @see README.md
 *
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
    // some fields are needed so that queries can easily filter and sort by the Lot's account, etc.
    @PrimaryKey(autoGenerate = true) val lotId: Int = 0, // 0 to auto generate a key
    val openTransactionId: Int,
    val account: String, // these are duplicated from the transaction since we query on them a lot
    val ticker: String,
    val sharesOpen: Int = 0, // for options, also a multiple of 100
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
) {
    @Ignore val openTransaction = transactions.first { it.transactionId == lot.openTransactionId }
}

data class RealizedClosed(
    val ticker: String?,
    val realizedClosed: Int?
)


@Dao
interface LotDao {

    @Insert
    fun insert(lot: Lot): Long

    @Insert
    fun insert(lotTransactionCrossRef: LotTransactionCrossRef)

    @Update
    fun update(lot: Lot)


    // fully qualified name to not conflict with the transaction class. whole operation is performed atomically
    @androidx.room.Transaction
    @Query("SELECT * FROM lot")
    fun getAll(): List<LotWithTransactions>


    @androidx.room.Transaction
    @Query("SELECT * FROM lot " +
           "WHERE account = :account OR account = 'All' "
    )
    fun getAll(account: String): List<LotWithTransactions>

    @androidx.room.Transaction
    @Query("SELECT * FROM lot " +
            "WHERE (account = :account OR account = 'All') AND ticker == :ticker")
    fun getTicker(account: String, ticker: String): List<LotWithTransactions>

    @androidx.room.Transaction
    @Query("SELECT * FROM lot " +
            "WHERE (account = :account OR account = 'All') AND sharesOpen > 0 "
    )
    fun getOpen(account: String): List<LotWithTransactions>

    @androidx.room.Transaction
    @Query("SELECT * FROM lot " +
            "WHERE (account = :account OR account = 'All') AND ticker == :ticker AND sharesOpen > 0 "
    )
    fun getOpen(account: String, ticker: String): List<LotWithTransactions>


    @Query("SELECT COUNT(*) FROM lot " +
            "WHERE account = :account OR account = 'All'")
    fun count(account: String): Int

    @Query("SELECT ticker, SUM(realizedClosed) FROM lot " +
            "WHERE (account = :account OR account = 'All') " +
            "GROUP BY ticker"
    )
    fun realizedClosed(account: String): List<RealizedClosed>

    @Query("SELECT DISTINCT ticker FROM lot " +
            "WHERE (account = :account OR account = 'All')")
    fun tickers(account: String): List<String>

    @androidx.room.Transaction // does all the queries here at once
    fun getOpenAndRealized(account: String): MutableMap<String, Pair<Int, List<LotWithTransactions>>> {
        val realized = realizedClosed(account)
        val out = mutableMapOf<String, Pair<Int, List<LotWithTransactions>>>()
        realized.forEach {
            if (it.ticker != null)
                out[it.ticker] = Pair(it.realizedClosed ?: 0, getOpen(account, it.ticker))
        }
        return out
    }

//    @androidx.room.Transaction
//    @Query("SELECT SUM(realizedClosed) FROM lot " +
//            "INNER JOIN transaction_table ON transaction_table.transactionId = lot.openTransactionId " +
//            "WHERE transaction_table.account = :account OR account = 'All'")

}