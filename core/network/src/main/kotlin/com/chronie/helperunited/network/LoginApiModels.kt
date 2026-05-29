package com.chronie.helperunited.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// ============================================================
// QR Code Login APIs (米哈游护照 App API)
// Ref: https://github.com/TimeRainStarSky/TRSS-Plugin
// ============================================================

@JsonClass(generateAdapter = true)
data class GenerateQRCodeResponse(
    @Json(name = "retcode") val retCode: Int,
    val message: String,
    val data: QrCodeData?,
)

@JsonClass(generateAdapter = true)
data class QrCodeData(
    val url: String,
    val ticket: String,
)

@JsonClass(generateAdapter = true)
data class QueryQrCodeStatusResponse(
    @Json(name = "retcode") val retCode: Int,
    val message: String,
    val data: QrCodeStatusData?,
)

@JsonClass(generateAdapter = true)
data class QrCodeStatusData(
    val status: String,
    val tokens: List<TokenItem>? = null,
    @Json(name = "user_info") val userInfo: QrUserInfo? = null,
)

@JsonClass(generateAdapter = true)
data class TokenItem(
    @Json(name = "token_type") val tokenType: Int,
    val token: String,
)

@JsonClass(generateAdapter = true)
data class QrUserInfo(
    val aid: String?,
    val uid: String?,
    @Json(name = "account_id") val accountId: String?,
    val mid: String?,
)

// ============================================================
// Device Fingerprint API
// ============================================================

@JsonClass(generateAdapter = true)
data class DeviceFpResponse(
    @Json(name = "retcode") val retCode: Int,
    val message: String,
    val data: DeviceFpData?,
)

@JsonClass(generateAdapter = true)
data class DeviceFpData(
    @Json(name = "device_fp") val deviceFp: String,
    val msg: String,
    val code: Int,
)

// ============================================================
// Cookie Token Exchange API (via stoken)
// ============================================================

@JsonClass(generateAdapter = true)
data class CookieTokenResponse(
    @Json(name = "retcode") val retCode: Int,
    val message: String,
    val data: CookieTokenData?,
)

@JsonClass(generateAdapter = true)
data class CookieTokenData(
    @Json(name = "cookie_token") val cookieToken: String,
    @Json(name = "uid") val uid: String,
)

// ============================================================
// MultiToken (login_ticket → stoken/ltoken) for HoYoLab WebView flow
// ============================================================

@JsonClass(generateAdapter = true)
data class MultiTokenResponse(
    @Json(name = "retcode") val retCode: Int,
    val message: String,
    val data: MultiTokenData?,
)

@JsonClass(generateAdapter = true)
data class MultiTokenData(
    val stoken: String,
    val ltoken: String,
    val mid: String? = null,
)

// ============================================================
// User Game Roles (getUserGameRolesByCookie)
// ============================================================

@JsonClass(generateAdapter = true)
data class UserGameRolesResponse(
    @Json(name = "retcode") val retCode: Int,
    val message: String,
    val data: GameRolesData?,
)

@JsonClass(generateAdapter = true)
data class GameRolesData(
    val list: List<FetchedAccount>,
)

@JsonClass(generateAdapter = true)
data class FetchedAccount(
    @Json(name = "game_uid") val gameUid: String,
    @Json(name = "game_biz") val gameBiz: String,
    val nickname: String,
    val level: Int = 0,
    val region: String? = null,
)