package com.hyalurion.lumi.helper.network

import com.hyalurion.lumi.helper.model.HoYoApiResponse
import com.hyalurion.lumi.helper.model.LedgerData4GI
import com.hyalurion.lumi.helper.model.LedgerData4HSR
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * HoYoLab Ledger API service (monthly income summary).
 * Uses a separate API host from the record API.
 */
interface HoYoLedgerApi {

    @GET("event/ys_ledger/monthInfo")
    suspend fun getLedgerGI_Miyoushe(
        @Query("lang") lang: String,
        @Query("region") server: String,
        @Query("uid") uid: String,
        @Query("month") month: String,
    ): HoYoApiResponse<LedgerData4GI>

    @GET("event/ysledgeros/month_info")
    suspend fun getLedgerGI_HoyoLab(
        @Query("lang") lang: String = "zh-cn",
        @Query("region") server: String,
        @Query("uid") uid: String,
        @Query("month") month: String,
    ): HoYoApiResponse<LedgerData4GI>

    @GET("event/srledger/month_info")
    suspend fun getLedgerHSR(
        @Query("lang") lang: String = "zh-cn",
        @Query("region") server: String,
        @Query("uid") uid: String,
        @Query("month") month: String,
    ): HoYoApiResponse<LedgerData4HSR>
}