package com.chronie.helperunited.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.chronie.helperunited.base.AccountRegion
import com.chronie.helperunited.base.SupportedGame
import com.chronie.helperunited.model.HoYoAccount

/**
 * Room entity for persisting HoYoverse accounts.
 * Ported from PizzaHelperUnited's AccountMO_Base.
 */
@Entity(tableName = "hoyo_accounts")
data class AccountEntity(
    @PrimaryKey
    val uuid: String,
    val uid: String,
    val name: String,
    val gameRawValue: String,
    val regionRawValue: String,
    val cookie: String,
    val deviceFingerPrint: String = "",
    val allowNotification: Boolean = true,
    val priority: Int = 0,
    val sTokenV2: String? = null,
) {
    fun toDomain(): HoYoAccount? {
        val game = SupportedGame.fromRawValue(gameRawValue) ?: return null
        val region = AccountRegion.fromRawValue(regionRawValue) ?: return null
        return HoYoAccount(
            uuid = uuid,
            uid = uid,
            name = name,
            region = region,
            cookie = cookie,
            deviceFingerPrint = deviceFingerPrint,
            allowNotification = allowNotification,
            priority = priority,
            sTokenV2 = sTokenV2,
        )
    }

    companion object {
        fun fromDomain(account: HoYoAccount) = AccountEntity(
            uuid = account.uuid,
            uid = account.uid,
            name = account.name,
            gameRawValue = account.game.rawValue,
            regionRawValue = account.region.rawValue,
            cookie = account.cookie,
            deviceFingerPrint = account.deviceFingerPrint,
            allowNotification = account.allowNotification,
            priority = account.priority,
            sTokenV2 = account.sTokenV2,
        )
    }
}

/**
 * Room entity for caching daily note data.
 */
@Entity(tableName = "daily_note_cache")
data class DailyNoteCacheEntity(
    @PrimaryKey
    val accountUid: String,
    val gameRawValue: String,
    val jsonPayload: String,
    val fetchedAt: Long,
)
