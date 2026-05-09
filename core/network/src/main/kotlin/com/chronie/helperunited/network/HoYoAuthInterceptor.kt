package com.chronie.helperunited.network

import com.chronie.helperunited.base.AccountRegion
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

/**
 * Interceptor for adding HoYoLab authentication headers to requests.
 */
class HoYoAuthInterceptor(
    private val cookie: String,
    private val region: AccountRegion,
    private val deviceId: String,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder()
            .header("Cookie", cookie)
            .header("X-Rpc-Device_id", deviceId)
            .header("X-Rpc-Client_type", "5")
            .header("X-Rpc-App_version", "2.106.0")
            .header("X-Rpc-Sys_version", "14")
            .header("X-Rpc-Channel", "miyoushe")
            .header("X-Rpc-Game_biz", region.game.hoyoBizId)
            .header("X-Rpc-Language", "zh-cn")

        return chain.proceed(requestBuilder.build())
    }
}
