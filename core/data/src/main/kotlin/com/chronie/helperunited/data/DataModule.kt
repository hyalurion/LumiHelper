package com.chronie.helperunited.data

import android.content.Context
import androidx.room.Room
import com.chronie.helperunited.network.EnkaNetworkApi
import com.chronie.helperunited.network.HoYoApiFactory
import com.squareup.moshi.Moshi
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
        )
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun provideAccountDao(database: AppDatabase): AccountDao =
        database.accountDao()

    @Provides
    @Singleton
    fun provideDailyNoteCacheDao(database: AppDatabase): DailyNoteCacheDao =
        database.dailyNoteCacheDao()

    @Provides
    @Singleton
    fun provideGachaEntryDao(database: AppDatabase): GachaEntryDao =
        database.gachaEntryDao()

    @Provides
    @Singleton
    fun provideGachaProfileDao(database: AppDatabase): GachaProfileDao =
        database.gachaProfileDao()

    @Provides
    @Singleton
    fun provideAccountRepository(
        accountDao: AccountDao,
        apiFactory: HoYoApiFactory,
    ): AccountRepository = AccountRepository(accountDao, apiFactory)

    @Provides
    @Singleton
    fun provideDailyNoteRepository(
        accountDao: AccountDao,
        apiFactory: HoYoApiFactory,
    ): DailyNoteRepository = DailyNoteRepository(accountDao, apiFactory)

    @Provides
    @Singleton
    fun provideEnkaNetworkApi(
        apiFactory: HoYoApiFactory,
    ): EnkaNetworkApi = apiFactory.createEnkaNetworkApi()

    @Provides
    @Singleton
    fun provideEnkaRepository(
        @ApplicationContext context: Context,
        enkaApi: EnkaNetworkApi,
        moshi: Moshi,
    ): EnkaRepository = EnkaRepository(context, enkaApi, moshi)

    @Provides
    @Singleton
    fun provideGachaRepository(
        gachaEntryDao: GachaEntryDao,
        gachaProfileDao: GachaProfileDao,
        moshi: Moshi,
    ): GachaRepository = GachaRepository(gachaEntryDao, gachaProfileDao, moshi)
}
