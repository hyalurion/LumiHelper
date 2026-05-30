package com.hyalurion.lumi.helper.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// ========== HoYoLAB Auto Sign-in Models ==========

/** Request body for sign-in API calls */
@JsonClass(generateAdapter = true)
data class SignInRequest(
    @Json(name = "act_id") val actId: String,
    val region: String = "",
)

/** Result from sign-in API call */
@JsonClass(generateAdapter = true)
data class SignInResult(
    val code: String? = null,
    @Json(name = "first_bind") val firstBind: Boolean? = null,
    @Json(name = "is_risk") val isRisk: Boolean? = null,
)

/** Sign-in info/status from API */
@JsonClass(generateAdapter = true)
data class SignInfo(
    /** Total sign-in days */
    @Json(name = "total_sign_day") val totalSignDay: Int = 0,
    /** Whether signed in today */
    @Json(name = "is_sign") val isSign: Boolean = false,
    /** Whether first sign-in */
    @Json(name = "is_sub_sign") val isSubSign: Boolean? = null,
    /** Today's reward info */
    @Json(name = "sign_cnt_missed") val signCntMissed: Int = 0,
    @Json(name = "short_sign_cnt_missed") val shortSignCntMissed: Int = 0,
    val month: Int = 0,
)

/** Per-game sign-in status */
data class GameSignStatus(
    val game: com.hyalurion.lumi.helper.base.SupportedGame,
    val isSigned: Boolean,
    val totalSignDays: Int,
    val isLoading: Boolean = false,
    val error: String? = null,
)
