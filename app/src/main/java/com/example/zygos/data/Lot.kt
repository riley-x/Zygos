package com.example.zygos.data

import android.content.Context
import androidx.annotation.NonNull
import androidx.room.*


enum class LotType {
    STOCK, CALL_LONG, CALL_SHORT, PUT_LONG, PUT_SHORT, BOND
}


/**
 * All integer dollar values are 1/100th of a cent
 */
@Entity(tableName = "lot")
data class Lot(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @NonNull val account: String,
    @NonNull val ticker: String,
    @NonNull val note: String,
    val type: LotType,
    val shares: Int, // should be multiple of 100 for options
    val date: Int,
    val expiration: Int,
    val price: Int,
    val extrinsic: Int, // for ITM options, just the time value of the option
    val strike: Int,
    val dividends: Int, // per share
    val fees: Int, // value + price * n
)


@Dao
interface LotDao {
    @Query("SELECT * FROM lot ORDER BY date DESC")
    fun getAll(): List<Lot>
}


@Database(entities = [Lot::class], version = 1)
abstract class LotDatabase : RoomDatabase() {
    abstract fun lotDao(): LotDao

    companion object {
        @Volatile
        private var INSTANCE: LotDatabase? = null

        fun getDatabase(context: Context): LotDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context,
                    LotDatabase::class.java,
                    "app_database"
                )
                    .createFromAsset("database/main.db")
                    .build()
                INSTANCE = instance

                instance
            }
        }
    }
}