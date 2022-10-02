package com.example.zygos.data

import androidx.annotation.NonNull
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.Query

/**
 * This stores the returns at end of day of each trading day. It does not include starting capital.
 * Values are 1/100th of a cent, and dates are in YYYYMMDD format.
 */
@Entity(tableName = "ohlc",
    primaryKeys = ["ticker", "date"],
)
data class Ohlc(
    @NonNull val ticker: String, // can be "All Accounts"
    val date: Int,
    val returns: Int,
)


@Dao
interface OhlcDao {
    @Insert
    fun addEntry(ohlc: Ohlc)

    @Query("SELECT * FROM ohlc")
    fun getAll(): List<Ohlc>

    @Query("SELECT * FROM ohlc WHERE ticker = :ticker ORDER BY date ASC")
    fun getTicker(ticker: String): List<Ohlc>

    @Query("SELECT COUNT(*) FROM ohlc")
    fun count(): Int

    @Query("SELECT COUNT(*) FROM ohlc WHERE ticker = :ticker")
    fun count(ticker: String): Int
}