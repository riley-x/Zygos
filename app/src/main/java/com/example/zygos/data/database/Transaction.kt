package com.example.zygos.data.database

import android.util.Log
import androidx.annotation.NonNull
import androidx.room.*
import androidx.sqlite.db.SimpleSQLiteQuery
import com.example.zygos.ui.components.allAccounts
import java.util.*

enum class TransactionType(val isOption: Boolean = false) {
    TRANSFER, INTEREST, DIVIDEND, STOCK,
    CALL_LONG(true), CALL_SHORT(true), PUT_LONG(true), PUT_SHORT(true),
    BOND, SPLIT, SPINOFF, RENAME, NONE;
}


/**
 * All integer dollar values are 1/100th of a cent, and dates are in YYYYMMDD format
 */
@Entity(tableName = "transaction_table",  // transaction is a keyword!!!!!!!
)
data class Transaction(
    @PrimaryKey(autoGenerate = true) val transactionId: Int = 0, // 0 to auto generate a key

    /** Common info **/
    @NonNull val account: String = "", // can be "All" for some special events like split or rename
    @NonNull val ticker: String = "",
    @NonNull val note: String = "", // for TransactionType::RENAME, MUST be the new ticker name
    val type: TransactionType = TransactionType.NONE,
    val shares: Int = 0, // should be multiple of 100 for options
    val date: Int = 0,
    val price: Int = 0, // price to track gain/loss, not the actual value of trade
    val value: Int = 0, // actual dollar change due to the trade
    val fees: Int = 0, // known fees associated with opening this position

    /** Option fields **/
    val expiration: Int = 0,
    val strike: Int = 0,
    val priceUnderlying: Int = 0, // when position was opened. Can be 0 for old parthenos transactions, in which case price is only the extrinsic
)


@Dao
interface TransactionDao {
    // TODO this returns the row id not the primary key...is this a problem?
    @Insert
    fun insert(transaction: Transaction): Long

    @Update
    fun update(transaction: Transaction)

    @Query("SELECT * FROM transaction_table ORDER BY date DESC")
    fun getAll(): List<Transaction>

    @Query("SELECT * FROM transaction_table WHERE account = :account OR account = 'All' ORDER BY date DESC")
    fun getAccount(account: String): List<Transaction>

    @Query("SELECT * FROM transaction_table WHERE account = :account OR account = 'All' ORDER BY transactionId DESC LIMIT :n")
    fun getLast(account: String, n: Int = 5): List<Transaction>

    @Query("SELECT * FROM transaction_table ORDER BY transactionId DESC LIMIT :n")
    fun getLast(n: Int = 5): List<Transaction>

    @Query("SELECT COUNT(*) FROM transaction_table")
    fun count(): Int

    @RawQuery
    fun getRaw(q: SimpleSQLiteQuery): List<Transaction>
    fun get(
        account: String = "",
        ticker: String = "",
        type: TransactionType = TransactionType.NONE,
        sort: String = "",
        ascending: Boolean = true,
    ): List<Transaction> {
        return getRaw(getTransactionFilteredQuery(
            account = account,
            ticker = ticker,
            type = type,
            sort = sort,
            ascending = ascending
        ))
    }
}


fun getTransactionFilteredQuery(
    account: String = "",
    ticker: String = "",
    type: TransactionType = TransactionType.NONE,
    sort: String = "",
    ascending: Boolean = true,
): SimpleSQLiteQuery {
    val args = mutableListOf<Any>()
    var q = "SELECT * FROM transaction_table"
    if (account.isNotBlank() && account != allAccounts) {
        q += " WHERE (account = ? OR account = 'All')"
        args.add(account)
    }
    if (ticker.isNotBlank()) {
        q += if ("WHERE" in q) " AND" else " WHERE"
        q += " ticker = ?"
        args.add(ticker)
    }
    if (type != TransactionType.NONE) {
        q += if ("WHERE" in q) " AND" else " WHERE"
        q += " type = ?"
        args.add(type.name)
    }
    if (sort.isNotBlank()) {
        q += " ORDER BY $sort" // can't use args here since it'll wrap the string type in quotes, but this is injection safe since you can't input sort method text anyways
        q += if (ascending) " ASC" else " DESC"
    }
    Log.i("Zygos/Transaction", q)
    return SimpleSQLiteQuery(q, args.toTypedArray())
}

