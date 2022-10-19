package com.example.zygos.data.database

import android.content.Context
import android.util.Log
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import java.io.File


@Database(
    entities = [
        Transaction::class,
        EquityHistory::class,
        Lot::class,
        LotTransactionCrossRef::class,
        Ohlc::class,
        ColorSettings::class,
        Names::class,
    ],
    version = 8,
    autoMigrations = [
        AutoMigration (from = 7, to = 8)
    ]
)
abstract class ZygosDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun lotDao(): LotDao
    abstract fun equityHistoryDao(): EquityHistoryDao
    abstract fun ohlcDao(): OhlcDao
    abstract fun colorDao(): ColorDao
    abstract fun namesDao(): NamesDao

    companion object {
        @Volatile
        private var INSTANCE: ZygosDatabase? = null

        fun getDatabase(context: Context): ZygosDatabase {
            return INSTANCE ?: synchronized(this) {
                val prepopFile = File(
                    context.filesDir,
                    "parthenos.db"
                ) // TODO hardcoded. Just drag drop this into the Android Studio file explorer
                Log.d("Zygos/ZygosDatabase/getDatabase", prepopFile.absolutePath)
                var builder = Room.databaseBuilder(
                    context,
                    ZygosDatabase::class.java,
                    "app_database"
                )
                if (prepopFile.exists()) {
                    builder = builder.createFromFile(prepopFile)
                }
                val instance = builder
                    .fallbackToDestructiveMigration() // this will delete the old database! But the prepop file has to be up-to-date with the schema
                    .build()
                INSTANCE = instance

                instance
            }
        }
    }
}

