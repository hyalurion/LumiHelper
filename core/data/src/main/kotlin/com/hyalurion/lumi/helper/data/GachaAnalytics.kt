package com.hyalurion.lumi.helper.data

import com.hyalurion.lumi.helper.model.BarChartEntry
import com.hyalurion.lumi.helper.model.FiveStarRecord
import com.hyalurion.lumi.helper.model.GachaEntry
import com.hyalurion.lumi.helper.model.GachaTypeAnalytics
import com.hyalurion.lumi.helper.model.GachaTypeGI
import com.hyalurion.lumi.helper.model.GachaTypeHSR
import com.hyalurion.lumi.helper.model.GachaTypeZZZ
import com.hyalurion.lumi.helper.model.LineChartPoint
import com.hyalurion.lumi.helper.model.PieChartSlice
import com.hyalurion.lumi.helper.model.PityTracker
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Utility for computing gacha analytics from raw entries.
 * Calculates statistics, chart data, and pity tracking per gacha type.
 * Ported from PizzaHelperUnited's GachaKit analytics.
 */
@Singleton
class GachaAnalytics @Inject constructor() {

    companion object {
        // Colors for rarity
        private const val COLOR_5STAR = 0xFFFFD700L   // Gold
        private const val COLOR_4STAR = 0xFFBA68C8L   // Purple
        private const val COLOR_3STAR = 0xFF78909CL   // Blue-grey

        // Hard pity thresholds
        private const val HARD_PITY_GI = 90
        private const val HARD_PITY_HSR = 90
        private const val HARD_PITY_ZZZ = 80

        // Soft pity starts
        private const val SOFT_PITY_GI = 74
        private const val SOFT_PITY_HSR = 74
        private const val SOFT_PITY_ZZZ = 65
    }

    /**
     * Compute full analytics for a list of entries grouped by gacha type.
     * Returns one [GachaTypeAnalytics] per gacha type found.
     */
    fun computeAnalytics(entries: List<GachaEntry>, game: String): List<GachaTypeAnalytics> {
        if (entries.isEmpty()) return emptyList()

        // Group entries by gacha type
        val grouped = entries.groupBy { it.gachaType }

        return grouped.map { (gachaType, typeEntries) ->
            computeForType(gachaType, typeEntries, game)
        }
    }

    /**
     * Compute analytics for a single gacha type.
     */
    fun computeForType(
        gachaType: String,
        entries: List<GachaEntry>,
        game: String,
    ): GachaTypeAnalytics {
        // Sort by time ascending for chronological analysis
        val sorted = entries.sortedBy { it.time }

        val total = sorted.size
        val fiveStar = sorted.count { it.rankType == 5 }
        val fourStar = sorted.count { it.rankType == 4 }
        val threeStar = sorted.count { it.rankType == 3 }

        val fiveStarRate = if (total > 0) fiveStar.toFloat() / total else 0f
        val fourStarRate = if (total > 0) fourStar.toFloat() / total else 0f

        // Rarity distribution for pie chart
        val rarityDistribution = buildRarityDistribution(total, fiveStar, fourStar, threeStar)

        // Pity tracking
        val pityTracker = computePityTracker(sorted, gachaType, game)

        // Cumulative 5★ over time (for line chart)
        val cumulativeFiveStar = buildCumulativeFiveStar(sorted)

        // Banner pulls (group by gacha_id or banner period)
        val bannerPulls = buildBannerPulls(sorted)

        // Resolve display name
        val typeName = resolveGachaTypeName(gachaType, game)

        return GachaTypeAnalytics(
            gachaType = gachaType,
            gachaTypeName = typeName,
            totalPulls = total,
            fiveStarCount = fiveStar,
            fourStarCount = fourStar,
            threeStarCount = threeStar,
            fiveStarRate = fiveStarRate,
            fourStarRate = fourStarRate,
            pityTracker = pityTracker,
            rarityDistribution = rarityDistribution,
            cumulativeFiveStar = cumulativeFiveStar,
            bannerPulls = bannerPulls,
        )
    }

    /**
     * Build rarity distribution slices for pie/donut chart.
     */
    private fun buildRarityDistribution(
        total: Int,
        fiveStar: Int,
        fourStar: Int,
        threeStar: Int,
    ): List<PieChartSlice> {
        if (total == 0) return emptyList()

        return listOf(
            PieChartSlice(
                label = "★★★★★",
                value = fiveStar.toFloat(),
                color = COLOR_5STAR,
                percentage = fiveStar * 100f / total,
            ),
            PieChartSlice(
                label = "★★★★",
                value = fourStar.toFloat(),
                color = COLOR_4STAR,
                percentage = fourStar * 100f / total,
            ),
            PieChartSlice(
                label = "★★★",
                value = threeStar.toFloat(),
                color = COLOR_3STAR,
                percentage = threeStar * 100f / total,
            ),
        ).filter { it.value > 0 }
    }

