package com.hyalurion.lumi.helper.data

import com.hyalurion.lumi.helper.base.AccountRegion
import com.hyalurion.lumi.helper.base.RegionType
import com.hyalurion.lumi.helper.model.GachaEntry
import com.hyalurion.lumi.helper.model.GachaTypeZZZ
import com.hyalurion.lumi.helper.model.ZZZGachaEntry
import com.hyalurion.lumi.helper.network.HoYoApiFactory
import javax.inject.Singleton

/**
 * Repository for fetching ZZZ gacha records from HoYoLAB API.
 * Handles paginated fetching and conversion to unified GachaEntry format.
 * Ported from PizzaHelperUnited's gacha fetch logic.
 */
@Singleton
class ZZZGachaFetchRepository(
    private val apiFactory: HoYoApiFactory,
    private val gachaRepository: GachaRepository,
) {
    companion object {
        private const val PAGE_SIZE = 20
        private const val MAX_PAGES = 500
    }

    /**
     * Fetch all ZZZ gacha records for a given account.
     * Iterates through all gacha types and pages until no more records.
     */
    suspend fun fetchAllGachaRecords(
        accountUid: String,
        region: AccountRegion,
        cookie: String,
        gachaTypes: List<GachaTypeZZZ> = GachaTypeZZZ.entries,
    ): Result<Int> = runCatching {
        var totalNew = 0
        for (gachaType in gachaTypes) {
            val count = fetchGachaType(accountUid, region, cookie, gachaType)
            totalNew += count
        }
        totalNew
    }

    /**
     * Fetch gacha records for a specific ZZZ gacha type.
     */
    suspend fun fetchGachaType(
        accountUid: String,
        region: AccountRegion,
        cookie: String,
        gachaType: GachaTypeZZZ,
    ): Int {
        val api = apiFactory.createRecordApi(region, cookie)
        val server = uidToServer(region, accountUid)
        val lang = if (region.type == RegionType.MIYOUSHE) "zh-cn" else "en-us"

        var totalNew = 0
        var endId = "0"
        var page = 1

        while (page <= MAX_PAGES) {
            val response = api.getGachaLogZZZ(
                uid = accountUid,
                region = server,
                gachaType = gachaType.code,
                page = page.toString(),
                size = PAGE_SIZE.toString(),
                endId = endId,
                lang = lang,
            )

            if (response.retCode != 0) {
                throw HoYoApiException(response.retCode, response.message)
            }

            val data = response.data ?: break
            val entries = data.list

            if (entries.isEmpty()) break

            val gachaEntries = entries.map { it.toGachaEntry(accountUid) }

            gachaRepository.insertEntries(gachaEntries)
            totalNew += entries.size

            endId = entries.last().id

            if (entries.size < PAGE_SIZE) break

            page++
        }

        return totalNew
    }

    /**
     * Convert API entry to unified GachaEntry.
     */
    private fun ZZZGachaEntry.toGachaEntry(uid: String): GachaEntry {
        return GachaEntry(
            id = id,
            uid = uid,
            game = "zzz",
            gachaType = gachaType.toString(),
            itemId = itemId,
            count = count.toIntOrNull() ?: 1,
            time = time,
            name = name,
            lang = lang,
            itemType = itemType,
            rankType = rankType.toIntOrNull(),
            gachaId = gachaId,
        )
    }

    /**
     * Maps UID to region string for gacha API.
     */
    private fun uidToServer(region: AccountRegion, uid: String): String = when (region.type) {
        RegionType.MIYOUSHE -> "prod_gf_cn"
        RegionType.HOYOLAB -> when {
            uid.startsWith("6") -> "prod_gf_us"
            uid.startsWith("7") -> "prod_gf_eu"
            uid.startsWith("8") -> "prod_gf_jp"
            uid.startsWith("9") -> "prod_gf_sg"
            else -> "prod_gf_us"
        }
    }
}
