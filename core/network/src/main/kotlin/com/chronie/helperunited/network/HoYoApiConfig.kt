package com.chronie.helperunited.network

import com.chronie.helperunited.base.AccountRegion
import com.chronie.helperunited.base.SupportedGame
import okhttp3.Interceptor
import okhttp3.Response
import okio.ByteString.Companion.toByteString
import java.security.MessageDigest

/**
 * HoYoLab API URL configuration and request signing.
 * Ported from PizzaHelperUnited's URLRequestConfig.
 */
object HoYoApiConfig {

    // Region-specific API hosts

    fun recordApiHost(region: AccountRegion): String = when (region) {
        is AccountRegion.Miyoushe -> "api-takumi-record.mihoyo.com"
        is AccountRegion.HoyoLab -> "bbs-api-os.hoyolab.com"
    }

    fun accountApiHost(region: AccountRegion): String = when (region) {
        is AccountRegion.Miyoushe -> "api-takumi.mihoyo.com"
        is AccountRegion.HoyoLab -> "api-account-os.hoyolab.com"
    }

    fun ledgerApiHost(region: AccountRegion): String = when (region) {
        is AccountRegion.Miyoushe -> when (region.game) {
            SupportedGame.GENSHIN_IMPACT -> "hk4e-api.mihoyo.com"
            SupportedGame.STAR_RAIL -> "api-takumi.mihoyo.com"
            SupportedGame.ZENLESS_ZONE -> ""
        }
        is AccountRegion.HoyoLab -> when (region.game) {
            SupportedGame.GENSHIN_IMPACT -> "sg-hk4e-api.hoyolab.com"
            SupportedGame.STAR_RAIL -> "sg-public-api.hoyolab.com"
            SupportedGame.ZENLESS_ZONE -> ""
        }
    }

    fun publicOpsDomain(region: AccountRegion): String = when (region) {
        is AccountRegion.Miyoushe -> when (region.game) {
            SupportedGame.GENSHIN_IMPACT -> "public-operation-hk4e.mihoyo.com"
            SupportedGame.STAR_RAIL -> "public-operation-hkrpg.mihoyo.com"
            SupportedGame.ZENLESS_ZONE -> "public-operation-nap.mihoyo.com"
        }
        is AccountRegion.HoyoLab -> when (region.game) {
            SupportedGame.GENSHIN_IMPACT -> "public-operation-hk4e-sg.hoyoverse.com"
            SupportedGame.STAR_RAIL -> "public-operation-hkrpg-sg.hoyoverse.com"
            SupportedGame.ZENLESS_ZONE -> "public-operation-nap-sg.hoyoverse.com"
        }
    }

    // Salts for DS signature
    private fun salt(region: AccountRegion): String = when (region) {
        is AccountRegion.Miyoushe -> "xV8v4Qu54lUKrEYFZkJhB8cuOh9Asafs"  // X4
        is AccountRegion.HoyoLab -> "okr4obncj8bw5a65hbnn5oo6ixjc3l9w"   // OSX6
    }

    // App version
    fun xRpcAppVersion(region: AccountRegion): String = when (region) {
        is AccountRegion.Miyoushe -> "2.40.1"
        is AccountRegion.HoyoLab -> "2.55.0"
    }

    // Client type
    fun xRpcClientType(region: AccountRegion): String = when (region) {
        is AccountRegion.Miyoushe -> "5"
        is AccountRegion.HoyoLab -> "2"
    }

    // Headers
    fun referer(region: AccountRegion): String = when (region) {
        is AccountRegion.Miyoushe -> "https://webstatic.mihoyo.com"
        is AccountRegion.HoyoLab -> "https://act.hoyolab.com"
    }

    fun origin(region: AccountRegion): String = referer(region)

    fun xRequestedWith(region: AccountRegion): String = when (region) {
        is AccountRegion.Miyoushe -> "com.mihoyo.hyperion"
        is AccountRegion.HoyoLab -> "com.mihoyo.hoyolab"
    }

    fun userAgent(region: AccountRegion): String =
        "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Chrome/120.0.0.0 Mobile Safari/537.36 miHoYoBBS/${xRpcAppVersion(region)}"

    // DS Signature generation (DS2)
    fun generateDsSignature(region: AccountRegion, query: String = "", body: String = ""): String {
        val salt = salt(region)
        val timestamp = (System.currentTimeMillis() / 1000).toString()
        val random = (100000 + (Math.random() * 900000).toInt()).toString()

        val check = "salt=$salt&t=$timestamp&r=$random&b=$body&q=$query"
        val md5 = MessageDigest.getInstance("MD5")
        val hash = md5.digest(check.toByteArray()).toByteString().hex()

        return "$timestamp,$random,$hash"
    }
}

/**
 * OkHttp interceptor that adds HoYoLab authentication headers.
 */
class HoYoAuthInterceptor(
    private val cookie: String,
    private val region: AccountRegion,
    private val deviceId: String = "",
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val query = original.url.query.orEmpty()
        val ds = HoYoApiConfig.generateDsSignature(region, query)

        val request = original.newBuilder()
            .header("Cookie", cookie)
            .header("DS", ds)
            .header("x-rpc-app_version", HoYoApiConfig.xRpcAppVersion(region))
            .header("x-rpc-client_type", HoYoApiConfig.xRpcClientType(region))
            .header("User-Agent", HoYoApiConfig.userAgent(region))
            .header("Referer", HoYoApiConfig.referer(region))
            .header("Origin", HoYoApiConfig.origin(region))
            .apply {
                if (deviceId.isNotBlank()) {
                    header("x-rpc-device_id", deviceId)
                }
            }
            .build()

        return chain.proceed(request)
    }
}