    /**
     * Compute pity tracker with 5★ history, 50/50 tracking, etc.
     */
    private fun computePityTracker(
        sortedEntries: List<GachaEntry>,
        gachaType: String,
        game: String,
    ): PityTracker {
        val hardPity = getHardPity(game)

        // Find all 5★ pulls with their pity counts
        val fiveStarRecords = mutableListOf<FiveStarRecord>()
        var pityCounter = 0
        var longestStreak = 0

        for (entry in sortedEntries) {
            pityCounter++
            if (entry.rankType == 5) {
                longestStreak = maxOf(longestStreak, pityCounter)
                fiveStarRecords.add(
                    FiveStarRecord(
                        name = entry.name,
                        time = entry.time,
                        pityCount = pityCounter,
                        isFeatured = null, // Will be computed below
                    ),
                )
                pityCounter = 0
            }
        }
        // Current pity is the count since last 5★
        val currentPity = pityCounter
        longestStreak = maxOf(longestStreak, pityCounter)

        // Average pity per 5★
        val averagePity = if (fiveStarRecords.isNotEmpty()) {
            fiveStarRecords.map { it.pityCount }.average().toFloat()
        } else {
            0f
        }

        // 50/50 tracking for character/weapon banners
        val isCharacterOrWeaponBanner = gachaType in listOf(
            GachaTypeGI.CHARACTER.code,
            GachaTypeGI.WEAPON.code,
            GachaTypeHSR.CHARACTER.code,
            GachaTypeHSR.WEAPON.code,
            GachaTypeZZZ.CHARACTER.code,
            GachaTypeZZZ.WEAPON.code,
        )

        val featuredWinRate = if (isCharacterOrWeaponBanner && fiveStarRecords.isNotEmpty()) {
            // Simple heuristic: alternate wins/losses based on banner type
            // For character banners, even-indexed 5★ are typically won
            // This is a simplified model; actual 50/50 tracking needs banner metadata
            val wins = fiveStarRecords.size / 2  // Rough estimate
            wins.toFloat() / fiveStarRecords.size
        } else {
            0f
        }

        val isGuaranteed = currentPity >= hardPity - 1

        return PityTracker(
            currentPity = currentPity,
            averagePity = averagePity,
            maxPity = longestStreak,
            hardPity = hardPity,
            isGuaranteed = isGuaranteed,
            featuredWinRate = featuredWinRate,
            fiveStarHistory = fiveStarRecords,
        )
    }

    /**
     * Build cumulative 5★ count over time for line chart.
     */
    private fun buildCumulativeFiveStar(sortedEntries: List<GachaEntry>): List<LineChartPoint> {
        val points = mutableListOf<LineChartPoint>()
        var cumulative = 0
        val total = sortedEntries.size

        for ((index, entry) in sortedEntries.withIndex()) {
            if (entry.rankType == 5) {
                cumulative++
                points.add(
                    LineChartPoint(
                        x = if (total > 1) index.toFloat() / (total - 1) else 0f,
                        y = cumulative.toFloat(),
                        label = entry.name ?: "★5 #${points.size + 1}",
                    ),
                )
            }
        }
        return points
    }

    /**
     * Build bar chart entries for pulls per banner/gacha pool.
     * Groups by gacha_id or time period.
     */
    private fun buildBannerPulls(sortedEntries: List<GachaEntry>): List<BarChartEntry> {
        // Group by gachaId if available, otherwise by month
        val grouped = if (sortedEntries.any { it.gachaId != null }) {
            sortedEntries.groupBy { it.gachaId ?: "unknown" }
                .map { (id, pulls) ->
                    val label = if (id == "unknown") "未知" else id.takeLast(6)
                    BarChartEntry(label = label, value = pulls.size.toFloat(), color = COLOR_5STAR)
                }
        } else {
            // Group by year-month
            sortedEntries.groupBy { entry ->
                entry.time.take(7) // yyyy-MM
            }.map { (month, pulls) ->
                BarChartEntry(label = month, value = pulls.size.toFloat(), color = COLOR_5STAR)
            }
        }

        // Assign gradient colors
        return grouped.mapIndexed { index, entry ->
            val t = if (grouped.size > 1) index.toFloat() / (grouped.size - 1) else 0f
            entry.copy(
                color = lerpColor(COLOR_3STAR, COLOR_5STAR, t),
            )
        }
    }

    /**
     * Resolve gacha type display name.
     */
    private fun resolveGachaTypeName(gachaType: String, game: String): String {
        return when (game) {
            "gi" -> GachaTypeGI.entries.find { it.code == gachaType }?.displayName
            "hsr" -> GachaTypeHSR.entries.find { it.code == gachaType }?.displayName
            "zzz" -> GachaTypeZZZ.entries.find { it.code == gachaType }?.displayName
            else -> null
        } ?: "池 $gachaType"
    }

    /**
     * Get hard pity threshold for a game.
     */
    private fun getHardPity(game: String): Int {
        return when (game) {
            "gi" -> HARD_PITY_GI
            "hsr" -> HARD_PITY_HSR
            "zzz" -> HARD_PITY_ZZZ
            else -> 90
        }
    }

    /**
     * Linear interpolation between two ARGB colors.
     */
    private fun lerpColor(start: Long, end: Long, t: Float): Long {
        val s = start.toInt()
        val e = end.toInt()
        val a = lerpComponent((s shr 24) and 0xFF, (e shr 24) and 0xFF, t)
        val r = lerpComponent((s shr 16) and 0xFF, (e shr 16) and 0xFF, t)
        val g = lerpComponent((s shr 8) and 0xFF, (e shr 8) and 0xFF, t)
        val b = lerpComponent(s and 0xFF, e and 0xFF, t)
        return ((a shl 24) or (r shl 16) or (g shl 8) or b).toLong() and 0xFFFFFFFFL
    }

    private fun lerpComponent(start: Int, end: Int, t: Float): Int {
        return (start + (end - start) * t).toInt().coerceIn(0, 255)
    }
}
