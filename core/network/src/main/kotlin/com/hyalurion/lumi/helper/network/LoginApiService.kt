package com.hyalurion.lumi.helper.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface LoginApiService {

    // ============================================================
    // QR Code Login (米哈游护照 App API)
    // ============================================================

    @POST("account/ma-cn-passport/app/createQRLogin")
    suspend fun createQRLogin(
        @Body body: Map<String, String>,
    ): GenerateQRCodeResponse

    @POST("account/ma-cn-passport/app/queryQRLoginStatus")
    suspend fun queryQRLoginStatus(
        @Body body: Map<String, String>,
    ): QueryQrCodeStatusResponse

    // ============================================================
    // Cookie Token Exchange
    // ============================================================

    @GET("auth/api/getCookieAccountInfoBySToken")
    suspend fun getCookieTokenBySToken(
        @Query("game_biz") gameBiz: String,
    ): CookieTokenResponse

    // ============================================================
    // MultiToken (login_ticket -> stoken/ltoken)
    // ============================================================

    @GET("auth/api/getMultiTokenByLoginTicket")
    suspend fun getMultiTokenByLoginTicket(
        @Query("login_ticket") loginTicket: String,
        @Query("uid") loginUid: String,
        @Query("token_types") tokenTypes: Int = 3,
    ): MultiTokenResponse

    // ============================================================
    // User Game Roles (fetch all UIDs bound to this account)
    // ============================================================

    @GET("binding/api/getUserGameRolesByCookie")
    suspend fun getUserGameRolesByCookie(
        @Query("game_biz") gameBiz: String,
    ): UserGameRolesResponse
}