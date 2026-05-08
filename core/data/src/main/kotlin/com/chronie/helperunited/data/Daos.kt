package com.chronie.helperunited.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Query("SELECT * FROM hoyo_accounts ORDER BY priority DESC, name ASC")
    fun observeAll(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM hoyo_accounts ORDER BY priority DESC, name ASC")
    suspend fun getAll(): List<AccountEntity>

    @Query("SELECT * FROM hoyo_accounts WHERE uuid = :uuid")
    suspend fun getById(uuid: String): AccountEntity?

    @Query("SELECT * FROM hoyo_accounts WHERE uid = :uid AND gameRawValue = :game LIMIT 1")
    suspend fun findByUidAndGame(uid: String, game: String): AccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(account: AccountEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(accounts: List<AccountEntity>)

    @Update
    suspend fun update(account: AccountEntity)

    @Delete
    suspend fun delete(account: AccountEntity)

    @Query("DELETE FROM hoyo_accounts WHERE uuid = :uuid")
    suspend fun deleteById(uuid: String)
}

@Dao
interface DailyNoteCacheDao {
    @Query("SELECT * FROM daily_note_cache WHERE accountUid = :accountUid AND gameRawValue = :game LIMIT 1")
    suspend fun get(accountUid: String, game: String): DailyNoteCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(cache: DailyNoteCacheEntity)

    @Query("DELETE FROM daily_note_cache WHERE accountUid = :accountUid")
    suspend fun deleteByAccount(accountUid: String)
}
