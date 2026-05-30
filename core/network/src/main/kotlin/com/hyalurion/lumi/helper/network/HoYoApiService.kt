package com.hyalurion.lumi.helper.network

import com.hyalurion.lumi.helper.model.CharacterListRequest
import com.hyalurion.lumi.helper.model.DailyNoteGI
import com.hyalurion.lumi.helper.model.DailyNoteHSR
import com.hyalurion.lumi.helper.model.DailyNoteZZZ
import com.hyalurion.lumi.helper.model.EnkaResponse
import com.hyalurion.lumi.helper.model.HoYoApiResponse
import com.hyalurion.lumi.helper.model.HoYoLabCharacterListGI
import com.hyalurion.lumi.helper.model.HoYoLabCharacterListHSR
import com.hyalurion.lumi.helper.model.HoYoLabCharacterListZZZ
import com.hyalurion.lumi.helper.model.SignInRequest
import com.hyalurion.lumi.helper.model.SignInResult
import com.hyalurion.lumi.helper.model.SignInfo
import com.hyalurion.lumi.helper.model.SpiralAbyssResponse
import com.hyalurion.lumi.helper.model.ChallengeResponse
import com.hyalurion.lumi.helper.model.ZZZGachaResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * HoYoLab Record API service (daily notes, character showcase, etc.)
 * Ported from PizzaHelperUnited's HoYoAPI endpoints.
 */
interface HoYoRecordApi {

    // ========== Genshin Impact ==========

    /** GI Real-time notes (daily notes) */
    @GET("game_record/genshin/api/dailyNote")
    suspend fun getDailyNoteGI(
        @Query("role_id") uid: String,
        @Query("server") server: String,
    ): HoYoApiResponse<DailyNoteGI>

    /** GI character list (POST with body) */
    @POST("game_record/genshin/api/character")
    suspend fun getCharacterListGI(
        @Body body: CharacterListRequest,
    ): HoYoApiResponse<HoYoLabCharacterListGI>

    /** GI Spiral Abyss (深渊螺旋) challenge record.
     * schedule_type: 1 = current period, 2 = previous period */
    @GET("game_record/genshin/api/spiralAbyss")
    suspend fun getSpiralAbyss(
        @Query("role_id") uid: String,
        @Query("server") server: String,
        @Query("schedule_type") scheduleType: Int = 1,
    ): HoYoApiResponse<SpiralAbyssResponse>

    // ========== Star Rail ==========

    /** HSR real-time notes */
    @GET("game_record/hkrpg/api/note")
    suspend fun getDailyNoteHSR(
        @Query("role_id") uid: String,
        @Query("server") server: String,
    ): HoYoApiResponse<DailyNoteHSR>

    /** HSR character list (POST with body) */
    @POST("game_record/hkrpg/api/role/basicInfo")
    suspend fun getCharacterListHSR(
        @Body body: CharacterListRequest,
    ): HoYoApiResponse<HoYoLabCharacterListHSR>

    /** HSR challenge record (Memory of Chaos / Pure Fiction).
     * schedule_type: 1 = Memory of Chaos (混沌回忆), 2 = Pure Fiction (虚构叙事).
     * server & role_id are passed as query params like other HSR endpoints. */
    @GET("game_record/hkrpg/api/challenge")
    suspend fun getChallengeHSR(
        @Query("role_id") uid: String,
        @Query("server") server: String,
        @Query("schedule_type") scheduleType: Int = 1,
        @Query("need_all") needAll: String = "true",
    ): HoYoApiResponse<ChallengeResponse>

    // ========== Zenless Zone Zero ==========

    /** ZZZ daily notes */
    @GET("game_record/nap/api/notes")
    suspend fun getDailyNoteZZZ(
        @Query("role_id") uid: String,
        @Query("server") server: String,
    ): HoYoApiResponse<DailyNoteZZZ>

    /** ZZZ character (agent) list - POST with filter body */
    @POST("game_record/nap/api/character")
    suspend fun getCharacterListZZZ(
        @Body body: CharacterListRequest,
    ): HoYoApiResponse<HoYoLabCharacterListZZZ>

    /** ZZZ gacha records (log) */
    @GET("game_record/nap/api/gacha")
    suspend fun getGachaLogZZZ(
        @Query("uid") uid: String,
        @Query("region") region: String,
        @Query("gacha_type") gachaType: String,
        @Query("page") page: String = "1",
        @Query("size") size: String = "20",
        @Query("end_id") endId: String = "0",
        @Query("lang") lang: String = "zh-cn",
    ): HoYoApiResponse<ZZZGachaResponse>

    // ========== HoYoLAB Auto Sign-in ==========

    /** Sign in (Miyoushe / CN) */
    @POST("community/community/user/wap/signPost")
    suspend fun signInMiyoushe(
        @Body body: SignInRequest,
    ): HoYoApiResponse<SignInResult>

    /** Sign in (HoyoLab / Global) */
    @POST("community/event/wapi/sign")
    suspend fun signInHoyoLab(
        @Body body: SignInRequest,
    ): HoYoApiResponse<SignInResult>

    /** Get sign-in info (Miyoushe / CN) */
    @GET("community/community/user/wap/signInfo")
    suspend fun getSignInfoMiyoushe(
        @Query("act_id") actId: String,
    ): HoYoApiResponse<SignInfo>

    /** Get sign-in info (HoyoLab / Global) */
    @GET("community/event/wapi/signInfo")
    suspend fun getSignInfoHoyoLab(
        @Query("act_id") actId: String,
    ): HoYoApiResponse<SignInfo>
}

/**
 * Enka.Network API service for character showcase data.
 */
interface EnkaApi {
    @GET("api/uid/{uid}")
    suspend fun getShowcase(
        @retrofit2.http.Path("uid") uid: String,
    ): EnkaResponse
}


