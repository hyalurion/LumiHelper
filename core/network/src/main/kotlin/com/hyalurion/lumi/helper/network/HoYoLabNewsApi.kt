package com.hyalurion.lumi.helper.network

import com.hyalurion.lumi.helper.model.HoYoLabNewsResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * HoYoLab BBS News API for fetching game event announcements.
 * Uses the public news endpoint (no auth required for basic listing).
 */
interface HoYoLabNewsApi {

    /**
     * Fetch news/announcements list.
     * @param gids Game ID: 1=GI, 6=HSR, 8=ZZZ
     * @param type News type: 1=info, 2=activity, 3=announcement
     * @param pageSize Number of posts per page
     * @param page Page number (1-based)
     */
    @GET("community/post/wapi/getNewsList")
    suspend fun getNewsList(
        @Query("gids") gids: Int,
        @Query("type") type: Int = 1,
        @Query("page_size") pageSize: Int = 20,
        @Query("page") page: Int = 1,
    ): HoYoLabNewsResponse
}
