package com.example.zygos.data

import android.content.Context
import androidx.annotation.NonNull
import androidx.room.*


enum class LotType {
    STOCK, CALL_LONG, CALL_SHORT, PUT_LONG, PUT_SHORT, BOND
}


/**
 * All integer dollar values are 1/100th of a cent, and dates are in YYYYMMDD format
 */
@Entity(
    tableName = "lot",
    foreignKeys = arrayOf(
        ForeignKey(
            entity = Transaction::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("transaction"),
            onDelete = ForeignKey.CASCADE
        )
    )
)
data class Lot(
    @PrimaryKey(autoGenerate = true) val id: Int = 0, // 0 to auto generate a key
    @ColumnInfo(index = true) val transaction: Int,

    /** Common info **/
    @NonNull val account: String,
    @NonNull val ticker: String,
    @NonNull val note: String,
    val type: LotType,
    val shares: Int, // should be multiple of 100 for options
    val date: Int,
    val price: Int, // price to track gain/loss, not the actual value of trade
    val basis: Int, // usually the value of the trade net fees, but can be adjusted from washes
    val fees: Int, // fees associated with opening this position
    val open: Int,

    /** Stock fields **/
    val dividends: Int = 0, // per share

    /** Option fields **/
    val expiration: Int = 0,
    val strike: Int = 0,
    val priceUnderlying: Int = 0, // when position was opened
)


@Dao
interface LotDao {
    @Insert
    fun addLot(lot: Lot)

    @Query("SELECT * FROM lot ORDER BY date DESC")
    fun getAll(): List<Lot>

    @Query("SELECT COUNT(*) FROM lot")
    fun count(): Int
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
//                    .createFromAsset("database/main.db")
                    .build()
                INSTANCE = instance

                instance
            }
        }
    }
}