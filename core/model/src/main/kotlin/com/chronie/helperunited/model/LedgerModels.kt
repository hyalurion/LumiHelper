package com.chronie.helperunited.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// ========== Genshin Impact Ledger ==========

@JsonClass(generateAdapter = true)
data class LedgerData4GI(
    @Json(name = "uid") val uid: Int,
    @Json(name = "month_data") val monthData: GILedgerMonthData,
    @Json(name = "data_month") val dataMonth: Int,
    @Json(name = "region") val region: String,
    @Json(name = "optional_month") val optionalMonth: List<Int> = emptyList(),
    @Json(name = "month") val month: Int,
    @Json(name = "nickname") val nickname: String = "",
    @Json(name = "lantern") val lantern: Boolean? = null,
    @Json(name = "day_data") val dayData: GILedgerDayData,
    @Json(name = "date") val date: String? = null,
    @Json(name = "data_last_month") val dataLastMonth: Int? = null,
    @Json(name = "account_id") val accountID: Int? = null,
)

@JsonClass(generateAdapter = true)
data class GILedgerMonthData(
    @Json(name = "current_primogems") val currentPrimogems: Int,
    @Json(name = "current_primogems_level") val currentPrimogemsLevel: Int? = null,
    @Json(name = "last_mora") val lastMora: Int,
    @Json(name = "primogems_rate") val primogemsRate: Int? = null,
    @Json(name = "primogem_rate") val primogemRate: Int? = null,
    @Json(name = "mora_rate") val moraRate: Int,
    @Json(name = "group_by") val groupBy: List<GILedgerDataGroup> = emptyList(),
    @Json(name = "last_primogems") val lastPrimogems: Int,
    @Json(name = "current_mora") val currentMora: Int,
)

@JsonClass(generateAdapter = true)
data class GILedgerDataGroup(
    @Json(name = "percent") val percent: Int,
    @Json(name = "num") val num: Double,
    @Json(name = "action_id") val actionId: Int,
    @Json(name = "action") val action: String,
)

@JsonClass(generateAdapter = true)
data class GILedgerDayData(
    @Json(name = "current_mora") val currentMora: Int,
    @Json(name = "last_primogems") val lastPrimogems: Int? = null,
    @Json(name = "last_mora") val lastMora: Int? = null,
    @Json(name = "current_primogems") val currentPrimogems: Int,
)

// ========== Star Rail Ledger ==========

@JsonClass(generateAdapter = true)
data class LedgerData4HSR(
    @Json(name = "uid") val uid: String,
    @Json(name = "month_data") val monthData: HSRLedgerMonthData,
    @Json(name = "data_month") val dataMonth: String,
    @Json(name = "region") val region: String,
    @Json(name = "optional_month") val optionalMonth: List<String> = emptyList(),
    @Json(name = "month") val month: String,
    @Json(name = "day_data") val dayData: HSRLedgerDayData,
    @Json(name = "date") val date: String? = null,
    @Json(name = "data_last_month") val dataLastMonth: Int? = null,
    @Json(name = "account_id") val accountID: Int? = null,
)

@JsonClass(generateAdapter = true)
data class HSRLedgerMonthData(
    @Json(name = "current_hcoin") val currentStellarJades: Int,
    @Json(name = "current_rails_pass") val currentPasses: Int,
    @Json(name = "last_hcoin") val prevStellarJade: Int,
    @Json(name = "last_rails_pass") val prevPasses: Int,
    @Json(name = "group_by") val groupBy: List<HSRLedgerDataGroup> = emptyList(),
    @Json(name = "hcoin_rate") val stellarJadeRate: Int,
    @Json(name = "rails_rate") val passesRate: Int,
)

@JsonClass(generateAdapter = true)
data class HSRLedgerDataGroup(
    @Json(name = "percent") val percent: Int,
    @Json(name = "num") val num: Double,
    @Json(name = "action_name") val actionName: String,
    @Json(name = "action") val action: String,
)

@JsonClass(generateAdapter = true)
data class HSRLedgerDayData(
    @Json(name = "current_hcoin") val currentStellarJades: Int,
    @Json(name = "current_rails_pass") val currentPasses: Int,
    @Json(name = "last_hcoin") val prevStellarJade: Int,
    @Json(name = "last_rails_pass") val prevPasses: Int,
)