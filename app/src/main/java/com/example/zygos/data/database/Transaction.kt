package com.example.zygos.data.database

import android.util.Log
import androidx.annotation.NonNull
import androidx.room.*
import androidx.sqlite.db.SimpleSQLiteQuery
import com.example.zygos.ui.components.allAccounts
import java.util.*

enum class TransactionType(val displayName: String, val isOption: Boolean = false, val isShort: Boolean = false) {
    TRANSFER("Transfer"),
    INTEREST("Interest"),
    DIVIDEND("Dividend"),
    STOCK("Stock"),
    CALL_LONG("Long Call", true),
    CALL_SHORT("Short Call", true, true),
    PUT_LONG("Long Put", true),
    PUT_SHORT("Short Put", true, true),
    BOND("Bond"),
    SPLIT("Split"),
    SPINOFF("Spin-off"),
    RENAME("Rename"),
    NONE("None");

    override fun toString(): String {
        return displayName
    }
}


/**
 * All integer dollar values are 1/100th of a cent, and dates are in YYYYMMDD format
 *
 * @param closeLot this transaction closes a specific lot. This should be the ordinal number of the
 * lot among all open lots of the account + ticker, increasing date sorted. Note this can't be the
 * database id of the lot because the lot table might be completely regenerated if a transaction is
 * deleted or updated. Set to -1 to indicate FIFO, in which case this transaction might close
 * multiple lots. This should not be set by the user, but auto-populated by clicking a "Close Lot"
 * button.
 *
 * @param priceUnderlying for options, price of the stock when the position was opened. Can be 0 for
 * old parthenos transactions, in which case price is only the extrinsic. TODO
 */
@Entity(tableName = "transaction_table",  // transaction is a keyword!!!!!!!
)
data class Transaction(
    @PrimaryKey(autoGenerate = true) val transactionId: Long = 0, // 0 to auto generate a key

    /** Common info **/
    @NonNull val account: String = "", // can be "All" for some special events like split or rename
    @NonNull val ticker: String = "",
    @NonNull val note: String = "", // for TransactionType::RENAME, MUST be the new ticker name
    val type: TransactionType = TransactionType.NONE,
    val shares: Long = 0, // should be multiple of 100 for options
    val date: Int = 0,
    val price: Long = 0, // price to track gain/loss, not the actual value of trade
    val value: Long = 0, // actual dollar change due to the trade
    val closeLot: Int = -1,

    /** Option fields **/
    val expiration: Int = 0,
    val strike: Long = 0,
    val priceUnderlying: Long = 0,
) {
    @Ignore val feesAndRounding = if (ticker == "CASH") 0 else (if (type.isShort) 1 else -1) * shares * price - value
}


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

    /** This function processes the supplied filters and submits a raw query **/
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

