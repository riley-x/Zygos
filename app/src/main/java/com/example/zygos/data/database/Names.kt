package com.example.zygos.data.database

import androidx.annotation.NonNull
import androidx.room.*


@Entity(tableName = "names",
    primaryKeys = ["type", "name"],
)
data class Names(
    @NonNull val type: String,
    @NonNull val name: String,
)

@Dao
interface NamesDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun add(name: Names)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun add(names: Collection<Names>)

    @Query("SELECT * FROM names WHERE type = 'watchlist' ORDER BY name ASC")
    fun getWatchlist(): List<Names>
}