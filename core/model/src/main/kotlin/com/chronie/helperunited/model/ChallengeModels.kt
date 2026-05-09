package com.chronie.helperunited.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// ========== Genshin Impact Spiral Abyss ==========

@JsonClass(generateAdapter = true)
data class SpiralAbyssResponse(
    @Json(name = "schedule_id") val scheduleId: Int,
    @Json(name = "start_time") val startTime: String,
    @Json(name = "end_time") val endTime: String,
    @Json(name = "total_battle_times") val totalBattleTimes: Int,
    @Json(name = "total_win_times") val totalWinTimes: Int,
    @Json(name = "max_floor") val maxFloor: String,
    @Json(name = "reveal_rank") val revealRank: List<AbyssRankEntry>,
    @Json(name = "defeat_rank") val defeatRank: List<AbyssRankEntry>,
    @Json(name = "damage_rank") val damageRank: List<AbyssRankEntry>,
    @Json(name = "take_damage_rank") val takeDamageRank: List<AbyssRankEntry>,
    @Json(name = "normal_skill_rank") val normalSkillRank: List<AbyssRankEntry>,
    @Json(name = "energy_skill_rank") val energySkillRank: List<AbyssRankEntry>,
    @Json(name = "floors") val floors: List<AbyssFloor>,
)

@JsonClass(generateAdapter = true)
data class AbyssRankEntry(
    @Json(name = "avatar_id") val avatarId: Int,
    @Json(name = "avatar_icon") val avatarIcon: String,
    @Json(name = "value") val value: Int,
    @Json(name = "rarity") val rarity: Int = 0,
)

@JsonClass(generateAdapter = true)
data class AbyssFloor(
    val index: Int,
    @Json(name = "icon") val icon: String?,
    @Json(name = "is_unlock") val isUnlock: Boolean,
    @Json(name = "star") val star: Int,          // stars earned this floor
    @Json(name = "max_star") val maxStar: Int,    // max possible stars
    val levels: List<AbyssLevel>,
)

@JsonClass(generateAdapter = true)
data class AbyssLevel(
    val index: Int,
    val star: Int,           // stars earned in this chamber
    @Json(name = "max_star") val maxStar: Int,
    val battles: List<AbyssBattle>,
)

@JsonClass(generateAdapter = true)
data class AbyssBattle(
    val index: Int,          // half 1 or 2
    @Json(name = "timestamp") val timestamp: String,
    val avatars: List<AbyssAvatar>,
)

@JsonClass(generateAdapter = true)
data class AbyssAvatar(
    val id: Int,
    @Json(name = "icon") val icon: String,
    val level: Int,
    val rarity: Int,
    @Json(name = "actived_constellation_num") val activedConstellationNum: Int = 0,
)

// ========== Honkai: Star Rail Challenge (Memory of Chaos / Pure Fiction) ==========

@JsonClass(generateAdapter = true)
data class ChallengeResponse(
    @Json(name = "schedule_id") val scheduleId: Int,
    @Json(name = "begin_time") val beginTime: String,
    @Json(name = "end_time") val endTime: String,
    @Json(name = "total_battle_num") val totalBattleNum: Int,
    @Json(name = "has_data") val hasData: Boolean = false,
    @Json(name = "groups") val groups: List<ChallengeGroup> = emptyList(),
)

@JsonClass(generateAdapter = true)
data class ChallengeGroup(
    @Json(name = "begin_time") val beginTime: String?,
    @Json(name = "end_time") val endTime: String?,
    @Json(name = "schedule_id") val scheduleId: Int = 0,
    @Json(name = "star") val star: Int = 0,           // total stars earned
    @Json(name = "max_star") val maxStar: Int = 0,    // max possible stars
    @Json(name = "floor") val floors: List<ChallengeFloor> = emptyList(),
)

@JsonClass(generateAdapter = true)
data class ChallengeFloor(
    @Json(name = "name") val name: String,
    @Json(name = "star") val star: Int = 0,
    @Json(name = "max_star") val maxStar: Int = 0,
    @Json(name = "node_1") val node1: ChallengeNode? = null,
    @Json(name = "node_2") val node2: ChallengeNode? = null,
)

@JsonClass(generateAdapter = true)
data class ChallengeNode(
    @Json(name = "avatars") val avatars: List<ChallengeAvatar> = emptyList(),
)

@JsonClass(generateAdapter = true)
data class ChallengeAvatar(
    @Json(name = "id") val id: Int,
    @Json(name = "level") val level: Int = 0,
    @Json(name = "name") val name: String? = null,
    @Json(name = "rarity") val rarity: Int = 0,
    @Json(name = "icon") val icon: String? = null,
    @Json(name = "rank") val rank: Int = 0,           // eidolon
)

// ========== HSR Challenge Query Types ==========

/**
 * Challenge types for HSR's API.
 * schedule_type=1: Memory of Chaos (混沌回忆)
 * schedule_type=2: Pure Fiction (虚构叙事)
 * All use the same `/game_record/hkrpg/api/challenge` endpoint.
 */
enum class HsrChallengeType(val scheduleType: Int, val displayName: String) {
    MEMORY_OF_CHAOS(1, "混沌回忆"),
    PURE_FICTION(2, "虚构叙事"),
}
