package com.chronie.helperunited.data

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME,
        ).build()

    @Provides
    @Singleton
    fun provideAccountDao(database: AppDatabase): AccountDao =
        database.accountDao()

    @Provides
    @Singleton
    fun provideDailyNoteCacheDao(database: AppDatabase): DailyNoteCacheDao =
        database.dailyNoteCacheDao()
}
