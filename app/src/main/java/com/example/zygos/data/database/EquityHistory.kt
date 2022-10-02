package com.example.zygos.data.database

import androidx.annotation.NonNull
import androidx.room.*


/**
 * This stores the returns at end of day of each trading day. It does not include starting capital.
 * Values are 1/100th of a cent, and dates are in YYYYMMDD format.
 */
@Entity(tableName = "equity_history",
    primaryKeys = ["account", "date"],
)
data class EquityHistory(
    @NonNull val account: String, // can be "All Accounts"
    val date: Int,
    val returns: Int,
)


@Dao
interface EquityHistoryDao {
    @Insert
    fun addEntry(equityHistory: EquityHistory)

    @Query("SELECT * FROM equity_history ORDER BY date DESC")
    fun getAll(): List<EquityHistory>

    @Query("SELECT * FROM equity_history WHERE account = :account ORDER BY date ASC")
    fun getAccount(account: String): List<EquityHistory>

    @Query("SELECT COUNT(*) FROM equity_history")
    fun count(): Int

    @Query("SELECT COUNT(*) FROM equity_history WHERE account = :account")
    fun count(account: String): Int
}