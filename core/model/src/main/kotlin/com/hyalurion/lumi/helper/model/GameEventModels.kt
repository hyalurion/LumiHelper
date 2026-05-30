package com.hyalurion.lumi.helper.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.time.Instant

// ========== Game Event Calendar Models ==========
// Ported from PizzaHelperUnited's event calendar feature.

/**
 * Type of in-game event.
 */
enum class EventType(val displayName: String, val icon: String) {
    LIMITED_TIME("限时活动", "🎯"),
    BANNER("角色/武器卡池", "⭐"),
    WEAPON_BANNER("武器卡池", "⚔️"),
    MAINTENANCE("维护", "🔧"),
    VERSION_UPDATE("版本更新", "🆕"),
    BATTLE_PASS("纪行", "📜"),
    WEB_EVENT("网页活动", "🌐"),
    CODEX("图鉴", "📖"),
    OTHER("其他", "📌"),
}

/**
 * Represents a single in-game event.
 */
@JsonClass(generateAdapter = true)
data class GameEvent(
    val id: String,
    val name: String,
    val description: String = "",
    @Json(name = "start_time") val startTime: String,  // ISO-8601 or timestamp
    @Json(name = "end_time") val endTime: String,
    val type: EventType = EventType.OTHER,
    val game: String = "",  // "GI", "HSR", "ZZZ"
    @Json(name = "icon_url") val iconUrl: String? = null,
    @Json(name = "wiki_url") val wikiUrl: String? = null,
    @Json(name = "banner_url") val bannerUrl: String? = null,
) {
    /**
     * Parse start time to epoch seconds. Supports ISO-8601 and numeric timestamps.
     */
    val startEpoch: Long
        get() = parseTimestamp(startTime)

    /**
     * Parse end time to epoch seconds.
     */
    val endEpoch: Long
        get() = parseTimestamp(endTime)

    /**
     * Whether the event is currently active.
     */
    val isActive: Boolean
        get() {
            val now = System.currentTimeMillis() / 1000
            return now in startEpoch..endEpoch
        }

    /**
     * Whether the event has ended.
     */
    val hasEnded: Boolean
        get() = System.currentTimeMillis() / 1000 > endEpoch

    /**
     * Whether the event is upcoming (not yet started).
     */
    val isUpcoming: Boolean
        get() = System.currentTimeMillis() / 1000 < startEpoch

    /**
     * Remaining time in seconds (for active events) or time until start (for upcoming).
     */
    val remainingSeconds: Long
        get() {
            val now = System.currentTimeMillis() / 1000
            return when {
                isActive -> endEpoch - now
                isUpcoming -> startEpoch - now
                else -> 0
            }
        }

    companion object {
        fun parseTimestamp(value: String): Long {
            return try {
                // Try ISO-8601 first
                Instant.parse(value).epochSecond
            } catch (_: Exception) {
                try {
                    // Try numeric timestamp (seconds)
                    value.toLong()
                } catch (_: Exception) {
                    try {
                        // Try numeric timestamp (milliseconds)
                        value.toLong() / 1000
                    } catch (_: Exception) {
                        0
                    }
                }
            }
        }
    }
}

/**
 * Version information for a game, with countdown to next phase.
 */
@JsonClass(generateAdapter = true)
data class VersionInfo(
    val game: String,        // "GI", "HSR", "ZZZ"
    val version: String,     // e.g. "5.6"
    val phase: Int = 1,      // 1 or 2
    @Json(name = "start_date") val startDate: String,  // ISO-8601
    @Json(name = "end_date") val endDate: String,
    @Json(name = "next_version") val nextVersion: String? = null,
    @Json(name = "next_phase_start") val nextPhaseStart: String? = null,
) {
    val startEpoch: Long
        get() = GameEvent.parseTimestamp(startDate)

    val endEpoch: Long
        get() = GameEvent.parseTimestamp(endDate)

    val nextPhaseStartEpoch: Long
        get() = nextPhaseStart?.let { GameEvent.parseTimestamp(it) } ?: 0

    /**
     * Display string: "v5.6 Phase 1"
     */
    val displayVersion: String
        get() = "v$version Phase $phase"

    /**
     * Seconds remaining until the current phase ends.
     */
    val countdownSeconds: Long
        get() {
            val now = System.currentTimeMillis() / 1000
            return (endEpoch - now).coerceAtLeast(0)
        }

    /**
     * Whether this version phase is currently active.
     */
    val isActive: Boolean
        get() {
            val now = System.currentTimeMillis() / 1000
            return now in startEpoch..endEpoch
        }
}

/**
 * Response model for HoYoLab news list API.
 */
@JsonClass(generateAdapter = true)
data class HoYoLabNewsResponse(
    val retcode: Int,
    val message: String,
    val data: HoYoLabNewsData?,
)

@JsonClass(generateAdapter = true)
data class HoYoLabNewsData(
    val list: List<HoYoLabNewsPost>?,
    @Json(name = "last_id") val lastId: Int = 0,
    @Json(name = "is_last") val isLast: Boolean = true,
)

@JsonClass(generateAdapter = true)
data class HoYoLabNewsPost(
    @Json(name = "post_id") val postId: String = "",
    @Json(name = "subject") val subject: String = "",
    @Json(name = "content") val content: String = "",
    @Json(name = "cover") val cover: String = "",
    @Json(name = "created_at") val createdAt: Long = 0,
    @Json(name = "images") val images: List<String> = emptyList(),
    @Json(name = "official_tags") val officialTags: List<HoYoLabTag> = emptyList(),
    @Json(name = "post_type") val postType: Int = 0,
    @Json(name = "structured_content") val structuredContent: String = "",
)

@JsonClass(generateAdapter = true)
data class HoYoLabTag(
    @Json(name = "id") val id: Int = 0,
    @Json(name = "name") val name: String = "",
    @Json(name = "cover") val cover: String = "",
)
