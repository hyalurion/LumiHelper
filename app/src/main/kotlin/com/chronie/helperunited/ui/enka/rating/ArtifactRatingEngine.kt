package com.chronie.helperunited.ui.enka.rating

import com.chronie.helperunited.model.EnkaStatEntry

object ArtifactRatingEngine {

    private const val MAX_SUBSTAT_SCORE = 54.6
    private const val MAX_MAIN_STAT_BONUS = 10.0

    private val substatWeights = mapOf(
        "FIGHT_PROP_CRITICAL" to 1.0,
        "FIGHT_PROP_CRITICAL_HURT" to 0.95,
        "FIGHT_PROP_ATTACK_PERCENT" to 0.75,
        "FIGHT_PROP_HP_PERCENT" to 0.5,
        "FIGHT_PROP_DEFENSE_PERCENT" to 0.3,
        "FIGHT_PROP_ELEMENT_MASTERY" to 0.7,
        "FIGHT_PROP_CHARGE_EFFICIENCY" to 0.65,
        "FIGHT_PROP_ATTACK" to 0.5,
        "FIGHT_PROP_HP" to 0.35,
        "FIGHT_PROP_DEFENSE" to 0.25,
        "FIGHT_PROP_PHYSICAL_ADD_HURT" to 0.0,
        "FIGHT_PROP_HEAL_ADD" to 0.1,
    )

    private val mainStatBonus = mapOf(
        "FIGHT_PROP_CRITICAL" to 10.0,
        "FIGHT_PROP_CRITICAL_HURT" to 10.0,
        "FIGHT_PROP_ATTACK_PERCENT" to 8.0,
        "FIGHT_PROP_HP_PERCENT" to 6.0,
        "FIGHT_PROP_DEFENSE_PERCENT" to 6.0,
        "FIGHT_PROP_ELEMENT_MASTERY" to 7.0,
        "FIGHT_PROP_CHARGE_EFFICIENCY" to 7.0,
        "FIGHT_PROP_PHYSICAL_ADD_HURT" to 8.0,
        "FIGHT_PROP_FIRE_ADD_HURT" to 8.0,
        "FIGHT_PROP_ELEC_ADD_HURT" to 8.0,
        "FIGHT_PROP_WATER_ADD_HURT" to 8.0,
        "FIGHT_PROP_GRASS_ADD_HURT" to 8.0,
        "FIGHT_PROP_WIND_ADD_HURT" to 8.0,
        "FIGHT_PROP_ICE_ADD_HURT" to 8.0,
        "FIGHT_PROP_ROCK_ADD_HURT" to 8.0,
        "FIGHT_PROP_HEAL_ADD" to 5.0,
    )

    private val hsrSubstatWeights = mapOf(
        "FIGHT_PROP_CRITICAL" to 1.0,
        "FIGHT_PROP_CRITICAL_HURT" to 0.95,
        "FIGHT_PROP_ATTACK_PERCENT" to 0.75,
        "FIGHT_PROP_HP_PERCENT" to 0.5,
        "FIGHT_PROP_DEFENSE_PERCENT" to 0.3,
        "FIGHT_PROP_SPEED" to 0.85,
        "FIGHT_PROP_ATTACK" to 0.5,
        "FIGHT_PROP_HP" to 0.35,
        "FIGHT_PROP_DEFENSE" to 0.25,
    )

    data class ArtifactRating(
        val totalScore: Double,
        val substatScore: Double,
        val mainStatBonus: Double,
        val grade: String,
        val gradeColor: Long,
    )

    private fun propIdInMapping(propId: String, mapping: Map<String, Double>): Double {
        val key = if (propId.length <= 4 && propId.all { it.isDigit() }) {
            numericPropIdToName(propId)
        } else {
            propId
        }
        return mapping[key] ?: mapping[propId] ?: 0.0
    }

    fun calculateRating(
        mainStat: EnkaStatEntry,
        subStats: List<EnkaStatEntry>,
        level: Int,
        rarity: Int,
        gameType: String = "gi",
    ): ArtifactRating {
        val weights = if (gameType == "hsr") hsrSubstatWeights else substatWeights
        val maxSubScore = if (gameType == "hsr") 54.6 * 0.8 else MAX_SUBSTAT_SCORE

        var substatScore = 0.0
        subStats.forEach { stat ->
            val weight = propIdInMapping(stat.propId, weights)
            substatScore += stat.value * weight
        }

        val levelRatio = ((rarity * 4 + level) / (rarity * 4 + 20.0)).coerceIn(0.0, 1.0)
        substatScore = (substatScore / maxSubScore * 100.0).coerceIn(0.0, 100.0)

        val mainBonus = propIdInMapping(mainStat.propId, mainStatBonus)
        val totalScore = (substatScore * 0.85 + mainBonus / MAX_MAIN_STAT_BONUS * 15.0)
            .coerceIn(0.0, 100.0)

        val grade = when {
            totalScore >= 55.0 -> "SSS"
            totalScore >= 48.0 -> "SS"
            totalScore >= 42.0 -> "S"
            totalScore >= 36.0 -> "A"
            totalScore >= 28.0 -> "B"
            totalScore >= 20.0 -> "C"
            else -> "D"
        }

        val gradeColor = when (grade) {
            "SSS" -> 0xFFFF6600
            "SS" -> 0xFFFF3333
            "S" -> 0xFFFF9900
            "A" -> 0xFFCC33FF
            "B" -> 0xFF3366FF
            "C" -> 0xFF33AA33
            else -> 0xFF999999
        }

        return ArtifactRating(
            totalScore = totalScore,
            substatScore = substatScore,
            mainStatBonus = mainBonus,
            grade = grade,
            gradeColor = gradeColor,
        )
    }

    private fun numericPropIdToName(propId: String): String = when (propId) {
        "2000" -> "FIGHT_PROP_HP"
        "2001" -> "FIGHT_PROP_ATTACK"
        "2002" -> "FIGHT_PROP_DEFENSE"
        "2003" -> "FIGHT_PROP_HP_PERCENT"
        "2004" -> "FIGHT_PROP_ATTACK_PERCENT"
        "2005" -> "FIGHT_PROP_DEFENSE_PERCENT"
        "2006" -> "FIGHT_PROP_CRITICAL"
        "2007" -> "FIGHT_PROP_CRITICAL_HURT"
        "2008" -> "FIGHT_PROP_ELEMENT_MASTERY"
        "2009" -> "FIGHT_PROP_CHARGE_EFFICIENCY"
        "2010" -> "FIGHT_PROP_HEALED"
        "2011" -> "FIGHT_PROP_HEAL_ADD"
        "2029" -> "FIGHT_PROP_PHYSICAL_ADD_HURT"
        "2030" -> "FIGHT_PROP_FIRE_ADD_HURT"
        "2032" -> "FIGHT_PROP_GRASS_ADD_HURT"
        else -> propId
    }
}