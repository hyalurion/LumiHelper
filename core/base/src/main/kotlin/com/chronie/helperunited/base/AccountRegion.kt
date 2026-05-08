package com.chronie.helperunited.base

/**
 * Account region - HoyoLab (global) or Miyoushe (CN).
 * Ported from PizzaHelperUnited's HoYo.AccountRegion.
 */
sealed class AccountRegion {
    abstract val game: SupportedGame

    data class HoyoLab(override val game: SupportedGame) : AccountRegion()
    data class Miyoushe(override val game: SupportedGame) : AccountRegion()

    val rawValue: String
        get() = when (this) {
            is HoyoLab -> when (game) {
                SupportedGame.GENSHIN_IMPACT -> "hk4e_global"
                SupportedGame.STAR_RAIL -> "hkrpg_global"
                SupportedGame.ZENLESS_ZONE -> "nap_global"
            }
            is Miyoushe -> when (game) {
                SupportedGame.GENSHIN_IMPACT -> "hk4e_cn"
                SupportedGame.STAR_RAIL -> "hkrpg_cn"
                SupportedGame.ZENLESS_ZONE -> "nap_cn"
            }
        }

    val isGlobal: Boolean get() = this is HoyoLab
    val isCN: Boolean get() = this is Miyoushe

    companion object {
        fun fromRawValue(rawValue: String): AccountRegion? = when (rawValue.lowercase()) {
            "hk4e_global" -> HoyoLab(SupportedGame.GENSHIN_IMPACT)
            "hk4e_cn" -> Miyoushe(SupportedGame.GENSHIN_IMPACT)
            "hkrpg_global" -> HoyoLab(SupportedGame.STAR_RAIL)
            "hkrpg_cn" -> Miyoushe(SupportedGame.STAR_RAIL)
            "nap_global" -> HoyoLab(SupportedGame.ZENLESS_ZONE)
            "nap_cn" -> Miyoushe(SupportedGame.ZENLESS_ZONE)
            else -> null
        }

        fun fromUid(uid: String, game: SupportedGame): AccountRegion? {
            val prefix = uid.firstOrNull()?.digitToIntOrNull() ?: return null
            return when {
                // GI: CN UIDs start with 1-5, global starts with 6-9 (overseas)
                game == SupportedGame.GENSHIN_IMPACT && prefix in 1..5 ->
                    Miyoushe(game)
                game == SupportedGame.GENSHIN_IMPACT && prefix in 6..9 ->
                    HoyoLab(game)
                // HSR: CN UIDs start with 1, global starts with 6-9
                game == SupportedGame.STAR_RAIL && prefix == 1 ->
                    Miyoushe(game)
                game == SupportedGame.STAR_RAIL && prefix in 6..9 ->
                    HoyoLab(game)
                // ZZZ: similar logic
                game == SupportedGame.ZENLESS_ZONE && prefix in 1..5 ->
                    Miyoushe(game)
                game == SupportedGame.ZENLESS_ZONE && prefix in 6..9 ->
                    HoyoLab(game)
                else -> null
            }
        }
    }
}
