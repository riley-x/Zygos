package com.example.zygos.data

import android.content.Context
import android.util.Log
import androidx.annotation.NonNull
import androidx.room.*
import java.io.File


/**
 * This stores the returns at end of day of each trading day. It does not include starting capital.
 * Values are 1/100th of a cent, and dates are in YYYYMMDD format.
 */
@Entity(tableName = "equity_history")
data class EquityHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0, // 0 to auto generate a key

    /** Common info **/
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

    @Query("SELECT * FROM equity_history WHERE account = :account ORDER BY date DESC")
    fun getAccount(account: String): List<EquityHistory>

    @Query("SELECT COUNT(*) FROM equity_history")
    fun count(): Int

    @Query("SELECT COUNT(*) FROM equity_history WHERE account = :account")
    fun count(account: String): Int
}
