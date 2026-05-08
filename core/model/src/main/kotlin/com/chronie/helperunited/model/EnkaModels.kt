package com.chronie.helperunited.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// ========== Enka Showcase Models ==========

/**
 * Enka.Network player info response.
 * Ported from PizzaHelperUnited's EnkaKit.
 */
@JsonClass(generateAdapter = true)
data class EnkaPlayerInfo(
    @Json(name = "nickname") val nickname: String,
    @Json(name = "level") val level: Int,
    @Json(name = "worldLevel") val worldLevel: Int? = null,
    @Json(name = "nameCardId") val nameCardId: Int,
    @Json(name = "finishAchievementNum") val finishAchievementNum: Int = 0,
    @Json(name = "towerFloorIndex") val towerFloorIndex: Int? = null,
    @Json(name = "towerLevelIndex") val towerLevelIndex: Int? = null,
    @Json(name = "showAvatarInfoList") val showAvatarInfoList: List<ShowAvatarInfo> = emptyList(),
    @Json(name = "showNameCardIdList") val showNameCardIdList: List<Int> = emptyList(),
    @Json(name = "profilePicture") val profilePicture: ProfilePicture? = null,
)

@JsonClass(generateAdapter = true)
data class ShowAvatarInfo(
    @Json(name = "avatarId") val avatarId: Int,
    @Json(name = "level") val level: Int? = null,
    @Json(name = "costumeId") val costumeId: Int? = null,
)

@JsonClass(generateAdapter = true)
data class ProfilePicture(
    @Json(name = "id") val id: Int,
)

@JsonClass(generateAdapter = true)
data class EnkaAvatarDetail(
    @Json(name = "avatarId") val avatarId: Int,
    @Json(name = "propMap") val propMap: Map<String, PropValue>,
    @Json(name = "talentIdList") val talentIdList: List<Int> = emptyList(),
    @Json(name = "fightPropMap") val fightPropMap: Map<Int, Double> = emptyMap(),
    @Json(name = "skillDepotId") val skillDepotId: Int,
    @Json(name = "inherentProudSkillList") val inherentProudSkillList: List<Int> = emptyList(),
    @Json(name = "skillLevelMap") val skillLevelMap: Map<Int, Int>,
    @Json(name = "equipList") val equipList: List<EnkaEquip>,
    @Json(name = "fetterInfo") val fetterInfo: FetterInfo? = null,
)

@JsonClass(generateAdapter = true)
data class PropValue(
    @Json(name = "type") val type: String,
    @Json(name = "val") val value: String?,
)

@JsonClass(generateAdapter = true)
data class FetterInfo(
    @Json(name = "expLevel") val expLevel: Int,
)

@JsonClass(generateAdapter = true)
data class EnkaEquip(
    @Json(name = "itemId") val itemId: Int,
    @Json(name = "reliquary") val reliquary: EnkaReliquary? = null,
    @Json(name = "weapon") val weapon: EnkaWeapon? = null,
    @Json(name = "flat") val flat: EnkaEquipFlat,
)

@JsonClass(generateAdapter = true)
data class EnkaReliquary(
    @Json(name = "level") val level: Int,
    @Json(name = "mainPropId") val mainPropId: Int,
    @Json(name = "appendPropIdList") val appendPropIdList: List<Int> = emptyList(),
)

@JsonClass(generateAdapter = true)
data class EnkaWeapon(
    @Json(name = "level") val level: Int,
    @Json(name = "promoteLevel") val promoteLevel: Int? = null,
    @Json(name = "affixMap") val affixMap: Map<Int, Int> = emptyMap(),
)

@JsonClass(generateAdapter = true)
data class EnkaEquipFlat(
    @Json(name = "nameTextMapHash") val nameTextMapHash: String,
    @Json(name = "setNameTextMapHash") val setNameTextMapHash: String? = null,
    @Json(name = "rankLevel") val rankLevel: Int,
    @Json(name = "reliquaryMainstat") val reliquaryMainstat: ReliquaryStat? = null,
    @Json(name = "reliquarySubstats") val reliquarySubstats: List<ReliquaryStat> = emptyList(),
    @Json(name = "weaponStats") val weaponStats: List<ReliquaryStat> = emptyList(),
    @Json(name = "itemType") val itemType: String,
    @Json(name = "icon") val icon: String,
    @Json(name = "equipType") val equipType: String? = null,
)

@JsonClass(generateAdapter = true)
data class ReliquaryStat(
    @Json(name = "mainPropId") val mainPropId: String,
    @Json(name = "statValue") val statValue: Double,
)

// ========== Full Enka Response ==========

@JsonClass(generateAdapter = true)
data class EnkaResponse(
    @Json(name = "playerInfo") val playerInfo: EnkaPlayerInfo,
    @Json(name = "avatarInfoList") val avatarInfoList: List<EnkaAvatarDetail> = emptyList(),
    @Json(name = "ttl") val ttl: Int = 0,
    @Json(name = "uid") val uid: String,
)
