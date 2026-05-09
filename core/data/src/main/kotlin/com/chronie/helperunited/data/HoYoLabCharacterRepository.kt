package com.chronie.helperunited.data

import com.chronie.helperunited.base.RegionType
import com.chronie.helperunited.base.SupportedGame
import com.chronie.helperunited.model.CharacterListRequest
import com.chronie.helperunited.model.HoYoAccount
import com.chronie.helperunited.model.HoYoLabCharacterDisplay
import com.chronie.helperunited.model.HoYoLabCharacterGI
import com.chronie.helperunited.model.HoYoLabCharacterHSR
import com.chronie.helperunited.network.HoYoApiFactory
import javax.inject.Singleton

/**
 * Repository for fetching and caching HoYoLAB character lists.
 * Ported from PizzaHelperUnited's PZHoYoLabKit character fetch logic.
 */
@Singleton
class HoYoLabCharacterRepository(
    private val apiFactory: HoYoApiFactory,
    private val characterDao: HoYoLabCharacterDao,
) {

    /**
     * Fetch the character list for a GI account from HoYoLAB API and cache it.
     */
    suspend fun fetchCharactersGI(account: HoYoAccount): Result<List<HoYoLabCharacterDisplay>> =
        runCatching {
            val api = apiFactory.createRecordApi(account.region, account.cookie)
            val server = uidToServer(account, account.uid)
            val response = api.getCharacterListGI(
                CharacterListRequest(roleId = account.uid, server = server)
            )
            if (response.retCode != 0) throw HoYoApiException(response.retCode, response.message)
            val data = response.data ?: throw HoYoApiException(-1, "Empty data")

            val displayList = data.avatars.map { it.toDisplay("GI") }

            characterDao.deleteByAccount(account.uid, SupportedGame.GENSHIN_IMPACT.rawValue)
            characterDao.upsertAll(data.avatars.map { it.toEntity(account.uid, SupportedGame.GENSHIN_IMPACT.rawValue) })

            displayList
        }

    /**
     * Fetch the character list for an HSR account from HoYoLAB API and cache it.
     */
    suspend fun fetchCharactersHSR(account: HoYoAccount): Result<List<HoYoLabCharacterDisplay>> =
        runCatching {
            val api = apiFactory.createRecordApi(account.region, account.cookie)
            val server = uidToServer(account, account.uid)
            val response = api.getCharacterListHSR(
                CharacterListRequest(roleId = account.uid, server = server)
            )
            if (response.retCode != 0) throw HoYoApiException(response.retCode, response.message)
            val data = response.data ?: throw HoYoApiException(-1, "Empty data")

            val displayList = data.avatarList.map { it.toDisplay("HSR") }

            characterDao.deleteByAccount(account.uid, SupportedGame.STAR_RAIL.rawValue)
            characterDao.upsertAll(data.avatarList.map { it.toEntity(account.uid, SupportedGame.STAR_RAIL.rawValue) })

            displayList
        }

    /**
     * Get cached characters for an account (observes changes).
     */
    fun observeCachedCharacters(accountUid: String, game: SupportedGame) =
        characterDao.observeByAccount(accountUid, game.rawValue)

    /**
     * Get cached characters for an account (one-shot).
     */
    suspend fun getCachedCharacters(accountUid: String, game: SupportedGame): List<HoYoLabCharacterDisplay> {
        return characterDao.getByAccount(accountUid, game.rawValue).map { it.toDisplay() }
    }

    /**
     * Filter and sort cached characters.
     */
    suspend fun getFilteredCharacters(
        accountUid: String,
        game: SupportedGame,
        element: String? = null,
        rarity: Int? = null,
        sortBy: SortOption = SortOption.LEVEL,
    ): List<HoYoLabCharacterDisplay> {
        var list = getCachedCharacters(accountUid, game)

        if (element != null) {
            list = list.filter { it.element.equals(element, ignoreCase = true) }
        }
        if (rarity != null) {
            list = list.filter { it.rarity == rarity }
        }

        list = when (sortBy) {
            SortOption.LEVEL -> list.sortedWith(
                compareByDescending<HoYoLabCharacterDisplay> { it.level }
                    .thenByDescending { it.rarity }
            )
            SortOption.CONSTELLATION -> list.sortedWith(
                compareByDescending<HoYoLabCharacterDisplay> { it.constellation }
                    .thenByDescending { it.level }
            )
            SortOption.FRIENDSHIP -> list.sortedWith(
                compareByDescending<HoYoLabCharacterDisplay> { it.friendship }
                    .thenByDescending { it.level }
            )
            SortOption.RARITY -> list.sortedWith(
                compareByDescending<HoYoLabCharacterDisplay> { it.rarity }
                    .thenByDescending { it.level }
            )
        }

        return list
    }

    suspend fun getDistinctElements(accountUid: String, game: SupportedGame): List<String> =
        characterDao.getDistinctElements(accountUid, game.rawValue)

    suspend fun getDistinctRarities(accountUid: String, game: SupportedGame): List<Int> =
        characterDao.getDistinctRarities(accountUid, game.rawValue)

    private fun HoYoLabCharacterGI.toDisplay(gameId: String = "GI") = HoYoLabCharacterDisplay(
        id = "${gameId}_$id",
        gameId = gameId,
        characterId = id,
        name = name,
        element = element,
        level = level,
        rarity = rarity,
        constellation = activedConstellationNum,
        friendship = fetter,
        iconUrl = image ?: cardImage,
        weaponName = weapon?.name,
        weaponLevel = weapon?.level,
        weaponRarity = weapon?.rarity,
        weaponRefinement = weapon?.affixLevel,
    )

    private fun HoYoLabCharacterGI.toEntity(accountUid: String, gameRawValue: String) = HoYoLabCharacterEntity(
        accountUid = accountUid,
        gameRawValue = gameRawValue,
        characterId = id,
        name = name,
        element = element,
        level = level,
        rarity = rarity,
        constellation = activedConstellationNum,
        friendship = fetter,
        iconUrl = image ?: cardImage,
        weaponName = weapon?.name,
        weaponLevel = weapon?.level,
        weaponRarity = weapon?.rarity,
        weaponRefinement = weapon?.affixLevel,
        fetchedAt = System.currentTimeMillis(),
    )

    private fun HoYoLabCharacterHSR.toDisplay(gameId: String = "HSR") = HoYoLabCharacterDisplay(
        id = "${gameId}_$avatarId",
        gameId = gameId,
        characterId = avatarId,
        name = name,
        element = element ?: "",
        level = level,
        rarity = rarity,
        constellation = rank,
        friendship = 0,
        iconUrl = image ?: portrait,
        weaponName = lightCone?.name,
        weaponLevel = lightCone?.level,
        weaponRarity = lightCone?.rarity,
        weaponRefinement = lightCone?.rank,
    )

    private fun HoYoLabCharacterHSR.toEntity(accountUid: String, gameRawValue: String) = HoYoLabCharacterEntity(
        accountUid = accountUid,
        gameRawValue = gameRawValue,
        characterId = avatarId,
        name = name,
        element = element ?: "",
        level = level,
        rarity = rarity,
        constellation = rank,
        friendship = 0,
        iconUrl = image ?: portrait,
        weaponName = lightCone?.name,
        weaponLevel = lightCone?.level,
        weaponRarity = lightCone?.rarity,
        weaponRefinement = lightCone?.rank,
        fetchedAt = System.currentTimeMillis(),
    )

    private fun HoYoLabCharacterEntity.toDisplay() = HoYoLabCharacterDisplay(
        id = "${gameRawValue}_$characterId",
        gameId = gameRawValue,
        characterId = characterId,
        name = name,
        element = element,
        level = level,
        rarity = rarity,
        constellation = constellation,
        friendship = friendship,
        iconUrl = iconUrl,
        weaponName = weaponName,
        weaponLevel = weaponLevel,
        weaponRarity = weaponRarity,
        weaponRefinement = weaponRefinement,
    )

    companion object {
        fun uidToServer(account: HoYoAccount, uid: String): String {
            val region = account.region
            return when (region.type) {
                RegionType.MIYOUSHE -> when (account.game) {
                    SupportedGame.GENSHIN_IMPACT -> when {
                        uid.startsWith("5") -> "cn_qd01"
                        else -> "cn_gf01"
                    }
                    SupportedGame.STAR_RAIL -> "prod_gf_cn"
                    SupportedGame.ZENLESS_ZONE -> "prod_gf_cn"
                }
                RegionType.HOYOLAB -> when (account.game) {
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
        }
    }
}

enum class SortOption {
    LEVEL,
    CONSTELLATION,
    FRIENDSHIP,
    RARITY,
}
