package com.chronie.helperunited.model

import com.chronie.helperunited.base.AccountRegion
import com.chronie.helperunited.base.SupportedGame
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.UUID

/**
 * HoYoverse account model.
 * Ported from PizzaHelperUnited's AccountMOProtocol.
 */
@JsonClass(generateAdapter = false)
data class HoYoAccount(
    val uuid: String = UUID.randomUUID().toString(),
    val uid: String,
    val name: String,
    val region: AccountRegion,
    val cookie: String,
    val deviceFingerPrint: String = "",
    val allowNotification: Boolean = true,
    val priority: Int = 0,
    val sTokenV2: String? = null,
) {
    val game: SupportedGame get() = region.game
    val displayUid: String get() = game.withUid(uid)

    companion object {
        const val ENTITY_NAME = "HoYoAccount"
    }
}

// --- Generic HoYoLab API Response ---

@JsonClass(generateAdapter = true)
data class HoYoApiResponse<T>(
    @Json(name = "retcode") val retCode: Int,
    val message: String,
    val data: T?,
)

// --- Generic Paginated Response ---

@JsonClass(generateAdapter = true)
data class PaginatedData<T>(
    val list: List<T>,
    val page: Int? = null,
    val pageSize: Int? = null,
    val total: Int? = null,
)
