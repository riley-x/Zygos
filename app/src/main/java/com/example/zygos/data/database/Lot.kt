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

    @Query("DELETE FROM lot")
    fun clearLots()

    @Query("DELETE FROM lotTransactionCrossRef")
    fun clearRefs()

    @androidx.room.Transaction
    fun clear() {
        clearRefs()
        clearLots()
    }

    @Query("SELECT COUNT(*) FROM lot")
    fun count(): Int

    @Query("SELECT COUNT(*) FROM lot " +
            "WHERE account = :account OR account = 'All'")
    fun count(account: String): Int

    @Query("SELECT DISTINCT ticker FROM lot")
    fun tickers(): List<String>

    @Query("SELECT DISTINCT ticker FROM lot " +
            "WHERE (account = :account OR account = 'All')")
    fun tickers(account: String): List<String>


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


    @MapInfo(keyColumn = "ticker", valueColumn = "sum")
    @Query("SELECT ticker, SUM(realizedClosed - feesAndRounding) AS sum FROM lot " +
            "WHERE lot.sharesOpen = 0 " +
            "GROUP BY ticker"
    )
    fun realizedClosed(): Map<String, Long>

    @MapInfo(keyColumn = "ticker", valueColumn = "sum")
    @Query("SELECT ticker, SUM(realizedClosed - feesAndRounding) as sum FROM lot " +
            "WHERE lot.sharesOpen = 0 and (account = :account OR account = 'All') " +
            "GROUP BY ticker"
    )
    fun realizedClosed(account: String): Map<String, Long>

    @androidx.room.Transaction // does all the queries here at once
    fun getOpenAndRealized(account: String): MutableMap<String, Pair<Long, List<LotWithTransactions>>> {
        val tickers = if (account == allAccounts) tickers() else tickers(account)
        val realizedClosed = if (account == allAccounts) realizedClosed() else realizedClosed(account)
        Log.d("Zygos/getOpenAndRealized", "Found ${tickers.size} tickers")

        val out = mutableMapOf<String, Pair<Long, List<LotWithTransactions>>>()
        tickers.forEach {
            Log.d("Zygos/getOpenAndRealized", "$it ${realizedClosed[it]}")
            if (it == "CASH") {
                val lots = if (account == allAccounts) getCash() else getCash(account)
                out[it] = Pair(realizedClosed[it] ?: 0, lots)
            } else {
                val lots = if (account == allAccounts) getOpenTicker(it) else getOpen(account, it)
                out[it] = Pair(realizedClosed[it] ?: 0, lots)
            }
        }
        return out
    }


}