package com.chronie.helperunited.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.time.Instant

// ========== Genshin Impact Daily Note ==========

@JsonClass(generateAdapter = true)
data class DailyNoteGI(
    @Json(name = "current_resin") val currentResin: Int,
    @Json(name = "max_resin") val maxResin: Int,
    @Json(name = "resin_recovery_time") val resinRecoveryTimeSeconds: String,
    @Json(name = "finished_task_num") val finishedTaskNum: Int,
    @Json(name = "total_task_num") val totalTaskNum: Int,
    @Json(name = "is_extra_task_reward_received") val isExtraTaskRewardReceived: Boolean,
    @Json(name = "remain_resin_discount_num") val remainResinDiscountNum: Int,
    @Json(name = "resin_discount_num_limit") val resinDiscountNumLimit: Int,
    @Json(name = "current_expedition_num") val currentExpeditionNum: Int,
    @Json(name = "max_expedition_num") val maxExpeditionNum: Int,
    @Json(name = "expeditions") val expeditions: List<ExpeditionGI>,
    @Json(name = "current_home_coin") val currentHomeCoin: Int,
    @Json(name = "max_home_coin") val maxHomeCoin: Int,
    @Json(name = "home_coin_recovery_time") val homeCoinRecoveryTimeSeconds: String,
    @Json(name = "transformer") val transformer: TransformerGI?,
    val fetchedAt: Long = Instant.now().epochSecond,
) {
    val resinFullTime: Instant?
        get() = if (resinRecoveryTimeSeconds != "0") {
            Instant.now().plusSeconds(resinRecoveryTimeSeconds.toLongOrNull() ?: 0)
        } else null

    val isResinFull: Boolean get() = currentResin >= maxResin

    val homeCoinFullTime: Instant?
        get() = if (homeCoinRecoveryTimeSeconds != "0") {
            Instant.now().plusSeconds(homeCoinRecoveryTimeSeconds.toLongOrNull() ?: 0)
        } else null

    val allDiscountsUsedUp: Boolean get() = remainResinDiscountNum == 0
}

@JsonClass(generateAdapter = true)
data class ExpeditionGI(
    @Json(name = "avatar_side_icon") val avatarSideIcon: String,
    val status: String, // "Ongoing" or "Finished"
    @Json(name = "remaining_time") val remainingTimeSeconds: String,
) {
    val isFinished: Boolean get() = status == "Finished" || remainingTimeSeconds == "0"
}

@JsonClass(generateAdapter = true)
data class TransformerGI(
    val obtained: Boolean,
    @Json(name = "recovery_time") val recoveryTime: RecoveryTime?,
) {
    @JsonClass(generateAdapter = true)
    data class RecoveryTime(
        @Json(name = "Day") val day: Int,
        @Json(name = "Hour") val hour: Int,
        @Json(name = "Minute") val minute: Int,
        @Json(name = "Second") val second: Int,
        @Json(name = "reached") val reached: Boolean,
    )
}

// ========== Star Rail Daily Note ==========

@JsonClass(generateAdapter = true)
data class DailyNoteHSR(
    @Json(name = "current_stamina") val currentStamina: Int,
    @Json(name = "max_stamina") val maxStamina: Int,
    @Json(name = "stamina_recover_time") val staminaRecoverTimeSeconds: Int,
    @Json(name = "accepted_epedition_num") val acceptedExpeditionNum: Int,
    @Json(name = "total_expedition_num") val totalExpeditionNum: Int,
    @Json(name = "expeditions") val expeditions: List<ExpeditionHSR>,
    @Json(name = "current_train_score") val currentTrainScore: Int,
    @Json(name = "max_train_score") val maxTrainScore: Int,
    @Json(name = "current_rogue_score") val currentRogueScore: Int,
    @Json(name = "max_rogue_score") val maxRogueScore: Int,
    @Json(name = "is_reserve_exhausted") val isReserveExhausted: Boolean,
    @Json(name = "current_reserve_stamina") val currentReserveStamina: Int,
    val fetchedAt: Long = Instant.now().epochSecond,
) {
    val staminaFullTime: Instant?
        get() = if (staminaRecoverTimeSeconds > 0) {
            Instant.now().plusSeconds(staminaRecoverTimeSeconds.toLong())
        } else null

    val isStaminaFull: Boolean get() = currentStamina >= maxStamina
}

@JsonClass(generateAdapter = true)
data class ExpeditionHSR(
    val avatars: List<String>,
    val status: String, // "Running" or "Finished"
    @Json(name = "remaining_time") val remainingTimeSeconds: Int,
    @Json(name = "name") val name: String?,
) {
    val isFinished: Boolean get() = status == "Finished"
}

// ========== Zenless Zone Zero Daily Note ==========

@JsonClass(generateAdapter = true)
data class DailyNoteZZZ(
    @Json(name = "energy") val energy: EnergyZZZ,
    @Json(name = "vitality") val vitality: VitalityZZZ,
    @Json(name = "card_sign") val cardSign: String,
    @Json(name = "bounty_commission") val bountyCommission: BountyCommissionZZZ,
    @Json(name = "survey_points") val surveyPoints: SurveyPointsZZZ,
    @Json(name = "drive_disc") val driveDisc: DriveDiscZZZ,
    val fetchedAt: Long = Instant.now().epochSecond,
)

@JsonClass(generateAdapter = true)
data class EnergyZZZ(
    val progress: Int,
    @Json(name = "max_progress") val maxProgress: Int,
    @Json(name = "restore") val restore: Int,
)

@JsonClass(generateAdapter = true)
data class VitalityZZZ(
    val current: Int,
    val max: Int,
)

@JsonClass(generateAdapter = true)
data class BountyCommissionZZZ(
    val num: Int,
    val total: Int,
)

@JsonClass(generateAdapter = true)
data class SurveyPointsZZZ(
    val num: Int,
    val total: Int,
)

@JsonClass(generateAdapter = true)
data class DriveDiscZZZ(
    val num: Int,
    val total: Int,
)
