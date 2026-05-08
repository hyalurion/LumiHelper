package com.chronie.helperunited.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        AccountEntity::class,
        DailyNoteCacheEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun dailyNoteCacheDao(): DailyNoteCacheDao

    companion object {
        const val DATABASE_NAME = "helper_united.db"
    }
}
