package com.chronie.helperunited.util

import android.content.Context
import android.os.Build
import android.provider.Settings
import com.chronie.helperunited.base.AccountRegion
import com.chronie.helperunited.base.RegionType
import com.chronie.helperunited.network.DeviceFpResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.math.BigInteger
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

/**
 * Device fingerprint generator for HoYo API (x-rpc-device_fp).
 *
 * Replicates the Swift version's logic:
 *   https://github.com/pizza-studio/PizzaHelperUnited
 *   PZAccountKit/GenerateDeviceFingerPrintAPI/GetDeviceFingerPrint.swift
 *
 * The MiHoYo API expects a device_fp value computed from a combination of
 * device identification, timestamp-based seed, and platform-specific ext_fields.
 */
object DeviceFingerprintUtil {

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    data class FingerPrintResult(
        val deviceFP: String,
        val seedID: String,
        val seedTime: String,
    )

    private var cachedDeviceID: String? = null

    /**
     * Get a persistent device ID for fingerprinting.
     * Uses Android ID if available, otherwise generates a UUID-style random ID.
     */
    fun getDeviceID(context: Context): String {
        if (cachedDeviceID != null) return cachedDeviceID!!

        val androidId = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        ) ?: ""

        val fallbackId = context.getSharedPreferences(
            "device_fp_prefs", Context.MODE_PRIVATE
        ).getString("device_id", null)

