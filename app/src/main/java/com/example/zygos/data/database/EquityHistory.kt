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
    @NonNull val account: String,
    val date: Int,
    val returns: Long,
)



@Dao
interface EquityHistoryDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun add(equityHistory: EquityHistory)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun add(equityHistory: List<EquityHistory>)

    @Query("SELECT * FROM equity_history ORDER BY date DESC")
    fun getAll(): List<EquityHistory>

    @Query("SELECT * FROM equity_history WHERE account = :account ORDER BY date ASC")
    fun getAccount(account: String): List<EquityHistory>

    // Still need to select account here or else Room complains, way around?
    @Query("SELECT account, date, SUM(returns) as returns FROM equity_history GROUP BY date")
    fun getAllAccounts(): List<EquityHistory>


    @MapInfo(keyColumn = "account", valueColumn = "date")
    @Query("SELECT account, MAX(date) as date FROM equity_history GROUP BY account")
    fun getLastEntries(): Map<String, Int>


    @Query("SELECT COUNT(*) FROM equity_history")
    fun count(): Int

    @Query("SELECT COUNT(*) FROM equity_history WHERE account = :account")
    fun count(account: String): Int
}
