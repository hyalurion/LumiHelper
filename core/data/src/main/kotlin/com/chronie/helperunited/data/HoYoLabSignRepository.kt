package com.chronie.helperunited.data

import com.chronie.helperunited.base.AccountRegion
import com.chronie.helperunited.base.RegionType
import com.chronie.helperunited.base.SupportedGame
import com.chronie.helperunited.model.GameSignStatus
import com.chronie.helperunited.model.HoYoAccount
import com.chronie.helperunited.model.SignInRequest
import com.chronie.helperunited.model.SignInResult
import com.chronie.helperunited.model.SignInfo
import com.chronie.helperunited.network.HoYoApiFactory
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject
import javax.inject.Singleton

/**
 * HoYoLAB/Miyoushe auto sign-in repository.
 * Handles daily sign-in for GI, HSR, and ZZZ.
 * Ported from PizzaHelperUnited's sign-in logic.
 */
@Singleton
class HoYoLabSignRepository @Inject constructor(
    private val apiFactory: HoYoApiFactory,
) {
    companion object {
        private val ACT_IDS = mapOf(
            SupportedGame.GENSHIN_IMPACT to "e202102251931481",
            SupportedGame.STAR_RAIL to "e202303301540311",
            SupportedGame.ZENLESS_ZONE to "e202406192138391",
        )
    }

    /**
     * Get sign-in status for a single account.
     */
    suspend fun getSignStatus(account: HoYoAccount): Result<SignInfo> = runCatching {
        val api = apiFactory.createRecordApi(account.region, account.cookie)
        val actId = ACT_IDS[account.game] ?: throw IllegalArgumentException("Unsupported game")

        val response = when (account.region.type) {
            RegionType.MIYOUSHE -> api.getSignInfoMiyoushe(actId)
            RegionType.HOYOLAB -> api.getSignInfoHoyoLab(actId)
        }

        if (response.retCode != 0) throw HoYoApiException(response.retCode, response.message)
        response.data ?: throw HoYoApiException(-1, "Empty sign info")
    }

    /**
     * Sign in for a single account.
     */
    suspend fun signIn(account: HoYoAccount): Result<SignInResult> = runCatching {
        val api = apiFactory.createRecordApi(account.region, account.cookie)
        val actId = ACT_IDS[account.game] ?: throw IllegalArgumentException("Unsupported game")

        val body = SignInRequest(actId = actId)

        val response = when (account.region.type) {
            RegionType.MIYOUSHE -> api.signInMiyoushe(body)
            RegionType.HOYOLAB -> api.signInHoyoLab(body)
        }

        if (response.retCode != 0) throw HoYoApiException(response.retCode, response.message)
        response.data ?: throw HoYoApiException(-1, "Empty sign result")
    }

    /**
     * Sign in for all accounts and return per-game status.
     */
    suspend fun signInAll(
        accounts: List<HoYoAccount>,
    ): List<GameSignStatus> = coroutineScope {
        accounts.map { account ->
            async {
                try {
                    val signInResult = signIn(account)
                    val signInfo = getSignStatus(account)

                    GameSignStatus(
                        game = account.game,
                        isSigned = signInfo.getOrNull()?.isSign == true,
                        totalSignDays = signInfo.getOrNull()?.totalSignDay ?: 0,
                    )
                } catch (e: Exception) {
                    GameSignStatus(
                        game = account.game,
                        isSigned = false,
                        totalSignDays = 0,
                        error = e.message ?: "签到失败",
                    )
                }
            }
        }.awaitAll()
    }

    /**
     * Get sign-in status for all accounts without signing in.
     */
    suspend fun getAllSignStatuses(
        accounts: List<HoYoAccount>,
    ): List<GameSignStatus> = coroutineScope {
        accounts.map { account ->
            async {
                try {
                    val signInfo = getSignStatus(account)
                    GameSignStatus(
                        game = account.game,
                        isSigned = signInfo.getOrNull()?.isSign == true,
                        totalSignDays = signInfo.getOrNull()?.totalSignDay ?: 0,
                    )
                } catch (e: Exception) {
                    GameSignStatus(
                        game = account.game,
                        isSigned = false,
                        totalSignDays = 0,
                        error = e.message ?: "获取签到状态失败",
                    )
                }
            }
        }.awaitAll()
    }
}
