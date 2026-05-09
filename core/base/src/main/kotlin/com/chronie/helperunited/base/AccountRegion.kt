package com.chronie.helperunited.base

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Account region - HoyoLab (global) or Miyoushe (CN).
 * Ported from PizzaHelperUnited's HoYo.AccountRegion.
 */
@JsonClass(generateAdapter = true)
data class AccountRegion(
    val type: RegionType,
    val game: SupportedGame,
) {
    val rawValue: String
        get() = when (type) {
            RegionType.HOYOLAB -> when (game) {
                SupportedGame.GENSHIN_IMPACT -> "hk4e_global"
                SupportedGame.STAR_RAIL -> "hkrpg_global"
                SupportedGame.ZENLESS_ZONE -> "nap_global"
            }
            RegionType.MIYOUSHE -> when (game) {
                SupportedGame.GENSHIN_IMPACT -> "hk4e_cn"
                SupportedGame.STAR_RAIL -> "hkrpg_cn"
                SupportedGame.ZENLESS_ZONE -> "nap_cn"
            }
        }

    val isGlobal: Boolean get() = type == RegionType.HOYOLAB
    val isCN: Boolean get() = type == RegionType.MIYOUSHE

    companion object {
        fun fromRawValue(rawValue: String): AccountRegion? = when (rawValue.lowercase()) {
            "hk4e_global" -> AccountRegion(RegionType.HOYOLAB, SupportedGame.GENSHIN_IMPACT)
            "hk4e_cn" -> AccountRegion(RegionType.MIYOUSHE, SupportedGame.GENSHIN_IMPACT)
            "hkrpg_global" -> AccountRegion(RegionType.HOYOLAB, SupportedGame.STAR_RAIL)
            "hkrpg_cn" -> AccountRegion(RegionType.MIYOUSHE, SupportedGame.STAR_RAIL)
            "nap_global" -> AccountRegion(RegionType.HOYOLAB, SupportedGame.ZENLESS_ZONE)
            "nap_cn" -> AccountRegion(RegionType.MIYOUSHE, SupportedGame.ZENLESS_ZONE)
            else -> null
        }

        fun fromUid(uid: String, game: SupportedGame): AccountRegion? {
            val prefix = uid.firstOrNull()?.digitToIntOrNull() ?: return null
            return when {
                // GI: CN UIDs start with 1-5, global starts with 6-9 (overseas)
                game == SupportedGame.GENSHIN_IMPACT && prefix in 1..5 ->
                    AccountRegion(RegionType.MIYOUSHE, game)
                game == SupportedGame.GENSHIN_IMPACT && prefix in 6..9 ->
                    AccountRegion(RegionType.HOYOLAB, game)
                // HSR: CN UIDs start with 1, global starts with 6-9
                game == SupportedGame.STAR_RAIL && prefix == 1 ->
                    AccountRegion(RegionType.MIYOUSHE, game)
                game == SupportedGame.STAR_RAIL && prefix in 6..9 ->
                    AccountRegion(RegionType.HOYOLAB, game)
                // ZZZ: similar logic
                game == SupportedGame.ZENLESS_ZONE && prefix in 1..5 ->
                    AccountRegion(RegionType.MIYOUSHE, game)
                game == SupportedGame.ZENLESS_ZONE && prefix in 6..9 ->
                    AccountRegion(RegionType.HOYOLAB, game)
                else -> null
            }
        }
    }
}

enum class RegionType {
    @Json(name = "hoyolab") HOYOLAB,
    @Json(name = "miyoushe") MIYOUSHE,
}
