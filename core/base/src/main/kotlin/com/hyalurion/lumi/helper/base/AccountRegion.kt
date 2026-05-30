package com.hyalurion.lumi.helper.base

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi

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
                else -> throw IllegalArgumentException("Unsupported game: ${game.rawValue}")
            }
            RegionType.MIYOUSHE -> when (game) {
                SupportedGame.GENSHIN_IMPACT -> "hk4e_cn"
                SupportedGame.STAR_RAIL -> "hkrpg_cn"
                SupportedGame.ZENLESS_ZONE -> "nap_cn"
                else -> throw IllegalArgumentException("Unsupported game: ${game.rawValue}")
            }
        }

    val isGlobal: Boolean get() = type == RegionType.HOYOLAB
    val isCN: Boolean get() = type == RegionType.MIYOUSHE
    val displayName: String get() = type.displayName

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
                game == SupportedGame.GENSHIN_IMPACT && prefix in 1..5 ->
                    AccountRegion(RegionType.MIYOUSHE, game)
                game == SupportedGame.GENSHIN_IMPACT && prefix in 6..9 ->
                    AccountRegion(RegionType.HOYOLAB, game)
                game == SupportedGame.STAR_RAIL && prefix == 1 ->
                    AccountRegion(RegionType.MIYOUSHE, game)
                game == SupportedGame.STAR_RAIL && prefix in 6..9 ->
                    AccountRegion(RegionType.HOYOLAB, game)
                game == SupportedGame.ZENLESS_ZONE && prefix in 1..5 ->
                    AccountRegion(RegionType.MIYOUSHE, game)
                game == SupportedGame.ZENLESS_ZONE && prefix in 6..9 ->
                    AccountRegion(RegionType.HOYOLAB, game)
                else -> null
            }
        }
    }
}

enum class RegionType(val displayName: String) {
    HOYOLAB("HoYoLAB"),
    MIYOUSHE("米游社"),
}

object AccountRegionAdapterFactory : JsonAdapter.Factory {
    override fun create(type: java.lang.reflect.Type, annotations: Set<Annotation>, moshi: Moshi): JsonAdapter<*>? {
        if (type != AccountRegion::class.java) return null
        return object : JsonAdapter<AccountRegion>() {
            override fun fromJson(reader: com.squareup.moshi.JsonReader): AccountRegion? {
                val rawValue = reader.readJsonValue() as? String ?: return null
                return AccountRegion.fromRawValue(rawValue)
            }

            override fun toJson(writer: com.squareup.moshi.JsonWriter, value: AccountRegion?) {
                if (value == null) {
                    writer.nullValue()
                } else {
                    writer.value(value.rawValue)
                }
            }
        }
    }
}
