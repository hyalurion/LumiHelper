package com.hyalurion.lumi.helper.data

import com.hyalurion.lumi.helper.base.AccountRegion
import com.hyalurion.lumi.helper.base.RegionType
import com.hyalurion.lumi.helper.base.SupportedGame
import com.hyalurion.lumi.helper.model.ChallengeResponse
import com.hyalurion.lumi.helper.model.HoYoAccount
import com.hyalurion.lumi.helper.model.HsrChallengeType
import com.hyalurion.lumi.helper.model.LedgerData4GI
import com.hyalurion.lumi.helper.model.LedgerData4HSR
import com.hyalurion.lumi.helper.model.SpiralAbyssResponse
import com.hyalurion.lumi.helper.network.HoYoApiFactory
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for fetching challenge record data (Spiral Abyss, Memory of Chaos, Pure Fiction).
 * Ported from PizzaHelperUnited's challenge data fetching logic.
 */
@Singleton
class ChallengeRepository @Inject constructor(
    private val apiFactory: HoYoApiFactory,
) {
    /**
     * Maps UID to the server string that the API expects.
     * Reuses the same logic as DailyNoteRepository.
     */
    private fun uidToServer(region: AccountRegion, uid: String): String = when (region.type) {
        RegionType.MIYOUSHE -> when (region.game) {
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
            else -> throw IllegalArgumentException("Unsupported game: ${region.game.rawValue}")
        }
        RegionType.HOYOLAB -> when (region.game) {
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
            else -> throw IllegalArgumentException("Unsupported game: ${region.game.rawValue}")
        }
    }

    /**
     * Fetch GI Spiral Abyss data.
     * @param scheduleType 1 = current period, 2 = previous period
     */
    suspend fun fetchSpiralAbyss(
        account: HoYoAccount,
        scheduleType: Int = 1,
    ): Result<SpiralAbyssResponse> = runCatching {
        val api = apiFactory.createRecordApi(account.region, account.cookie)
        val server = uidToServer(account.region, account.uid)
        val response = api.getSpiralAbyss(account.uid, server, scheduleType)
        if (response.retCode != 0) throw HoYoApiException(response.retCode, response.message)
        response.data ?: throw HoYoApiException(-1, "Empty data")
    }

    /**
     * Fetch HSR challenge data (Memory of Chaos or Pure Fiction).
     * @param challengeType The type of challenge to fetch
     */
    suspend fun fetchChallengeHSR(
        account: HoYoAccount,
        challengeType: HsrChallengeType = HsrChallengeType.MEMORY_OF_CHAOS,
    ): Result<ChallengeResponse> = runCatching {
        val api = apiFactory.createRecordApi(account.region, account.cookie)
        val server = uidToServer(account.region, account.uid)
        val response = api.getChallengeHSR(
            uid = account.uid,
            server = server,
            scheduleType = challengeType.scheduleType,
        )
        if (response.retCode != 0) throw HoYoApiException(response.retCode, response.message)
        response.data ?: throw HoYoApiException(-1, "Empty data")
    }

    // ========== Ledger (Monthly Income) ==========

    suspend fun fetchLedgerGI(account: HoYoAccount): Result<LedgerData4GI> = runCatching {
        val month = Calendar.getInstance().get(Calendar.MONTH) + 1
        try {
            fetchLedgerGI(account, month)
        } catch (e: Exception) {
            when {
                e is HoYoApiException && e.code == -2 -> {
                    val prevMonth = if (month == 1) 12 else month - 1
                    fetchLedgerGI(account, prevMonth)
                }
                else -> throw e
            }
        }
    }

    private suspend fun fetchLedgerGI(account: HoYoAccount, month: Int): LedgerData4GI {
        val api = apiFactory.createLedgerApi(account.region, account.cookie)
        val server = uidToServer(account.region, account.uid)
        val monthStr = month.toString()
        val response = when (account.region.type) {
            RegionType.MIYOUSHE -> api.getLedgerGI_Miyoushe(
                lang = "zh-cn",
                server = server,
                uid = account.uid,
                month = monthStr,
            )
            RegionType.HOYOLAB -> api.getLedgerGI_HoyoLab(
                server = server,
                uid = account.uid,
                month = monthStr,
            )
        }
        if (response.retCode != 0) throw HoYoApiException(response.retCode, response.message)
        return response.data ?: throw HoYoApiException(-1, "Empty data")
    }

    suspend fun fetchLedgerHSR(account: HoYoAccount): Result<LedgerData4HSR> = runCatching {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR).toString()
        var month = (calendar.get(Calendar.MONTH) + 1).toString()
        if (month.length == 1) month = "0$month"
        val monthStr = year + month

        val api = apiFactory.createLedgerApi(account.region, account.cookie)
        val server = uidToServer(account.region, account.uid)
        val response = api.getLedgerHSR(
            server = server,
            uid = account.uid,
            month = monthStr,
        )
        if (response.retCode != 0) throw HoYoApiException(response.retCode, response.message)
        response.data ?: throw HoYoApiException(-1, "Empty data")
    }
}
