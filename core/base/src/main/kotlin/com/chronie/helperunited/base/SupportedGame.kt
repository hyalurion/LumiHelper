package com.chronie.helperunited.base

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Supported HoYoverse games.
 * Ported from PizzaHelperUnited's Pizza.SupportedGame.
 */
@JsonClass(generateAdapter = true)
data class SupportedGame(
    @Json(name = "raw_value") val rawValue: String,
    @Json(name = "hoyo_biz_id") val hoyoBizId: String,
    @Json(name = "uid_prefix") val uidPrefix: String,
    @Json(name = "display_name") val displayName: String,
    @Json(name = "short_name") val shortName: String,
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
