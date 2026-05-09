package com.chronie.helperunited.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        AccountEntity::class,
        DailyNoteCacheEntity::class,
        GachaEntryEntity::class,
        GachaProfileEntity::class,
        HoYoLabCharacterEntity::class,
    ],
    version = 3,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun dailyNoteCacheDao(): DailyNoteCacheDao
    abstract fun gachaEntryDao(): GachaEntryDao
    abstract fun gachaProfileDao(): GachaProfileDao
    abstract fun hoyolabCharacterDao(): HoYoLabCharacterDao

    companion object {
        const val DATABASE_NAME = "helper_united.db"
    }
}
