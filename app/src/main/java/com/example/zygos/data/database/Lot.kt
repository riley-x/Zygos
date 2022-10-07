package com.example.zygos.data.database

import android.util.Log
import androidx.room.*
import com.example.zygos.ui.components.allAccounts

/**
 * @see README.md
 *
 * Values are 1/100th of a cent, and dates are in YYYYMMDD format.
 */
@Entity(tableName = "lot")
data class Lot(
    @PrimaryKey(autoGenerate = true) val lotId: Long = 0, // 0 to auto generate a key
    val account: String, // these are duplicated from the transaction since we query on them a lot
    val ticker: String,
    val sharesOpen: Long, // for options, also a multiple of 100
    val feesAndRounding: Long, // -value = feesAndRounding + price * sharesAll. Fees are positive here, and is not altered if sharesOpen changes.
    val dividendsPerShare: Long, // any rounding errors should be placed into feesAndRounding above
    val realizedClosed: Long, // sum of realized returns of all closed shares
)


@Entity(primaryKeys = ["transactionId", "lotId"])
data class LotTransactionCrossRef(
    val transactionId: Long,
    val lotId: Long
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
    @Ignore val openTransaction =
        if (lot.ticker == "CASH") transactions[0]
        else transactions.first { it.shares > 0 }
}

data class RealizedClosed(
    val ticker: String?,
    val realizedClosed: Long?
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
            "WHERE ticker = 'CASH'"
    )
    fun getCash(): List<LotWithTransactions>

    @androidx.room.Transaction
    @Query("SELECT * FROM lot " +
            "WHERE (account = :account OR account = 'All') AND ticker = 'CASH'"
    )
    fun getCash(account: String): List<LotWithTransactions>

    @androidx.room.Transaction
    @Query("SELECT * FROM lot " +
            "WHERE ticker = :ticker AND sharesOpen > 0 "
    )
    fun getOpenTicker(ticker: String): List<LotWithTransactions>

    @androidx.room.Transaction
    @Query("SELECT * FROM lot " +
            "WHERE (account = :account OR account = 'All') AND (sharesOpen > 0 OR ticker == 'CASH')"
    )
    fun getOpenAccount(account: String): List<LotWithTransactions>

    @androidx.room.Transaction
    @Query("SELECT * FROM lot " +
            "WHERE (account = :account OR account = 'All') AND ticker = :ticker AND sharesOpen > 0 "
    )
    fun getOpen(account: String, ticker: String): List<LotWithTransactions>

    @Query("SELECT COUNT(*) FROM lot")
    fun count(): Int

    @Query("SELECT COUNT(*) FROM lot " +
            "WHERE account = :account OR account = 'All'")
    fun count(account: String): Int

    @Query("SELECT ticker, SUM(realizedClosed) FROM lot " +
            "GROUP BY ticker"
    )
    fun realizedClosed(): List<RealizedClosed>

    @Query("SELECT ticker, SUM(realizedClosed) FROM lot " +
            "WHERE (account = :account OR account = 'All') " +
            "GROUP BY ticker"
    )
    fun realizedClosed(account: String): List<RealizedClosed>

    @Query("SELECT DISTINCT ticker FROM lot " +
            "WHERE (account = :account OR account = 'All')")
    fun tickers(account: String): List<String>

    @androidx.room.Transaction // does all the queries here at once
    fun getOpenAndRealized(account: String): MutableMap<String, Pair<Long, List<LotWithTransactions>>> {
        val realized = if (account == allAccounts) realizedClosed() else realizedClosed(account)
        Log.i("Zygos/getOpenAndRealized", "Found ${realized.size} tickers")

        val out = mutableMapOf<String, Pair<Long, List<LotWithTransactions>>>()
        realized.forEach {
            if (it.ticker == "CASH") {
                val lots = if (account == allAccounts) getCash() else getCash(account)
                out[it.ticker] = Pair(it.realizedClosed ?: 0, lots)
            } else if (it.ticker != null) {
                val lots = if (account == allAccounts) getOpenTicker(it.ticker) else getOpen(account, it.ticker)
                out[it.ticker] = Pair(it.realizedClosed ?: 0, lots)
            }
        }
        return out
    }

//    @androidx.room.Transaction
//    @Query("SELECT SUM(realizedClosed) FROM lot " +
//            "INNER JOIN transaction_table ON transaction_table.transactionId = lot.openTransactionId " +
//            "WHERE transaction_table.account = :account OR account = 'All'")

}