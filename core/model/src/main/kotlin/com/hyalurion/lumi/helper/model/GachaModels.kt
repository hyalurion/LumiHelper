package com.hyalurion.lumi.helper.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// ========== Gacha Models ==========
// Ported from PizzaHelperUnited's GachaKit

/**
 * Gacha entry - represents a single gacha pull.
 * Compatible with UIGF v4 / SRGF / ZGF formats.
 */
@JsonClass(generateAdapter = true)
data class GachaEntry(
    @Json(name = "id") val id: String,           // Unique transaction ID
    @Json(name = "uid") val uid: String,          // Game UID
    @Json(name = "game") val game: String,        // gi, hsr, zzz
    @Json(name = "gachaType") val gachaType: String, // Gacha pool type
    @Json(name = "itemId") val itemId: String,    // Item ID
    @Json(name = "count") val count: Int = 1,     // Pull count
    @Json(name = "time") val time: String,        // Pull time (yyyy-MM-dd HH:mm:ss)
    @Json(name = "name") val name: String?,       // Item name
    @Json(name = "lang") val lang: String?,       // Language code
    @Json(name = "itemType") val itemType: String?, // Character/Weapon
    @Json(name = "rankType") val rankType: Int?,  // Rarity (3/4/5)
    @Json(name = "gacha_id") val gachaId: String?, // Gacha pool ID
)

/**
 * Gacha profile - represents a user's gacha records for a specific game.
 */
@JsonClass(generateAdapter = true)
data class GachaProfile(
    @Json(name = "uid") val uid: String,
    @Json(name = "game") val game: String,
    @Json(name = "lastUpdateTime") val lastUpdateTime: String? = null,
)

/**
 * Gacha pool types for Genshin Impact.
 */
enum class GachaTypeGI(val code: String, val displayName: String) {
    BEGINNER("100", "新手池"),
    STANDARD("200", "常驻池"),
    CHARACTER("301", "角色池"),
    WEAPON("302", "武器池"),
    CHRONICLED("400", "集录祈愿"),
}

/**
 * Gacha pool types for Star Rail.
 */
enum class GachaTypeHSR(val code: String, val displayName: String) {
    BEGINNER("1", "新手池"),
    STANDARD("2", "常驻池"),
    CHARACTER("11", "角色池"),
    WEAPON("12", "光锥池"),
}

/**
 * Gacha pool types for Zenless Zone Zero.
 */
enum class GachaTypeZZZ(val code: String, val displayName: String) {
    STANDARD("1", "常驻池"),
    CHARACTER("2", "角色池"),
    WEAPON("3", "音擎池"),
    BANGBOO("5", "邦布池"),
}

/**
 * Item rank type (rarity).
 */
enum class GachaRankType(val value: Int, val displayName: String) {
    BLUE(3, "三星"),
    PURPLE(4, "四星"),
    GOLD(5, "五星"),
}

/**
 * Item type.
 */
enum class GachaItemType(val value: String, val displayName: String) {
    CHARACTER("角色", "角色"),
    WEAPON("武器", "武器"),
    EQUIPMENT("光锥", "光锥"),  // HSR
    W_ENGINE("音擎", "音擎"),  // ZZZ
    BANGBOO("邦布", "邦布"),    // ZZZ
}

// ========== UIGF Export Format ==========

/**
 * UIGF v4 export format.
 */
@JsonClass(generateAdapter = true)
data class UIGFExport(
    @Json(name = "info") val info: UIGFInfo,
    @Json(name = "list") val list: List<GachaEntry>,
)

@JsonClass(generateAdapter = true)
data class UIGFInfo(
    @Json(name = "uid") val uid: String,
    @Json(name = "lang") val lang: String,
    @Json(name = "export_time") val exportTime: String,
    @Json(name = "export_app") val exportApp: String = "LumiHelper",
    @Json(name = "export_app_version") val exportAppVersion: String = "1.0",
    @Json(name = "uigf_version") val uigfVersion: String = "v4.0",
)
