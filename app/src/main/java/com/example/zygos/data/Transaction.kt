package com.example.zygos.data

import android.content.Context
import android.util.Log
import androidx.annotation.NonNull
import androidx.room.*
import java.io.File

enum class TransactionType {
    TRANSFER, INTEREST, DIVIDEND, STOCK, CALL_LONG, CALL_SHORT, PUT_LONG, PUT_SHORT, BOND,
    SPLIT, SPINOFF, RENAME,
}


/**
 * All integer dollar values are 1/100th of a cent, and dates are in YYYYMMDD format
 */
@Entity(tableName = "transaction_table",  // transaction is a keyword!!!!!!!
    foreignKeys = [ForeignKey(
        entity = Transaction::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("openId")
    )]
)
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0, // 0 to auto generate a key

    /** Common info **/
    @NonNull val account: String, // can be "All" for some special events like split or rename
    @NonNull val ticker: String,
    @NonNull val note: String, // for TransactionType::RENAME, MUST be the new ticker name
    val type: TransactionType,
    val shares: Int, // should be multiple of 100 for options
    val date: Int,
    val price: Int, // price to track gain/loss, not the actual value of trade
    val value: Int, // actual dollar change due to the trade
    val fees: Int, // known fees associated with opening this position

    val openId: Int? = null, // on close transactions, id of the opening transaction

    /** Option fields **/
    val expiration: Int = 0,
    val strike: Int = 0,
    val priceUnderlying: Int = 0, // when position was opened. Can be 0 for old parthenos transactions, in which case price is only the extrinsic
)


@Dao
interface TransactionDao {
    @Insert
    fun addTransaction(transaction: Transaction)

    @Query("SELECT * FROM transaction_table ORDER BY date DESC")
    fun getAll(): List<Transaction>

    @Query("SELECT COUNT(*) FROM transaction_table")
    fun count(): Int
}


@Database(entities = [Transaction::class], version = 1)
abstract class TransactionDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var INSTANCE: TransactionDatabase? = null

        fun getDatabase(context: Context): TransactionDatabase {
            return INSTANCE ?: synchronized(this) {
                val prepopFile = File(context.filesDir, "parthenos.db") // TODO hardcoded. Just drag drop this into the Android Studio file explorer
                Log.d("Zygos/TransactionDatabase/getDatabase", prepopFile.absolutePath)
                var builder = Room.databaseBuilder(
                    context,
                    TransactionDatabase::class.java,
                    "app_database"
                )
                if (prepopFile.exists()) {
                    builder = builder.createFromFile(prepopFile)
                }
                val instance = builder.build()
                INSTANCE = instance

                instance
            }
        }
    }
}