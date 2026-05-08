package com.chronie.helperunited.data

import com.chronie.helperunited.model.GachaEntry
import com.chronie.helperunited.model.GachaProfile
import com.chronie.helperunited.model.UIGFExport
import com.chronie.helperunited.model.UIGFInfo
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for gacha records.
 * Handles CRUD operations and UIGF import/export.
 * Ported from PizzaHelperUnited's GachaKit.
 */
@Singleton
class GachaRepository @Inject constructor(
    private val gachaEntryDao: GachaEntryDao,
    private val gachaProfileDao: GachaProfileDao,
    private val moshi: Moshi,
) {
    /**
     * Get all gacha entries for a UID and game.
     */
    suspend fun getEntries(uid: String, game: String): List<GachaEntry> {
        return gachaEntryDao.getEntriesByUidAndGame(uid, game).map { it.toModel() }
    }

    /**
     * Observe gacha entries for a UID and game.
     */
    fun observeEntries(uid: String, game: String): Flow<List<GachaEntry>> {
        return gachaEntryDao.observeEntriesByUidAndGame(uid, game).map { entities ->
            entities.map { it.toModel() }
        }
    }

    /**
     * Get entries by gacha type.
     */
    suspend fun getEntriesByType(uid: String, game: String, gachaType: String): List<GachaEntry> {
        return gachaEntryDao.getEntriesByType(uid, game, gachaType).map { it.toModel() }
    }

    /**
     * Get five-star entries.
     */
    suspend fun getFiveStarEntries(uid: String, game: String): List<GachaEntry> {
        return gachaEntryDao.getFiveStarEntries(uid, game).map { it.toModel() }
    }

    /**
     * Get four-star entries.
     */
    suspend fun getFourStarEntries(uid: String, game: String): List<GachaEntry> {
        return gachaEntryDao.getFourStarEntries(uid, game).map { it.toModel() }
    }

    /**
     * Get total pull count.
     */
    suspend fun getTotalCount(uid: String, game: String): Int {
        return gachaEntryDao.getTotalCount(uid, game)
    }

    /**
     * Get pull count by gacha type.
     */
    suspend fun getCountByType(uid: String, game: String, gachaType: String): Int {
        return gachaEntryDao.getCountByType(uid, game, gachaType)
    }

    /**
     * Get pull count by gacha type and rarity.
     */
    suspend fun getCountByTypeAndRank(uid: String, game: String, gachaType: String, rankType: Int): Int {
        return gachaEntryDao.getCountByTypeAndRank(uid, game, gachaType, rankType)
    }

    /**
     * Get the latest entry for a gacha type.
     */
    suspend fun getLatestEntry(uid: String, game: String, gachaType: String): GachaEntry? {
        return gachaEntryDao.getLatestEntry(uid, game, gachaType)?.toModel()
    }

    /**
     * Insert a single gacha entry.
     */
    suspend fun insertEntry(entry: GachaEntry) = withContext(Dispatchers.IO) {
        gachaEntryDao.insertEntry(entry.toEntity())
        updateProfile(entry.uid, entry.game)
    }

    /**
     * Insert multiple gacha entries.
     */
    suspend fun insertEntries(entries: List<GachaEntry>) = withContext(Dispatchers.IO) {
        gachaEntryDao.insertEntries(entries.map { it.toEntity() })
        // Update profiles for all affected UIDs
        entries.groupBy { "${it.uid}_${it.game}" }.forEach { (_, group) ->
            val first = group.first()
            updateProfile(first.uid, first.game)
        }
    }

    /**
     * Delete all entries for a UID and game.
     */
    suspend fun deleteAll(uid: String, game: String) = withContext(Dispatchers.IO) {
        gachaEntryDao.deleteAllByUidAndGame(uid, game)
        gachaProfileDao.deleteProfile(uid, game)
    }

    /**
     * Get all gacha profiles.
     */
    suspend fun getAllProfiles(): List<GachaProfile> {
        return gachaProfileDao.getAllProfiles().map { it.toModel() }
    }

    /**
     * Get UIDs with records for a game.
     */
    suspend fun getUidsWithRecords(game: String): List<String> {
        return gachaEntryDao.getUidsWithRecords(game)
    }

    /**
     * Export gacha records in UIGF format.
     */
    suspend fun exportUIGF(uid: String, game: String): UIGFExport = withContext(Dispatchers.IO) {
        val entries = gachaEntryDao.getEntriesByUidAndGame(uid, game).map { it.toModel() }
        val now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        UIGFExport(
            info = UIGFInfo(
                uid = uid,
                lang = "zh-cn",
                exportTime = now,
            ),
            list = entries,
        )
    }

    /**
     * Export gacha records as JSON string.
     */
    suspend fun exportUIGFJson(uid: String, game: String): String {
        val export = exportUIGF(uid, game)
        return moshi.adapter(UIGFExport::class.java).indent("  ").toJson(export)
    }

    /**
     * Import gacha records from UIGF JSON string.
     */
    suspend fun importUIGF(json: String): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val adapter = moshi.adapter(UIGFExport::class.java)
            val export = adapter.fromJson(json)
                ?: return@withContext Result.failure(Exception("Invalid UIGF format"))

            val entries = export.list
            insertEntries(entries)
            Result.success(entries.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update profile last update time.
     */
    private suspend fun updateProfile(uid: String, game: String) {
        val now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        val existing = gachaProfileDao.getProfile(uid, game)
        if (existing != null) {
            gachaProfileDao.updateLastUpdateTime(uid, game, now)
        } else {
            gachaProfileDao.insertProfile(
                GachaProfileEntity(uid = uid, game = game, lastUpdateTime = now),
            )
        }
    }
}

// Extension functions for entity <-> model conversion

private fun GachaEntryEntity.toModel() = GachaEntry(
    id = id,
    uid = uid,
    game = game,
    gachaType = gachaType,
    itemId = itemId,
    count = count,
    time = time,
    name = name,
    lang = lang,
    itemType = itemType,
    rankType = rankType,
    gachaId = gachaId,
)

private fun GachaEntry.toEntity() = GachaEntryEntity(
    id = id,
    uid = uid,
    game = game,
    gachaType = gachaType,
    itemId = itemId,
    count = count,
    time = time,
    name = name,
    lang = lang,
    itemType = itemType,
    rankType = rankType,
    gachaId = gachaId,
)

private fun GachaProfileEntity.toModel() = GachaProfile(
    uid = uid,
    game = game,
    lastUpdateTime = lastUpdateTime,
)
