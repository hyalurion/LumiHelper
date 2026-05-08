package com.chronie.helperunited.network

import com.chronie.helperunited.model.DailyNoteGI
import com.chronie.helperunited.model.DailyNoteHSR
import com.chronie.helperunited.model.DailyNoteZZZ
import com.chronie.helperunited.model.EnkaResponse
import com.chronie.helperunited.model.HoYoApiResponse
import retrofit2.http.GET
import retrofit2.http.Header
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

    /** GI character showcase */
    @GET("game_record/genshin/api/character")
    suspend fun getCharacterListGI(
        @Query("role_id") uid: String,
        @Query("server") server: String,
    ): HoYoApiResponse<CharacterListDataGI>

    // ========== Star Rail ==========

    /** HSR real-time notes */
    @GET("game_record/hkrpg/api/note")
    suspend fun getDailyNoteHSR(
        @Query("role_id") uid: String,
        @Query("server") server: String,
    ): HoYoApiResponse<DailyNoteHSR>

    /** HSR character list */
    @GET("game_record/hkrpg/api/role/basicInfo")
    suspend fun getCharacterListHSR(
        @Query("role_id") uid: String,
        @Query("server") server: String,
    ): HoYoApiResponse<CharacterListDataHSR>

    // ========== Zenless Zone Zero ==========

    /** ZZZ daily notes */
    @GET("game_record/nap/api/notes")
    suspend fun getDailyNoteZZZ(
        @Query("role_id") uid: String,
        @Query("server") server: String,
    ): HoYoApiResponse<DailyNoteZZZ>
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

// ========== Response wrapper data classes ==========

data class CharacterListDataGI(
    val avatars: List<AvatarSummaryGI>,
)

data class AvatarSummaryGI(
    val id: Int,
    val name: String,
    val element: String,
    val level: Int,
    val fetter: Int,
    val rarity: Int,
    val image: String?,
    val activedConstellationNum: Int,
)

data class CharacterListDataHSR(
    val avatarList: List<AvatarSummaryHSR>,
)

data class AvatarSummaryHSR(
    val avatarId: Int,
    val level: Int,
    val name: String?,
    val element: String?,
    val rarity: Int,
)
