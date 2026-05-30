package com.hyalurion.lumi.helper.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// ========== HoYoLAB Character List API Models ==========
// These models match the HoYoLAB game_record API response format.
// Unlike Enka models, these come from HoYoLAB's own character endpoint.

// ========== Request Body ==========

@JsonClass(generateAdapter = true)
data class CharacterListRequest(
    @Json(name = "role_id") val roleId: String,
    val server: String,
)

// ========== Genshin Impact Character List ==========

@JsonClass(generateAdapter = true)
data class HoYoLabCharacterListGI(
    @Json(name = "avatars") val avatars: List<HoYoLabCharacterGI>,
    @Json(name = "role") val role: HoYoLabRoleInfo?,
)

@JsonClass(generateAdapter = true)
data class HoYoLabRoleInfo(
    @Json(name = "AvatarUrl") val avatarUrl: String?,
    @Json(name = "nickname") val nickname: String,
    @Json(name = "region") val region: String,
    @Json(name = "level") val level: Int,
)

@JsonClass(generateAdapter = true)
data class HoYoLabCharacterGI(
    @Json(name = "id") val id: Int,
    @Json(name = "image") val image: String?,
    @Json(name = "name") val name: String,
    @Json(name = "element") val element: String,
    @Json(name = "fetter") val fetter: Int,           // friendship level
    @Json(name = "level") val level: Int,
    @Json(name = "rarity") val rarity: Int,
    @Json(name = "actived_constellation_num") val activedConstellationNum: Int,
    @Json(name = "card_image") val cardImage: String?,
    @Json(name = "is_chosen") val isChosen: Boolean = false,
    @Json(name = "weapon") val weapon: HoYoLabWeaponGI?,
    @Json(name = "reliquaries") val reliquaries: List<HoYoLabReliquaryGI> = emptyList(),
    @Json(name = "constellations") val constellations: List<HoYoLabConstellationGI> = emptyList(),
)

@JsonClass(generateAdapter = true)
data class HoYoLabWeaponGI(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String,
    @Json(name = "icon") val icon: String?,
    @Json(name = "type") val type: Int,
    @Json(name = "rarity") val rarity: Int,
    @Json(name = "level") val level: Int,
    @Json(name = "affix_level") val affixLevel: Int,  // refinement rank
)

@JsonClass(generateAdapter = true)
data class HoYoLabReliquaryGI(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String?,
    @Json(name = "icon") val icon: String?,
    @Json(name = "pos") val pos: Int,                 // artifact slot (1-5)
    @Json(name = "rarity") val rarity: Int,
    @Json(name = "level") val level: Int,
    @Json(name = "set") val reliquarySet: HoYoLabReliquarySet?,
)

@JsonClass(generateAdapter = true)
data class HoYoLabReliquarySet(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String?,
    @Json(name = "affixes") val affixes: List<HoYoLabReliquaryAffix> = emptyList(),
)

@JsonClass(generateAdapter = true)
data class HoYoLabReliquaryAffix(
    @Json(name = "activation_number") val activationNumber: Int,
    @Json(name = "effect") val effect: String?,
)

@JsonClass(generateAdapter = true)
data class HoYoLabConstellationGI(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String?,
    @Json(name = "icon") val icon: String?,
    @Json(name = "effect") val effect: String?,
    @Json(name = "is_actived") val isActived: Boolean,
    @Json(name = "pos") val pos: Int,
)

// ========== Star Rail Character List ==========

@JsonClass(generateAdapter = true)
data class HoYoLabCharacterListHSR(
    @Json(name = "avatar_list") val avatarList: List<HoYoLabCharacterHSR>,
    @Json(name = "cur_head_icon_url") val curHeadIconUrl: String?,
    @Json(name = "nickname") val nickname: String?,
    @Json(name = "level") val playerLevel: Int?,
)

@JsonClass(generateAdapter = true)
data class HoYoLabCharacterHSR(
    @Json(name = "avatar_id") val avatarId: Int,
    @Json(name = "level") val level: Int,
    @Json(name = "name") val name: String,
    @Json(name = "element") val element: String?,
    @Json(name = "rarity") val rarity: Int,
    @Json(name = "rank") val rank: Int,               // eidolon count (constellation equivalent)
    @Json(name = "image") val image: String?,
    @Json(name = "portrait") val portrait: String?,
    @Json(name = "is_chosen") val isChosen: Boolean = false,
    @Json(name = "promotion") val promotion: Int = 0,  // ascension level
    @Json(name = "relic") val relics: List<HoYoLabRelicHSR> = emptyList(),
    @Json(name = "light_cone") val lightCone: HoYoLabLightConeHSR?,
)

@JsonClass(generateAdapter = true)
data class HoYoLabLightConeHSR(
    @Json(name = "item_id") val itemId: Int,
    @Json(name = "name") val name: String?,
    @Json(name = "icon") val icon: String?,
    @Json(name = "rarity") val rarity: Int,
    @Json(name = "level") val level: Int,
    @Json(name = "rank") val rank: Int,               // superimposition
    @Json(name = "promotion") val promotion: Int = 0,
)

@JsonClass(generateAdapter = true)
data class HoYoLabRelicHSR(
    @Json(name = "item_id") val itemId: Int,
    @Json(name = "name") val name: String?,
    @Json(name = "icon") val icon: String?,
    @Json(name = "rarity") val rarity: Int,
    @Json(name = "level") val level: Int,
    @Json(name = "type") val type: Int,
    @Json(name = "set_id") val setId: Int?,
)

