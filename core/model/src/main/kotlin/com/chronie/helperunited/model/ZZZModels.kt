package com.chronie.helperunited.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// ========== ZZZ Character (Agent) Models ==========
// Ported from PizzaHelperUnited's ZZZ character showcase logic.
// Based on HoYoLAB API: game_record/nap/api/character

/**
 * Response wrapper for ZZZ character list API.
 */
@JsonClass(generateAdapter = true)
data class ZZZCharacterListResponse(
    @Json(name = "avatar_list") val avatarList: List<ZZZAgentInfo>,
    @Json(name = "cur_head_icon_url") val curHeadIconUrl: String? = null,
)

/**
 * ZZZ Agent (character) information.
 * Maps to HoYoLAB's avatar_list entry for NAP (Zenless Zone Zero).
 */
@JsonClass(generateAdapter = true)
data class ZZZAgentInfo(
    @Json(name = "id") val id: Int,
    @Json(name = "level") val level: Int,
    @Json(name = "name_mi18n") val nameMi18n: String? = null,
    @Json(name = "full_name_mi18n") val fullNameMi18n: String? = null,
    @Json(name = "element_type") val elementType: Int,
    @Json(name = "star_level") val starLevel: Int,           // Rarity (S/A/B)
    @Json(name = "role_square_url") val roleSquareUrl: String? = null,
    @Json(name = "role_vertical_painting_url") val roleVerticalPaintingUrl: String? = null,
    @Json(name = "equip") val equip: ZZZEquipInfo? = null,
    @Json(name = "w_engine") val wEngine: ZZZWEngineInfo? = null,
    @Json(name = "equip_list") val equipList: List<ZZZDriveDiscInfo> = emptyList(),
    @Json(name = "skill_level") val skillLevel: ZZZSkillLevel? = null,
    @Json(name = "rank") val rank: Int = 0,                  // Mindscape Cinema / 命座等效
    @Json(name = "properties") val properties: List<ZZZProperty> = emptyList(),
    @Json(name = "talent_switch") val talentSwitch: List<ZZZTalentSwitch> = emptyList(),
    @Json(name = "weapon") val weapon: ZZZWEngineInfo? = null,  // Alternate field name
    @Json(name = "skills") val skills: List<ZZZSkill> = emptyList(),
)

/**
 * ZZZ equipment summary (legacy field).
 */
@JsonClass(generateAdapter = true)
data class ZZZEquipInfo(
    @Json(name = "id") val id: Int,
    @Json(name = "level") val level: Int,
    @Json(name = "name") val name: String? = null,
    @Json(name = "star_level") val starLevel: Int = 0,
    @Json(name = "icon") val icon: String? = null,
)

/**
 * ZZZ W-Engine (weapon equivalent) information.
 */
@JsonClass(generateAdapter = true)
data class ZZZWEngineInfo(
    @Json(name = "id") val id: Int,
    @Json(name = "level") val level: Int,
    @Json(name = "name") val name: String? = null,
    @Json(name = "star_level") val starLevel: Int = 0,
    @Json(name = "icon") val icon: String? = null,
    @Json(name = "upgrade_level") val upgradeLevel: Int = 0,  // Enhancement level (精炼)
    @Json(name = "equip_icon") val equipIcon: String? = null,
    @Json(name = "gacha_card_url") val gachaCardUrl: String? = null,
)

/**
 * ZZZ Drive Disc (relic/artifact equivalent) information.
 */
@JsonClass(generateAdapter = true)
data class ZZZDriveDiscInfo(
    @Json(name = "id") val id: Int,
    @Json(name = "level") val level: Int,
    @Json(name = "name") val name: String? = null,
    @Json(name = "star_level") val starLevel: Int = 0,
    @Json(name = "icon") val icon: String? = null,
    @Json(name = "suit_id") val suitId: Int = 0,             // Drive Disc set ID
    @Json(name = "suit_name") val suitName: String? = null,  // Drive Disc set name
    @Json(name = "main_property") val mainProperty: ZZZDiscProperty? = null,
    @Json(name = "sub_properties") val subProperties: List<ZZZDiscProperty> = emptyList(),
    @Json(name = "equipment_type") val equipmentType: Int = 0, // Slot position (1-6)
)

/**
 * ZZZ Drive Disc property (main stat or sub stat).
 */
@JsonClass(generateAdapter = true)
data class ZZZDiscProperty(
    @Json(name = "property_name") val propertyName: String? = null,
    @Json(name = "property_id") val propertyId: Int = 0,
    @Json(name = "value") val value: String? = null,
    @Json(name = "times") val times: Int = 0,                // Number of upgrades
)

/**
 * ZZZ agent property (stats).
 */
