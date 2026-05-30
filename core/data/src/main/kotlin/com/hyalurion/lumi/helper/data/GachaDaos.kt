package com.hyalurion.lumi.helper.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * DAO for gacha entries.
 */
@Dao
interface GachaEntryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: GachaEntryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntries(entries: List<GachaEntryEntity>)

    @Query("SELECT * FROM gacha_entries WHERE uid = :uid AND game = :game ORDER BY time DESC")
    suspend fun getEntriesByUidAndGame(uid: String, game: String): List<GachaEntryEntity>

    @Query("SELECT * FROM gacha_entries WHERE uid = :uid AND game = :game AND gachaType = :gachaType ORDER BY time DESC")
    suspend fun getEntriesByType(uid: String, game: String, gachaType: String): List<GachaEntryEntity>

    @Query("SELECT * FROM gacha_entries WHERE uid = :uid AND game = :game ORDER BY time DESC")
    fun observeEntriesByUidAndGame(uid: String, game: String): Flow<List<GachaEntryEntity>>

    @Query("SELECT * FROM gacha_entries WHERE uid = :uid AND game = :game AND rankType = 5 ORDER BY time DESC")
    suspend fun getFiveStarEntries(uid: String, game: String): List<GachaEntryEntity>

    @Query("SELECT * FROM gacha_entries WHERE uid = :uid AND game = :game AND rankType = 4 ORDER BY time DESC")
    suspend fun getFourStarEntries(uid: String, game: String): List<GachaEntryEntity>

    @Query("SELECT COUNT(*) FROM gacha_entries WHERE uid = :uid AND game = :game")
    suspend fun getTotalCount(uid: String, game: String): Int

    @Query("SELECT COUNT(*) FROM gacha_entries WHERE uid = :uid AND game = :game AND gachaType = :gachaType")
    suspend fun getCountByType(uid: String, game: String, gachaType: String): Int

    @Query("SELECT COUNT(*) FROM gacha_entries WHERE uid = :uid AND game = :game AND gachaType = :gachaType AND rankType = :rankType")
    suspend fun getCountByTypeAndRank(uid: String, game: String, gachaType: String, rankType: Int): Int

    @Query("SELECT * FROM gacha_entries WHERE uid = :uid AND game = :game AND gachaType = :gachaType ORDER BY time DESC LIMIT 1")
    suspend fun getLatestEntry(uid: String, game: String, gachaType: String): GachaEntryEntity?

    @Query("DELETE FROM gacha_entries WHERE uid = :uid AND game = :game")
    suspend fun deleteAllByUidAndGame(uid: String, game: String)

    @Query("DELETE FROM gacha_entries WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT DISTINCT uid FROM gacha_entries WHERE game = :game")
    suspend fun getUidsWithRecords(game: String): List<String>

    @Query("SELECT * FROM gacha_entries WHERE uid = :uid AND game = :game AND time >= :startTime AND time <= :endTime ORDER BY time DESC")
    suspend fun getEntriesByTimeRange(uid: String, game: String, startTime: String, endTime: String): List<GachaEntryEntity>
}

/**
 * DAO for gacha profiles.
 */
@Dao
interface GachaProfileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: GachaProfileEntity)

    @Query("SELECT * FROM gacha_profiles ORDER BY uid")
    suspend fun getAllProfiles(): List<GachaProfileEntity>

    @Query("SELECT * FROM gacha_profiles WHERE uid = :uid AND game = :game")
    suspend fun getProfile(uid: String, game: String): GachaProfileEntity?

    @Query("DELETE FROM gacha_profiles WHERE uid = :uid AND game = :game")
    suspend fun deleteProfile(uid: String, game: String)

    @Query("UPDATE gacha_profiles SET lastUpdateTime = :time WHERE uid = :uid AND game = :game")
    suspend fun updateLastUpdateTime(uid: String, game: String, time: String)
}
