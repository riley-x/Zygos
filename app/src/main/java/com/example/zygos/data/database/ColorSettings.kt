package com.example.zygos.data.database

import androidx.annotation.NonNull
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.room.*
import androidx.room.Transaction

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

    @Transaction
    fun add(colors: Map<String, Color>) {
        colors.forEach {
            add(ColorSettings(it.key, it.value.toArgb()))
        }
    }

    @Update
    fun update(colorSettings: ColorSettings)

    @Query("SELECT COUNT(*) FROM $colors_table")
    fun count(): Int

    @MapInfo(keyColumn = "key", valueColumn = "color")
    @Query("SELECT * FROM $colors_table")
    fun getMap(): Map<String, Int>
}