@JsonClass(generateAdapter = true)
data class ZZZProperty(
    @Json(name = "property_name") val propertyName: String? = null,
    @Json(name = "property_id") val propertyId: Int = 0,
    @Json(name = "base") val base: String? = null,
    @Json(name = "add") val add: String? = null,
    @Json(name = "final") val final: String? = null,
)

/**
 * ZZZ skill level info.
 */
@JsonClass(generateAdapter = true)
data class ZZZSkillLevel(
    @Json(name = "basic_attack") val basicAttack: Int = 0,
    @Json(name = "dodge") val dodge: Int = 0,
    @Json(name = "assist") val assist: Int = 0,
    @Json(name = "special_attack") val specialAttack: Int = 0,
    @Json(name = "chain_attack") val chainAttack: Int = 0,
)

/**
 * ZZZ talent switch (mindscape cinema).
 */
@JsonClass(generateAdapter = true)
data class ZZZTalentSwitch(
    @Json(name = "talent_id") val talentId: Int = 0,
    @Json(name = "name") val name: String? = null,
    @Json(name = "desc") val desc: String? = null,
    @Json(name = "is_unlocked") val isUnlocked: Boolean = false,
)

/**
 * ZZZ skill info.
 */
@JsonClass(generateAdapter = true)
data class ZZZSkill(
    @Json(name = "skill_type") val skillType: Int = 0,
    @Json(name = "level") val level: Int = 0,
    @Json(name = "name") val name: String? = null,
    @Json(name = "desc") val desc: String? = null,
    @Json(name = "icon") val icon: String? = null,
)

// ========== ZZZ Element / Attribute Types ==========

/**
 * ZZZ element (attribute) types.
 */
enum class ZZZElementType(val code: Int, val displayName: String, val icon: String) {
    PHYSICAL(200, "物理", "⚡"),
    FIRE(201, "火", "🔥"),
    ICE(202, "冰", "❄️"),
    ELECTRIC(203, "电", "⚡"),
    ETHER(205, "以太", "🌀"),
    ;

    companion object {
        fun fromCode(code: Int): ZZZElementType =
            entries.find { it.code == code } ?: PHYSICAL
    }
}

// ========== ZZZ Summarized Models (for UI) ==========

/**
 * Summarized ZZZ agent data for UI display.
 * Similar to EnkaAvatarSummarized but ZZZ-specific.
 */
data class ZZZAgentSummarized(
    val id: Int,
    val name: String,
    val fullName: String?,
    val level: Int,
    val rarity: Int,           // S=5, A=4, B=3
    val elementType: ZZZElementType,
    val rank: Int,             // Mindscape Cinema level (0-6)
    val iconUrl: String?,
    val verticalPaintingUrl: String?,
    val wEngine: ZZZWEngineSummarized?,
    val driveDiscs: List<ZZZDriveDiscSummarized>,
    val skills: ZZZSkillLevel?,
    val properties: List<ZZZProperty>,
)

data class ZZZWEngineSummarized(
    val id: Int,
    val name: String,
    val level: Int,
    val rarity: Int,
    val enhancementLevel: Int,  // 精炼等级
    val iconUrl: String?,
)

data class ZZZDriveDiscSummarized(
    val id: Int,
    val name: String,
    val level: Int,
    val rarity: Int,
    val suitId: Int,
    val suitName: String?,
    val slot: Int,              // 1-6
    val iconUrl: String?,
    val mainStat: ZZZDiscProperty?,
    val subStats: List<ZZZDiscProperty>,
)

/**
 * Summarized ZZZ profile for UI display.
 */
data class ZZZProfileSummarized(
    val uid: String,
    val nickname: String,
    val level: Int,
    val headIconUrl: String?,
    val agentList: List<ZZZAgentSummarized>,
)

// ========== ZZZ Gacha Record Models ==========

/**
 * ZZZ gacha record API response.
 * Endpoint: game_record/nap/api/gacha
 */
@JsonClass(generateAdapter = true)
data class ZZZGachaResponse(
    @Json(name = "list") val list: List<ZZZGachaEntry>,
    @Json(name = "region") val region: String? = null,
    @Json(name = "region_time_zone") val regionTimeZone: Int = 0,
)

/**
 * Single ZZZ gacha pull entry from API.
 */
@JsonClass(generateAdapter = true)
data class ZZZGachaEntry(
    @Json(name = "uid") val uid: String,
    @Json(name = "gacha_id") val gachaId: String,
    @Json(name = "gacha_type") val gachaType: Int,
    @Json(name = "item_id") val itemId: String,
    @Json(name = "count") val count: String,
    @Json(name = "time") val time: String,
    @Json(name = "name") val name: String,
    @Json(name = "lang") val lang: String,
    @Json(name = "item_type") val itemType: String,
    @Json(name = "rank_type") val rankType: String,
    @Json(name = "id") val id: String,
)
