package com.hyalurion.lumi.helper.data

import com.hyalurion.lumi.helper.base.AccountRegion
import com.hyalurion.lumi.helper.base.RegionType
import com.hyalurion.lumi.helper.base.SupportedGame
import com.hyalurion.lumi.helper.model.CharacterListRequest
import com.hyalurion.lumi.helper.model.HoYoLabCharacterListZZZ
import com.hyalurion.lumi.helper.model.HoYoLabCharacterZZZ
import com.hyalurion.lumi.helper.model.HoYoLabDiscPropertyZZZ
import com.hyalurion.lumi.helper.model.HoYoLabDriveDiscZZZ
import com.hyalurion.lumi.helper.model.HoYoLabWEngineZZZ
import com.hyalurion.lumi.helper.model.HoYoAccount
import com.hyalurion.lumi.helper.model.ZZZAgentSummarized
import com.hyalurion.lumi.helper.model.ZZZDiscProperty
import com.hyalurion.lumi.helper.model.ZZZDriveDiscSummarized
import com.hyalurion.lumi.helper.model.ZZZElementType
import com.hyalurion.lumi.helper.model.ZZZProfileSummarized
import com.hyalurion.lumi.helper.model.ZZZWEngineSummarized
import com.hyalurion.lumi.helper.network.HoYoApiFactory
import com.hyalurion.lumi.helper.network.HoYoApiConfig
import javax.inject.Singleton

/**
 * Repository for ZZZ character showcase data.
 * Fetches agent list from HoYoLAB API and processes it for UI display.
 * Ported from PizzaHelperUnited's ZZZ showcase logic.
 */
@Singleton
class ZZZShowcaseRepository(
    private val apiFactory: HoYoApiFactory,
) {
    /**
     * Fetch ZZZ agent showcase for a given account.
     */
    suspend fun fetchShowcase(account: HoYoAccount): Result<ZZZProfileSummarized> = runCatching {
        val api = apiFactory.createRecordApi(account.region, account.cookie)
        val server = uidToServer(account.region, account.uid)
        val body = CharacterListRequest(roleId = account.uid, server = server)
        val response = api.getCharacterListZZZ(body)
        if (response.retCode != 0) throw HoYoApiException(response.retCode, response.message)
        val data = response.data ?: throw HoYoApiException(-1, "Empty data")
        processResponse(account.uid, data)
    }

    /**
     * Fetch ZZZ agent showcase for a given UID (without account, for public query).
     */
    suspend fun fetchShowcaseByUid(
        uid: String,
        region: AccountRegion,
        cookie: String,
    ): Result<ZZZProfileSummarized> = runCatching {
        val api = apiFactory.createRecordApi(region, cookie)
        val server = uidToServer(region, uid)
        val body = CharacterListRequest(roleId = uid, server = server)
        val response = api.getCharacterListZZZ(body)
        if (response.retCode != 0) throw HoYoApiException(response.retCode, response.message)
        val data = response.data ?: throw HoYoApiException(-1, "Empty data")
        processResponse(uid, data)
    }

    /**
     * Process raw API response into summarized profile.
     */
    private fun processResponse(
        uid: String,
        data: HoYoLabCharacterListZZZ,
    ): ZZZProfileSummarized {
        val agents = data.avatarList.map { agent -> summarizeAgent(agent) }

        return ZZZProfileSummarized(
            uid = uid,
            nickname = data.nickname ?: "",
            level = data.playerLevel ?: 0,
            headIconUrl = data.curHeadIconUrl,
            agentList = agents,
        )
    }

    /**
     * Convert a single HoYoLab ZZZ agent to summarized form.
     */
    private fun summarizeAgent(agent: HoYoLabCharacterZZZ): ZZZAgentSummarized {
        val wEngineRaw = agent.wEngine ?: agent.weapon
        val wEngine = wEngineRaw?.let { summarizeWEngine(it) }

        val driveDiscs = agent.equipList.map { summarizeDriveDisc(it) }

        return ZZZAgentSummarized(
            id = agent.id,
            name = agent.nameMi18n ?: "Agent #${agent.id}",
            fullName = agent.fullNameMi18n,
            level = agent.level,
            rarity = agent.starLevel,
            elementType = ZZZElementType.fromCode(agent.elementType),
            rank = agent.rank,
            iconUrl = agent.roleSquareUrl,
            verticalPaintingUrl = agent.roleVerticalPaintingUrl,
            wEngine = wEngine,
            driveDiscs = driveDiscs,
            skills = null,
            properties = emptyList(),
        )
    }

    /**
     * Convert W-Engine to summarized form.
     */
    private fun summarizeWEngine(engine: HoYoLabWEngineZZZ): ZZZWEngineSummarized {
        return ZZZWEngineSummarized(
            id = engine.id,
            name = engine.name ?: "W-Engine #${engine.id}",
            level = engine.level,
            rarity = engine.starLevel,
            enhancementLevel = engine.upgradeLevel,
            iconUrl = engine.icon ?: engine.equipIcon ?: engine.gachaCardUrl,
        )
    }

    /**
     * Convert Drive Disc to summarized form.
     */
    private fun summarizeDriveDisc(disc: HoYoLabDriveDiscZZZ): ZZZDriveDiscSummarized {
        return ZZZDriveDiscSummarized(
            id = disc.id,
            name = disc.name ?: "Drive Disc #${disc.id}",
            level = disc.level,
            rarity = disc.starLevel,
            suitId = disc.suitId,
            suitName = disc.suitName,
            slot = disc.equipmentType,
            iconUrl = disc.icon,
            mainStat = disc.mainProperty?.toDiscProperty(),
            subStats = disc.subProperties.map { it.toDiscProperty() },
        )
    }

    private fun HoYoLabDiscPropertyZZZ.toDiscProperty() = ZZZDiscProperty(
        propertyName = this.propertyName,
        propertyId = this.propertyId,
        value = this.value,
        times = this.times,
    )

    /**
     * Maps UID to the server string that the API expects.
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
