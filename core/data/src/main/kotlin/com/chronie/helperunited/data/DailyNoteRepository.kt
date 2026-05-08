package com.chronie.helperunited.data

import com.chronie.helperunited.base.AccountRegion
import com.chronie.helperunited.base.SupportedGame
import com.chronie.helperunited.model.DailyNoteGI
import com.chronie.helperunited.model.DailyNoteHSR
import com.chronie.helperunited.model.DailyNoteZZZ
import com.chronie.helperunited.network.HoYoApiFactory
import com.chronie.helperunited.network.HoYoApiConfig
import com.squareup.moshi.Moshi
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for fetching daily notes (resin, stamina, energy).
 * Ported from PizzaHelperUnited's DailyNote fetch logic.
 */
@Singleton
class DailyNoteRepository @Inject constructor(
    private val apiFactory: HoYoApiFactory,
    private val dailyNoteCacheDao: DailyNoteCacheDao,
    private val moshi: Moshi,
) {
    /**
     * Maps UID to the server string that the API expects.
     */
    private fun uidToServer(region: AccountRegion, uid: String): String = when (region) {
        is AccountRegion.Miyoushe -> when (region.game) {
            SupportedGame.GENSHIN_IMPACT -> when {
                uid.startsWith("1") -> "cn_gf01"
                uid.startsWith("2") -> "cn_gf01"
                uid.startsWith("5") -> "cn_qd01"
                else -> "cn_gf01"
            }
            SupportedGame.STAR_RAIL -> when {
                uid.startsWith("1") -> "prod_gf_cn"
                uid.startsWith("2") -> "prod_gf_cn"
                else -> "prod_gf_cn"
            }
            SupportedGame.ZENLESS_ZONE -> "prod_gf_cn"
        }
        is AccountRegion.HoyoLab -> when (region.game) {
            SupportedGame.GENSHIN_IMPACT -> when {
                uid.startsWith("6") -> "os_usa"
                uid.startsWith("7") -> "os_euro"
                uid.startsWith("8") -> "os_asia"
                uid.startsWith("9") -> "os_cht"
                else -> "os_usa"
            }
            SupportedGame.STAR_RAIL -> when {
                uid.startsWith("6") -> "prod_official_usa"
                uid.startsWith("7") -> "prod_official_eur"
                uid.startsWith("8") -> "prod_official_asia"
                uid.startsWith("9") -> "prod_official_cht"
                else -> "prod_official_usa"
            }
            SupportedGame.ZENLESS_ZONE -> when {
                uid.startsWith("6") -> "prod_gf_us"
                uid.startsWith("7") -> "prod_gf_eu"
                uid.startsWith("8") -> "prod_gf_jp"
                uid.startsWith("9") -> "prod_gf_sg"
                else -> "prod_gf_us"
            }
        }
    }

    suspend fun fetchDailyNoteGI(account: com.chronie.helperunited.model.HoYoAccount): Result<DailyNoteGI> =
        runCatching {
            val api = apiFactory.createRecordApi(account.region, account.cookie)
            val server = uidToServer(account.region, account.uid)
            val response = api.getDailyNoteGI(account.uid, server)
            if (response.retCode != 0) throw HoYoApiException(response.retCode, response.message)
            response.data ?: throw HoYoApiException(-1, "Empty data")
        }

    suspend fun fetchDailyNoteHSR(account: com.chronie.helperunited.model.HoYoAccount): Result<DailyNoteHSR> =
        runCatching {
            val api = apiFactory.createRecordApi(account.region, account.cookie)
            val server = uidToServer(account.region, account.uid)
            val response = api.getDailyNoteHSR(account.uid, server)
            if (response.retCode != 0) throw HoYoApiException(response.retCode, response.message)
            response.data ?: throw HoYoApiException(-1, "Empty data")
        }

    suspend fun fetchDailyNoteZZZ(account: com.chronie.helperunited.model.HoYoAccount): Result<DailyNoteZZZ> =
        runCatching {
            val api = apiFactory.createRecordApi(account.region, account.cookie)
            val server = uidToServer(account.region, account.uid)
            val response = api.getDailyNoteZZZ(account.uid, server)
            if (response.retCode != 0) throw HoYoApiException(response.retCode, response.message)
            response.data ?: throw HoYoApiException(-1, "Empty data")
        }
}

class HoYoApiException(val code: Int, override val message: String) : Exception(message)
