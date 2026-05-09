package com.chronie.helperunited.base

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson

/**
 * Supported HoYoverse games.
 * Ported from PizzaHelperUnited's Pizza.SupportedGame.
 */
data class SupportedGame(
    val rawValue: String,
    val hoyoBizId: String,
    val uidPrefix: String,
    val displayName: String,
    val shortName: String,
) {
    val id: String get() = rawValue

    fun withUid(uid: String): String = "$uidPrefix-$uid"

    companion object {
        val GENSHIN_IMPACT = SupportedGame(
            rawValue = "GI",
            hoyoBizId = "hk4e",
            uidPrefix = "GI",
            displayName = "原神",
            shortName = "原神",
        )
        val STAR_RAIL = SupportedGame(
            rawValue = "HSR",
            hoyoBizId = "hkrpg",
            uidPrefix = "SR",
            displayName = "崩坏：星穹铁道",
            shortName = "星铁",
        )
        val ZENLESS_ZONE = SupportedGame(
            rawValue = "ZZZ",
            hoyoBizId = "nap",
            uidPrefix = "ZZ",
            displayName = "绝区零",
            shortName = "绝区零",
        )

        val entries: List<SupportedGame> get() = listOf(GENSHIN_IMPACT, STAR_RAIL, ZENLESS_ZONE)

        fun fromRawValue(rawValue: String): SupportedGame? =
            entries.find { it.rawValue == rawValue }

        fun fromUidPrefix(prefix: String): SupportedGame? =
            entries.find { it.uidPrefix == prefix }

        fun fromBizId(bizId: String): SupportedGame? =
            entries.find { it.hoyoBizId == bizId }
    }
}

/**
 * Moshi adapter for SupportedGame.
 * Serializes as raw value string (e.g., "GI", "HSR", "ZZZ").
 */
class SupportedGameAdapter {
    @ToJson
    fun toJson(game: SupportedGame): String = game.rawValue

    @FromJson
    fun fromJson(rawValue: String): SupportedGame? = SupportedGame.fromRawValue(rawValue)
}
