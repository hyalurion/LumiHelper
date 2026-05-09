package com.chronie.helperunited.data

import android.content.Context
import com.chronie.helperunited.model.EnkaArtifactMeta
import com.chronie.helperunited.model.EnkaArtifactSummarized
import com.chronie.helperunited.model.EnkaAvatarSummarized
import com.chronie.helperunited.model.EnkaCharacterMeta
import com.chronie.helperunited.model.EnkaProfileSummarized
import com.chronie.helperunited.model.EnkaResponse
import com.chronie.helperunited.model.EnkaStatEntry
import com.chronie.helperunited.model.EnkaWeaponMeta
import com.chronie.helperunited.model.EnkaWeaponSummarized
import com.chronie.helperunited.network.EnkaAssetUrls
import com.chronie.helperunited.network.EnkaNetworkApi
import com.squareup.moshi.Moshi
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Singleton

/**
 * Repository for Enka.Network data.
 * Handles fetching, caching, and processing of character showcase data.
 * Ported from PizzaHelperUnited's EnkaKit.
 */
@Singleton
class EnkaRepository(
    @ApplicationContext private val context: Context,
    private val enkaApi: EnkaNetworkApi,
    private val moshi: Moshi,
) {
    private val cacheDir: File
        get() = File(context.filesDir, "enka_cache").also { it.mkdirs() }

    /**
     * Fetch and process character showcase data for a given UID.
     * @param uid The game UID to query
     * @return Processed profile data ready for UI display
     */
    suspend fun fetchShowcase(uid: String): Result<EnkaProfileSummarized> = withContext(Dispatchers.IO) {
        try {
            val response = enkaApi.getShowcase(uid)
            val profile = processResponse(response)
            // Cache the response
            cacheResponse(uid, response)
            Result.success(profile)
        } catch (e: Exception) {
            // Try to load from cache if network fails
            val cached = loadCachedResponse(uid)
            if (cached != null) {
                Result.success(processResponse(cached))
            } else {
                Result.failure(e)
            }
        }
    }

    /**
     * Process Enka response into summarized profile data.
     */
    private fun processResponse(response: EnkaResponse): EnkaProfileSummarized {
        val playerInfo = response.playerInfo
        val avatarList = response.avatarInfoList.map { avatar ->
            val characterMeta = getCharacterMeta(avatar.avatarId)
            val weapon = avatar.equipList.firstOrNull { it.weapon != null }?.let { equip ->
                val weaponMeta = getWeaponMeta(equip.itemId)
                EnkaWeaponSummarized(
                    id = equip.itemId,
                    name = weaponMeta?.name ?: "Unknown Weapon",
                    icon = weaponMeta?.icon?.let { EnkaAssetUrls.getIconUrl(it) } ?: "",
                    level = equip.weapon?.level ?: 1,
                    promoteLevel = equip.weapon?.promoteLevel,
                    refinement = equip.weapon?.affixMap?.values?.firstOrNull() ?: 0,
                    rarity = weaponMeta?.rankLevel ?: equip.flat.rankLevel,
                    mainStat = equip.flat.weaponStats.firstOrNull()?.let {
                        EnkaStatEntry(it.mainPropId, it.statValue)
                    },
                    subStat = equip.flat.weaponStats.getOrNull(1)?.let {
                        EnkaStatEntry(it.mainPropId, it.statValue)
                    },
                )
            }

            val artifacts = avatar.equipList
                .filter { it.reliquary != null }
                .map { equip ->
                    val artifactMeta = getArtifactMeta(equip.itemId)
                    EnkaArtifactSummarized(
                        id = equip.itemId,
                        name = artifactMeta?.name ?: equip.flat.nameTextMapHash,
                        icon = EnkaAssetUrls.getIconUrl(equip.flat.icon),
                        setID = artifactMeta?.setID ?: 0,
                        setName = equip.flat.setNameTextMapHash?.let { null }, // TODO: resolve from DB
                        level = equip.reliquary?.level ?: 0,
                        rarity = equip.flat.rankLevel,
                        equipType = equip.flat.equipType ?: "",
                        mainStat = EnkaStatEntry(
                            propId = equip.flat.reliquaryMainstat?.mainPropId ?: "",
                            value = equip.flat.reliquaryMainstat?.statValue ?: 0.0,
                        ),
                        subStats = equip.flat.reliquarySubstats.map {
                            EnkaStatEntry(it.mainPropId, it.statValue)
                        },
                    )
                }

            // Build stats map from fightPropMap
            val stats = avatar.fightPropMap.mapKeys { it.key.toString() }
                .mapValues { it.value }

            EnkaAvatarSummarized(
                avatarId = avatar.avatarId,
                name = characterMeta?.name ?: "Character #${avatar.avatarId}",
                element = characterMeta?.costElemType,
                level = avatar.propMap["4001"]?.value?.toIntOrNull() ?: 1,
                constellation = avatar.talentIdList.size,
                friendship = avatar.fetterInfo?.expLevel ?: 0,
                rarity = characterMeta?.let { getRarityFromQuality(it.qualityType) } ?: 5,
                iconUrl = characterMeta?.icon?.let { EnkaAssetUrls.getIconUrl(it) } ?: "",
                sideIconUrl = characterMeta?.sideIcon?.let { EnkaAssetUrls.getSideIconUrl(it) },
                weapon = weapon,
                artifacts = artifacts,
                stats = stats,
                talentLevels = avatar.skillLevelMap,
            )
        }

        val profilePictureUrl = playerInfo.profilePicture?.id?.let { id ->
            // TODO: resolve profile picture URL from database
            ""
        } ?: ""

        return EnkaProfileSummarized(
            uid = response.uid,
            nickname = playerInfo.nickname,
            level = playerInfo.level,
            worldLevel = playerInfo.worldLevel ?: 0,
            signature = "", // TODO: add signature to EnkaPlayerInfo
            headIconUrl = profilePictureUrl,
            nameCardId = playerInfo.nameCardId,
            finishAchievementNum = playerInfo.finishAchievementNum,
            towerFloorIndex = playerInfo.towerFloorIndex,
            towerLevelIndex = playerInfo.towerLevelIndex,
            avatarList = avatarList,
        )
    }

    /**
     * Get character metadata from database.
     * TODO: Implement actual database lookup
     */
    private fun getCharacterMeta(avatarId: Int): EnkaCharacterMeta? {
        // Placeholder - will be implemented with actual EnkaDB
        return null
    }

    /**
     * Get weapon metadata from database.
     * TODO: Implement actual database lookup
     */
    private fun getWeaponMeta(itemId: Int): EnkaWeaponMeta? {
        // Placeholder - will be implemented with actual EnkaDB
        return null
    }

    /**
     * Get artifact metadata from database.
     * TODO: Implement actual database lookup
     */
    private fun getArtifactMeta(itemId: Int): EnkaArtifactMeta? {
        // Placeholder - will be implemented with actual EnkaDB
        return null
    }

    /**
     * Get rarity from quality type string.
     */
    private fun getRarityFromQuality(qualityType: String?): Int {
        return when (qualityType) {
            "QUALITY_ORANGE" -> 5
            "QUALITY_PURPLE" -> 4
            "QUALITY_BLUE" -> 3
            "QUALITY_GREEN" -> 2
            else -> 5
        }
    }

    /**
     * Cache Enka response to local storage.
     */
    private fun cacheResponse(uid: String, response: EnkaResponse) {
        try {
            val adapter = moshi.adapter(EnkaResponse::class.java)
            val json = adapter.toJson(response)
            File(cacheDir, "$uid.json").writeText(json)
        } catch (e: Exception) {
            // Silently fail caching
        }
    }

    /**
     * Load cached Enka response from local storage.
     */
    private fun loadCachedResponse(uid: String): EnkaResponse? {
        return try {
            val file = File(cacheDir, "$uid.json")
            if (file.exists()) {
                val json = file.readText()
                moshi.adapter(EnkaResponse::class.java).fromJson(json)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Get all cached UIDs.
     */
    fun getCachedUids(): List<String> {
        return cacheDir.listFiles()
            ?.filter { it.extension == "json" }
            ?.map { it.nameWithoutExtension }
            ?: emptyList()
    }

    /**
     * Clear cache for a specific UID.
     */
    fun clearCache(uid: String) {
        File(cacheDir, "$uid.json").delete()
    }

    /**
     * Clear all cached data.
     */
    fun clearAllCache() {
        cacheDir.listFiles()?.forEach { it.delete() }
    }
}
