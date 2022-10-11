package com.example.zygos.data.database

import androidx.annotation.NonNull
import androidx.room.*

const val colors_table = "colors"

@Entity(tableName = colors_table)
data class ColorSettings(
    @PrimaryKey @NonNull val key: String,
    val color: Int,
)


@Dao
interface ColorDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun add(colorSettings: ColorSettings)

    @Update
    fun update(colorSettings: ColorSettings)

    @Query("SELECT COUNT(*) FROM $colors_table")
    fun count(): Int

    @MapInfo(keyColumn = "key", valueColumn = "color")
    @Query("SELECT * FROM $colors_table")
    fun getMap(): Map<String, Int>
}