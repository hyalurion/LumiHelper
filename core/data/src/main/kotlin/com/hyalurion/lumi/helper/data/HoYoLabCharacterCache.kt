package com.hyalurion.lumi.helper.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Room entity for caching HoYoLAB character list data.
 * Each row represents one character from the HoYoLAB game_record API.
 */
@Entity(
    tableName = "hoyolab_characters",
    primaryKeys = ["accountUid", "gameRawValue", "characterId"],
)
data class HoYoLabCharacterEntity(
    val accountUid: String,
    val gameRawValue: String,
    val characterId: Int,
    val name: String,
    val element: String,
    val level: Int,
    val rarity: Int,
    val constellation: Int,
    val friendship: Int,
    val iconUrl: String?,
    val weaponName: String?,
    val weaponLevel: Int?,
    val weaponRarity: Int?,
    val weaponRefinement: Int?,
    val fetchedAt: Long,
)

/**
 * DAO for HoYoLAB character cache operations.
 */
@Dao
interface HoYoLabCharacterDao {

    @Query("SELECT * FROM hoyolab_characters WHERE accountUid = :accountUid AND gameRawValue = :gameRawValue ORDER BY level DESC, rarity DESC")
    fun observeByAccount(accountUid: String, gameRawValue: String): Flow<List<HoYoLabCharacterEntity>>

    @Query("SELECT * FROM hoyolab_characters WHERE accountUid = :accountUid AND gameRawValue = :gameRawValue ORDER BY level DESC, rarity DESC")
    suspend fun getByAccount(accountUid: String, gameRawValue: String): List<HoYoLabCharacterEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(characters: List<HoYoLabCharacterEntity>)

    @Query("DELETE FROM hoyolab_characters WHERE accountUid = :accountUid AND gameRawValue = :gameRawValue")
    suspend fun deleteByAccount(accountUid: String, gameRawValue: String)

    @Query("DELETE FROM hoyolab_characters WHERE accountUid = :accountUid")
    suspend fun deleteByAccountAllGames(accountUid: String)

    @Query("SELECT DISTINCT element FROM hoyolab_characters WHERE accountUid = :accountUid AND gameRawValue = :gameRawValue")
    suspend fun getDistinctElements(accountUid: String, gameRawValue: String): List<String>

    @Query("SELECT DISTINCT rarity FROM hoyolab_characters WHERE accountUid = :accountUid AND gameRawValue = :gameRawValue ORDER BY rarity DESC")
    suspend fun getDistinctRarities(accountUid: String, gameRawValue: String): List<Int>
}
