package com.hyalurion.lumi.helper.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity for gacha entries.
 * Stores individual gacha pull records.
 */
@Entity(
    tableName = "gacha_entries",
    indices = [
        Index(value = ["uid", "game"]),
        Index(value = ["uid", "game", "gachaType"]),
    ],
)
data class GachaEntryEntity(
    @PrimaryKey val id: String,           // Unique transaction ID
    val uid: String,                       // Game UID
    val game: String,                      // gi, hsr, zzz
    val gachaType: String,                 // Gacha pool type code
    val itemId: String,                    // Item ID
    val count: Int = 1,                    // Pull count
    val time: String,                      // Pull time (yyyy-MM-dd HH:mm:ss)
    val name: String? = null,              // Item name
    val lang: String? = null,              // Language code
    val itemType: String? = null,          // Character/Weapon/etc
    val rankType: Int? = null,             // Rarity (3/4/5)
    val gachaId: String? = null,           // Gacha pool ID
)

/**
 * Room entity for gacha profiles.
 * Tracks which UIDs have gacha records.
 */
@Entity(
    tableName = "gacha_profiles",
    primaryKeys = ["uid", "game"],
)
data class GachaProfileEntity(
    val uid: String,
    val game: String,
    val lastUpdateTime: String? = null,
)
