package com.chronie.helperunited.network

import com.chronie.helperunited.model.CharacterListRequest
import com.chronie.helperunited.model.DailyNoteGI
import com.chronie.helperunited.model.DailyNoteHSR
import com.chronie.helperunited.model.DailyNoteZZZ
import com.chronie.helperunited.model.EnkaResponse
import com.chronie.helperunited.model.HoYoApiResponse
import com.chronie.helperunited.model.HoYoLabCharacterListGI
import com.chronie.helperunited.model.HoYoLabCharacterListHSR
import com.chronie.helperunited.model.HoYoLabCharacterListZZZ
import com.chronie.helperunited.model.ZZZGachaResponse
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


