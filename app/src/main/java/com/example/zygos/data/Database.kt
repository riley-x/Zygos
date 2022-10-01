package com.example.zygos.data

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import java.io.File

@Database(entities = [Transaction::class, EquityHistory::class], version = 1)
abstract class ZygosDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun equityHistoryDao(): EquityHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: ZygosDatabase? = null

        fun getDatabase(context: Context): ZygosDatabase {
            return INSTANCE ?: synchronized(this) {
                val prepopFile = File(context.filesDir, "parthenos.db") // TODO hardcoded. Just drag drop this into the Android Studio file explorer
                Log.d("Zygos/TransactionDatabase/getDatabase", prepopFile.absolutePath)
                var builder = Room.databaseBuilder(
                    context,
                    ZygosDatabase::class.java,
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