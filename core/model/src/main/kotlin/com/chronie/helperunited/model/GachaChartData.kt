package com.chronie.helperunited.model

/**
 * Chart data models for gacha analytics visualization.
 * Used by Canvas-based Compose charts.
 */

/**
 * A slice in a pie/donut chart.
 */
data class PieChartSlice(
    val label: String,
    val value: Float,
    val color: Long, // ARGB color
    val percentage: Float,
)

/**
 * A point in a line chart (cumulative 5★ over time).
 */
data class LineChartPoint(
    val x: Float, // Normalized position (0..1)
    val y: Float, // Cumulative count
    val label: String, // Date or item name
)

/**
 * An entry in a bar chart (pulls per banner).
 */
data class BarChartEntry(
    val label: String,
    val value: Float,
    val color: Long,
)

/**
 * Pity tracking information for a gacha type.
 */
data class PityTracker(
    val currentPity: Int,          // Pulls since last 5★
    val averagePity: Float,        // Average pulls per 5★
    val maxPity: Int,              // Longest streak without 5★
    val hardPity: Int,             // Guaranteed 5★ at this count (90 for GI, 90 for HSR, 80 for ZZZ)
    val isGuaranteed: Boolean,     // Next 5★ is guaranteed featured
    val featuredWinRate: Float,    // Win rate for featured character (50/50)
    val fiveStarHistory: List<FiveStarRecord>, // Chronological 5★ pulls
)

/**
 * A recorded 5★ pull with pity context.
 */
data class FiveStarRecord(
    val name: String?,
    val time: String,
    val pityCount: Int, // How many pulls it took to get this 5★
    val isFeatured: Boolean?, // null if unknown, true/false for character/weapon banner
)

/**
 * Comprehensive analytics for a gacha type.
 */
data class GachaTypeAnalytics(
    val gachaType: String,
    val gachaTypeName: String,
    val totalPulls: Int,
    val fiveStarCount: Int,
    val fourStarCount: Int,
    val threeStarCount: Int,
    val fiveStarRate: Float,
    val fourStarRate: Float,
    val pityTracker: PityTracker,
    val rarityDistribution: List<PieChartSlice>,
    val cumulativeFiveStar: List<LineChartPoint>,
    val bannerPulls: List<BarChartEntry>,
)
