package com.chronie.helperunited.base

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi

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

object SupportedGameAdapterFactory : JsonAdapter.Factory {
    override fun create(type: java.lang.reflect.Type, annotations: Set<Annotation>, moshi: Moshi): JsonAdapter<*>? {
        if (type != SupportedGame::class.java) return null
        return object : JsonAdapter<SupportedGame>() {
            override fun fromJson(reader: com.squareup.moshi.JsonReader): SupportedGame? {
                val rawValue = reader.readJsonValue() as? String ?: return null
                return SupportedGame.fromRawValue(rawValue)
            }

            override fun toJson(writer: com.squareup.moshi.JsonWriter, value: SupportedGame?) {
                if (value == null) {
                    writer.nullValue()
                } else {
                    writer.value(value.rawValue)
                }
            }
        }
    }
}
