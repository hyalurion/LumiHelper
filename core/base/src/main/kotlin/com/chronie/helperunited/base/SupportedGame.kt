package com.chronie.helperunited.base

/**
 * Supported HoYoverse games.
 * Ported from PizzaHelperUnited's Pizza.SupportedGame.
 */
enum class SupportedGame(
    val rawValue: String,
    val hoyoBizId: String,
    val uidPrefix: String,
    val displayName: String,
    val shortName: String,
) {
    GENSHIN_IMPACT(
        rawValue = "GI",
        hoyoBizId = "hk4e",
        uidPrefix = "GI",
        displayName = "原神",
        shortName = "原神",
    ),
    STAR_RAIL(
        rawValue = "HSR",
        hoyoBizId = "hkrpg",
        uidPrefix = "SR",
        displayName = "崩坏：星穹铁道",
        shortName = "星铁",
    ),
    ZENLESS_ZONE(
        rawValue = "ZZZ",
        hoyoBizId = "nap",
        uidPrefix = "ZZ",
        displayName = "绝区零",
        shortName = "绝区零",
    );

    val id: String get() = rawValue

    fun withUid(uid: String): String = "$uidPrefix-$uid"

    companion object {
        fun fromRawValue(rawValue: String): SupportedGame? =
            entries.find { it.rawValue == rawValue }

        fun fromUidPrefix(prefix: String): SupportedGame? =
            entries.find { it.uidPrefix == prefix }

        fun fromBizId(bizId: String): SupportedGame? =
            entries.find { it.hoyoBizId == bizId }
    }
}
