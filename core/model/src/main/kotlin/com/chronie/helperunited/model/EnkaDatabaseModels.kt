package com.chronie.helperunited.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// ========== Enka Database Models ==========
// Ported from PizzaHelperUnited's EnkaKit/EnkaDB

/**
 * Enka database for character/weapon/artifact metadata lookup.
 * Contains text map hashes and asset mappings.
 */
@JsonClass(generateAdapter = true)
data class EnkaDatabase(
    @Json(name = "locTable") val locTable: Map<String, String> = emptyMap(),
    @Json(name = "characters") val characters: Map<String, EnkaCharacterMeta> = emptyMap(),
    @Json(name = "weapons") val weapons: Map<String, EnkaWeaponMeta> = emptyMap(),
    @Json(name = "artifacts") val artifacts: Map<String, EnkaArtifactMeta> = emptyMap(),
    @Json(name = "profilePictures") val profilePictures: Map<String, EnkaProfilePicture> = emptyMap(),
)

@JsonClass(generateAdapter = true)
data class EnkaCharacterMeta(
    @Json(name = "NameTextMapHash") val nameTextMapHash: Long? = null,
    @Json(name = "SideIconName") val sideIconName: String? = null,
    @Json(name = "QualityType") val qualityType: String? = null,
    @Json(name = "Element") val element: String? = null,
    @Json(name = "WeaponType") val weaponType: String? = null,
    @Json(name = "Costumes") val costumes: Map<String, EnkaCostume>? = null,
    @Json(name = "name") val name: String? = null, // Resolved from locTable
) {
    val icon: String?
        get() = sideIconName?.replace("Side_", "")
}

@JsonClass(generateAdapter = true)
data class EnkaCostume(
    @Json(name = "icon") val icon: String? = null,
    @Json(name = "sideIconName") val sideIconName: String? = null,
    @Json(name = "art") val art: String? = null,
)

@JsonClass(generateAdapter = true)
data class EnkaWeaponMeta(
    @Json(name = "NameTextMapHash") val nameTextMapHash: Long? = null,
    @Json(name = "Icon") val icon: String? = null,
    @Json(name = "RankLevel") val rankLevel: Int? = null,
    @Json(name = "WeaponType") val weaponType: String? = null,
    @Json(name = "name") val name: String? = null, // Resolved from locTable
)

@JsonClass(generateAdapter = true)
data class EnkaArtifactMeta(
    @Json(name = "NameTextMapHash") val nameTextMapHash: Long? = null,
    @Json(name = "Icon") val icon: String? = null,
    @Json(name = "SetID") val setID: Int? = null,
    @Json(name = "RankLevel") val rankLevel: Int? = null,
    @Json(name = "EquipType") val equipType: String? = null,
    @Json(name = "name") val name: String? = null, // Resolved from locTable
)

@JsonClass(generateAdapter = true)
data class EnkaProfilePicture(
    @Json(name = "Id") val id: Int? = null,
    @Json(name = "IconPath") val iconPath: String? = null,
)

// ========== Enka Avatar Summarized ==========
// Processed avatar data ready for UI display

data class EnkaAvatarSummarized(
    val avatarId: Int,
    val name: String,
    val element: String?,
    val level: Int,
    val constellation: Int,
    val friendship: Int,
    val rarity: Int,
    val iconUrl: String,
    val sideIconUrl: String?,
    val weapon: EnkaWeaponSummarized?,
    val artifacts: List<EnkaArtifactSummarized>,
    val stats: Map<String, Double>,
    val talentLevels: Map<Int, Int>,
)

data class EnkaWeaponSummarized(
    val id: Int,
    val name: String,
    val icon: String,
    val level: Int,
    val promoteLevel: Int?,
    val refinement: Int,
    val rarity: Int,
    val mainStat: EnkaStatEntry?,
    val subStat: EnkaStatEntry?,
)

data class EnkaArtifactSummarized(
    val id: Int,
    val name: String,
    val icon: String,
    val setID: Int,
    val setName: String?,
    val level: Int,
    val rarity: Int,
    val equipType: String,
    val mainStat: EnkaStatEntry,
    val subStats: List<EnkaStatEntry>,
)

data class EnkaStatEntry(
    val propId: String,
    val value: Double,
)

// ========== Enka Profile Summarized ==========
// Processed profile data ready for UI display

data class EnkaProfileSummarized(
    val uid: String,
    val nickname: String,
    val level: Int,
    val worldLevel: Int,
    val signature: String,
    val headIconUrl: String,
    val nameCardId: Int,
    val finishAchievementNum: Int,
    val towerFloorIndex: Int?,
    val towerLevelIndex: Int?,
    val avatarList: List<EnkaAvatarSummarized>,
)

// ========== Enka Game Type ==========

enum class EnkaGameType(val prefix: String, val apiPath: String) {
    GENSHIN("gi", "genshin"),
    STAR_RAIL("hsr", "hkrpg"),
    ZENLESS("zzz", "nap"),
}