// ========== Zenless Zone Zero Character List ==========

@JsonClass(generateAdapter = true)
data class HoYoLabCharacterListZZZ(
    @Json(name = "avatar_list") val avatarList: List<HoYoLabCharacterZZZ>,
    @Json(name = "cur_head_icon_url") val curHeadIconUrl: String?,
    @Json(name = "nickname") val nickname: String?,
    @Json(name = "level") val playerLevel: Int?,
)

@JsonClass(generateAdapter = true)
data class HoYoLabCharacterZZZ(
    @Json(name = "id") val id: Int,
    @Json(name = "level") val level: Int,
    @Json(name = "name_mi18n") val nameMi18n: String? = null,
    @Json(name = "full_name_mi18n") val fullNameMi18n: String? = null,
    @Json(name = "element_type") val elementType: Int,
    @Json(name = "star_level") val starLevel: Int,              // S=5, A=4, B=3
    @Json(name = "role_square_url") val roleSquareUrl: String? = null,
    @Json(name = "role_vertical_painting_url") val roleVerticalPaintingUrl: String? = null,
    @Json(name = "rank") val rank: Int = 0,                     // Mindscape Cinema (0-6)
    @Json(name = "w_engine") val wEngine: HoYoLabWEngineZZZ? = null,
    @Json(name = "weapon") val weapon: HoYoLabWEngineZZZ? = null,  // alternate field
    @Json(name = "equip_list") val equipList: List<HoYoLabDriveDiscZZZ> = emptyList(),
    @Json(name = "skills") val skills: List<HoYoLabSkillZZZ> = emptyList(),
    @Json(name = "properties") val properties: List<HoYoLabPropertyZZZ> = emptyList(),
    @Json(name = "talent_switch") val talentSwitch: List<HoYoLabTalentZZZ> = emptyList(),
)

@JsonClass(generateAdapter = true)
data class HoYoLabWEngineZZZ(
    @Json(name = "id") val id: Int,
    @Json(name = "level") val level: Int,
    @Json(name = "name") val name: String? = null,
    @Json(name = "star_level") val starLevel: Int = 0,
    @Json(name = "icon") val icon: String? = null,
    @Json(name = "upgrade_level") val upgradeLevel: Int = 0,
    @Json(name = "equip_icon") val equipIcon: String? = null,
    @Json(name = "gacha_card_url") val gachaCardUrl: String? = null,
)

@JsonClass(generateAdapter = true)
data class HoYoLabDriveDiscZZZ(
    @Json(name = "id") val id: Int,
    @Json(name = "level") val level: Int,
    @Json(name = "name") val name: String? = null,
    @Json(name = "star_level") val starLevel: Int = 0,
    @Json(name = "icon") val icon: String? = null,
    @Json(name = "suit_id") val suitId: Int = 0,
    @Json(name = "suit_name") val suitName: String? = null,
    @Json(name = "main_property") val mainProperty: HoYoLabDiscPropertyZZZ? = null,
    @Json(name = "sub_properties") val subProperties: List<HoYoLabDiscPropertyZZZ> = emptyList(),
    @Json(name = "equipment_type") val equipmentType: Int = 0,
)

@JsonClass(generateAdapter = true)
data class HoYoLabDiscPropertyZZZ(
    @Json(name = "property_name") val propertyName: String? = null,
    @Json(name = "property_id") val propertyId: Int = 0,
    @Json(name = "value") val value: String? = null,
    @Json(name = "times") val times: Int = 0,
)

@JsonClass(generateAdapter = true)
data class HoYoLabPropertyZZZ(
    @Json(name = "property_name") val propertyName: String? = null,
    @Json(name = "property_id") val propertyId: Int = 0,
    @Json(name = "base") val base: String? = null,
    @Json(name = "add") val add: String? = null,
    @Json(name = "final") val final: String? = null,
)

@JsonClass(generateAdapter = true)
data class HoYoLabSkillZZZ(
    @Json(name = "skill_type") val skillType: Int = 0,
    @Json(name = "level") val level: Int = 0,
    @Json(name = "name") val name: String? = null,
    @Json(name = "desc") val desc: String? = null,
    @Json(name = "icon") val icon: String? = null,
)

@JsonClass(generateAdapter = true)
data class HoYoLabTalentZZZ(
    @Json(name = "talent_id") val talentId: Int = 0,
    @Json(name = "name") val name: String? = null,
    @Json(name = "desc") val desc: String? = null,
    @Json(name = "is_unlocked") val isUnlocked: Boolean = false,
)

// ========== Unified Display Model ==========
// A common model used for displaying characters in the UI regardless of game.

data class HoYoLabCharacterDisplay(
    val id: String,             // unique identifier: "${game}_${charId}"
    val gameId: String,         // "GI", "HSR", or "ZZZ"
    val characterId: Int,
    val name: String,
    val element: String,
    val level: Int,
    val rarity: Int,
    val constellation: Int,     // actived_constellation_num / eidolon rank / mindscape cinema
    val friendship: Int,        // fetter level / 0 for HSR/ZZZ
    val iconUrl: String?,
    val weaponName: String?,    // weapon / light cone / w-engine name
    val weaponLevel: Int?,      // weapon / light cone / w-engine level
    val weaponRarity: Int?,     // weapon / light cone / w-engine rarity
    val weaponRefinement: Int?, // affix_level / superimposition / enhancement
)