        cachedDeviceID = if (androidId.isNotBlank() && androidId != "9774d56d682e549c") {
            androidId
        } else if (!fallbackId.isNullOrBlank()) {
            fallbackId
        } else {
            val newId = java.util.UUID.randomUUID().toString().replace("-", "")
            context.getSharedPreferences("device_fp_prefs", Context.MODE_PRIVATE)
                .edit().putString("device_id", newId).apply()
            newId
        }
        return cachedDeviceID!!
    }

    /**
     * Generate a device fingerprint by calling MiHoYo's public-data API.
     *
     * @param region Account region (MIYOUSHE or HOYOLAB)
     * @param context Android context
     * @param deviceID Optional device ID; falls back to getDeviceID(context)
     * @return FingerPrintResult with deviceFP, seedID, seedTime
     */
    suspend fun generateDeviceFingerPrint(
        region: AccountRegion,
        context: Context,
        deviceID: String? = null,
    ): FingerPrintResult = withContext(Dispatchers.IO) {
        val id = deviceID ?: getDeviceID(context)
        val deviceId = "$id${System.currentTimeMillis() / 1000}".md5()
        val seedID = generateSeed()
        val epochSeconds = System.currentTimeMillis() / 1000
        val seedTime = "${epochSeconds}000"
        val initialRandomFp = String(deviceId.md5().toCharArray().apply { shuffle() }.take(13).toCharArray())

        val publicDataHost = when (region.type) {
            RegionType.MIYOUSHE -> "https://public-data-api.mihoyo.com"
            RegionType.HOYOLAB -> "https://sg-public-data-api.hoyoverse.com"
        }

        val platformID = when (region.type) {
            RegionType.MIYOUSHE -> "1"
            RegionType.HOYOLAB -> "5"
        }

        val extFields = buildExtFields(region, deviceId)

        val requestBody = mapOf(
            "seed_id" to seedID,
            "device_id" to deviceId,
            "platform" to platformID,
            "seed_time" to seedTime,
            "ext_fields" to extFields,
            "app_name" to region.rawValue,
            "device_fp" to initialRandomFp,
        )

        val jsonBody = moshi.adapter(Map::class.java).toJson(requestBody)

        val request = Request.Builder()
            .url("$publicDataHost/device-fp/api/getFp")
            .post(jsonBody.toRequestBody("application/json".toMediaType()))
            .header("Content-Type", "application/json")
            .build()

        val response = client.newCall(request).execute()
        val bodyString = response.body?.string() ?: throw Exception("Empty response body")

        val adapter = moshi.adapter(DeviceFpResponse::class.java)
        val result = adapter.fromJson(bodyString)
            ?: throw Exception("Failed to parse response: $bodyString")

        if (result.retCode != 0) {
            throw Exception("DeviceFP API error: ${result.message}")
        }

        val fp = result.data?.deviceFp ?: throw Exception("No device_fp in response")

        FingerPrintResult(
            deviceFP = fp,
            seedID = seedID,
            seedTime = seedTime,
        )
    }

    // ============================================================
    // Private helpers
    // ============================================================

    private fun generateSeed(length: Int = 16): String {
        val characters = "0123456789abcdef"
        return (1..length).map { characters.random() }.joinToString("")
    }

    private fun generateRandom64(): String {
        val chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..64).map { chars.random() }.joinToString("")
    }

    private fun buildExtFields(region: AccountRegion, deviceID: String): String {
        return when (region.type) {
            RegionType.MIYOUSHE -> {
                val modelName = Build.MODEL.replace("\"", "\\\"")
                val osVersion = Build.VERSION.RELEASE
                val androidId = deviceID.uppercase()

                buildString {
                    append("{")
                    append("\"ramCapacity\":\"${getRamCapacity()}\",")
                    append("\"hasVpn\":\"0\",")
                    append("\"proxyStatus\":\"0\",")
                    append("\"screenBrightness\":\"0.550\",")
                    append("\"packageName\":\"com.miHoYo.mhybbs\",")
                    append("\"romRemain\":\"100513\",")
                    append("\"deviceName\":\"$modelName\",")
                    append("\"isJailBreak\":\"0\",")
                    append("\"magnetometer\":\"-160.495300x-206.488358x58.534348\",")
                    append("\"buildTime\":\"1706406805675\",")
                    append("\"ramRemain\":\"97\",")
                    append("\"accelerometer\":\"-0.419876x-0.748367x-0.508057\",")
                    append("\"cpuCores\":\"${Runtime.getRuntime().availableProcessors()}\",")
                    append("\"cpuType\":\"CPU_TYPE_ARM64\",")
                    append("\"packageVersion\":\"2.20.1\",")
                    append("\"gyroscope\":\"0.133974x-0.051780x-0.062961\",")
                    append("\"batteryStatus\":\"45\",")
                    append("\"appUpdateTimeDiff\":\"1707130080397\",")
                    append("\"appMemory\":\"57\",")
                    append("\"screenSize\":\"414×896\",")
                    append("\"vendor\":\"--\",")
                    append("\"model\":\"$modelName\",")
                    append("\"IDFV\":\"$androidId\",")
                    append("\"romCapacity\":\"488153\",")
                    append("\"isPushEnabled\":\"1\",")
                    append("\"appInstallTimeDiff\":\"1696756955347\",")
                    append("\"osVersion\":\"$osVersion\",")
                    append("\"chargeStatus\":\"1\",")
                    append("\"isSimInserted\":\"1\",")
                    append("\"networkType\":\"WIFI\"")
                    append("}")
                }
            }
            RegionType.HOYOLAB -> {
                val canvas = generateRandom64()
                val webgl = generateRandom64()
                buildString {
                    append("{")
                    append("\"userAgent\":\"Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36\",")
                    append("\"browserScreenSize\":\"387904\",")
                    append("\"maxTouchPoints\":\"5\",")
                    append("\"isTouchSupported\":\"1\",")
                    append("\"browserLanguage\":\"zh-CN\",")
                    append("\"browserPlat\":\"Linux aarch64\",")
                    append("\"browserTimeZone\":\"Asia/Shanghai\",")
                    append("\"webGlRender\":\"Adreno (TM) 640\",")
                    append("\"webGlVendor\":\"Qualcomm\",")
                    append("\"numOfPlugins\":\"0\",")
                    append("\"listOfPlugins\":\"unknown\",")
                    append("\"screenRatio\":\"2.625\",")
                    append("\"deviceMemory\":\"4\",")
                    append("\"hardwareConcurrency\":\"8\",")
                    append("\"cpuClass\":\"unknown\",")
                    append("\"ifNotTrack\":\"unknown\",")
                    append("\"ifAdBlock\":\"0\",")
                    append("\"hasLiedLanguage\":\"0\",")
                    append("\"hasLiedResolution\":\"1\",")
                    append("\"hasLiedOs\":\"0\",")
                    append("\"hasLiedBrowser\":\"0\",")
                    append("\"canvas\":\"$canvas\",")
                    append("\"webDriver\":\"0\",")
                    append("\"colorDepth\":\"24\",")
                    append("\"pixelRatio\":\"2.625\",")
                    append("\"packageName\":\"unknown\",")
                    append("\"packageVersion\":\"2.27.0\",")
                    append("\"webgl\":\"$webgl\"")
                    append("}")
                }
            }
        }
    }

    private fun getRamCapacity(): String {
        return try {
            val memInfo = Runtime.getRuntime().maxMemory() / (1024 * 1024)
            (memInfo * 1024).toString()
        } catch (_: Exception) {
            "3746"
        }
    }

    private fun String.md5(): String {
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(toByteArray())
        return BigInteger(1, digest).toString(16).padStart(32, '0')
    }
}