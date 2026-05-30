package com.hyalurion.lumi.helper.data

import android.content.Context
import androidx.room.Room
import com.hyalurion.lumi.helper.network.EnkaNetworkApi
import com.hyalurion.lumi.helper.network.HoYoApiFactory
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
    fun provideHoYoLabCharacterDao(database: AppDatabase): HoYoLabCharacterDao =
        database.hoyolabCharacterDao()

    @Provides
    @Singleton
    fun provideAccountRepository(
        accountDao: AccountDao,
    ): AccountRepository = AccountRepository(accountDao)

    @Provides
    @Singleton
    fun provideDailyNoteRepository(
        apiFactory: HoYoApiFactory,
        dailyNoteCacheDao: DailyNoteCacheDao,
        moshi: Moshi,
    ): DailyNoteRepository = DailyNoteRepository(apiFactory, dailyNoteCacheDao, moshi)

    @Provides
    @Singleton
    fun provideEnkaRepository(
        @ApplicationContext context: Context,
        enkaApi: EnkaNetworkApi,
        moshi: Moshi,
        enkaDbManager: EnkaDatabaseManager,
    ): EnkaRepository = EnkaRepository(context, enkaApi, moshi, enkaDbManager)

    @Provides
    @Singleton
    fun provideGachaRepository(
        gachaEntryDao: GachaEntryDao,
        gachaProfileDao: GachaProfileDao,
        moshi: Moshi,
    ): GachaRepository = GachaRepository(gachaEntryDao, gachaProfileDao, moshi)

    @Provides
    @Singleton
    fun provideHoYoLabCharacterRepository(
        apiFactory: HoYoApiFactory,
        characterDao: HoYoLabCharacterDao,
    ): HoYoLabCharacterRepository = HoYoLabCharacterRepository(apiFactory, characterDao)

    @Provides
    @Singleton
    fun provideZZZShowcaseRepository(
        apiFactory: HoYoApiFactory,
    ): ZZZShowcaseRepository = ZZZShowcaseRepository(apiFactory)

    @Provides
    @Singleton
    fun provideChallengeRepository(
        apiFactory: HoYoApiFactory,
    ): ChallengeRepository = ChallengeRepository(apiFactory)

    @Provides
    @Singleton
    fun provideZZZGachaFetchRepository(
        apiFactory: HoYoApiFactory,
        gachaRepository: GachaRepository,
    ): ZZZGachaFetchRepository = ZZZGachaFetchRepository(apiFactory, gachaRepository)

    @Provides
    @Singleton
    fun provideEnkaDatabaseManager(
        @ApplicationContext context: Context,
        moshi: Moshi,
    ): EnkaDatabaseManager = EnkaDatabaseManager(context, moshi)

    @Provides
    @Singleton
    fun provideHoYoLabSignRepository(
        apiFactory: HoYoApiFactory,
    ): HoYoLabSignRepository = HoYoLabSignRepository(apiFactory)

    @Provides
    @Singleton
    fun provideUIGFManager(
        @ApplicationContext context: Context,
        moshi: Moshi,
        gachaRepository: GachaRepository,
    ): UIGFManager = UIGFManager(context, moshi, gachaRepository)
}
