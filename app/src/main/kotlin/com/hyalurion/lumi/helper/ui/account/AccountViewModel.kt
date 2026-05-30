package com.hyalurion.lumi.helper.ui.account

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hyalurion.lumi.helper.base.AccountRegion
import com.hyalurion.lumi.helper.base.RegionType
import com.hyalurion.lumi.helper.base.SupportedGame
import com.hyalurion.lumi.helper.data.AccountRepository
import com.hyalurion.lumi.helper.model.HoYoAccount
import com.hyalurion.lumi.helper.network.FetchedAccount
import com.hyalurion.lumi.helper.network.UserGameRolesResponse
import com.hyalurion.lumi.helper.ui.account.login.HoYoLabCookieData
import com.hyalurion.lumi.helper.util.DeviceFingerprintUtil
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject

enum class AddProfileStatus {
    Pending,
    GotCookie,
    GotProfile,
}

enum class LoginMethod {
    HOYOLAB_WEBVIEW,
    MIYOUSHE_QRCODE,
}

data class AccountUiState(
    val accounts: List<HoYoAccount> = emptyList(),
    val isEditMode: Boolean = false,
    val isBusy: Boolean = false,

    val showCreateSheet: Boolean = false,
    val createStep: AddProfileStatus = AddProfileStatus.Pending,
    val selectedGame: SupportedGame = SupportedGame.GENSHIN_IMPACT,
    val selectedRegionType: RegionType = RegionType.MIYOUSHE,
    val importAllUIDs: Boolean = true,
    val loginMethod: LoginMethod? = null,
    val fetchedAccounts: List<FetchedAccount> = emptyList(),
    val cookieToken: String? = null,
    val ltoken: String? = null,
    val ltuid: String? = null,
    val stoken: String? = null,
    val mid: String? = null,
    val deviceFp: String? = null,
    val isFetchingAccounts: Boolean = false,
    val profileName: String = "",
    val selectedFetchedAccount: FetchedAccount? = null,

    val showEditSheet: Boolean = false,
    val editingAccount: HoYoAccount? = null,

    val showSuccessToast: Boolean = false,
    val showErrorToast: Boolean = false,
    val toastMessage: String = "",
    val error: String? = null,
)

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val application: Application,
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(AccountUiState())
    val uiState: StateFlow<AccountUiState> = _uiState.asStateFlow()

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    init {
        loadAccounts()
    }

    private fun loadAccounts() {
        viewModelScope.launch {
            accountRepository.observeAllAccounts().collect { accounts ->
                _uiState.value = _uiState.value.copy(accounts = accounts)
            }
        }
    }

    fun toggleEditMode() {
        _uiState.value = _uiState.value.copy(
            isEditMode = !_uiState.value.isEditMode,
        )
    }

    // ============================================================
    // Create Profile Flow (matches CreateProfileSheetView)
    // ============================================================

    fun showCreateSheet() {
        _uiState.value = _uiState.value.copy(
            showCreateSheet = true,
            createStep = AddProfileStatus.Pending,
            selectedGame = SupportedGame.GENSHIN_IMPACT,
            selectedRegionType = RegionType.MIYOUSHE,
            importAllUIDs = true,
            loginMethod = null,
            fetchedAccounts = emptyList(),
            cookieToken = null,
            ltoken = null,
            ltuid = null,
            stoken = null,
            mid = null,
            deviceFp = null,
            isFetchingAccounts = false,
            profileName = "",
            selectedFetchedAccount = null,
            error = null,
        )
    }

    fun dismissCreateSheet() {
        _uiState.value = _uiState.value.copy(
            showCreateSheet = false,
            createStep = AddProfileStatus.Pending,
            loginMethod = null,
            fetchedAccounts = emptyList(),
            cookieToken = null,
            ltoken = null,
            ltuid = null,
            stoken = null,
            deviceFp = null,
            isFetchingAccounts = false,
            profileName = "",
            selectedFetchedAccount = null,
            error = null,
        )
    }

    fun selectGame(game: SupportedGame) {
        _uiState.value = _uiState.value.copy(selectedGame = game)
    }

    fun selectRegionType(type: RegionType) {
        _uiState.value = _uiState.value.copy(selectedRegionType = type)
    }

    fun toggleImportAllUIDs() {
        _uiState.value = _uiState.value.copy(
            importAllUIDs = !_uiState.value.importAllUIDs,
        )
    }

    fun selectLoginMethod(method: LoginMethod) {
        _uiState.value = _uiState.value.copy(
            loginMethod = method,
            createStep = AddProfileStatus.GotCookie,
        )
    }

    // ============================================================
    // HoYoLab WebView Cookie Flow
    // ============================================================

    fun onHoYoLabCookieExtracted(cookie: HoYoLabCookieData) {
        _uiState.value = _uiState.value.copy(
            createStep = AddProfileStatus.GotCookie,
            cookieToken = cookie.cookieToken,
            ltoken = cookie.ltoken,
            ltuid = cookie.ltuid,
            isFetchingAccounts = true,
        )

        viewModelScope.launch {
            try {
                val region = AccountRegion(RegionType.HOYOLAB, _uiState.value.selectedGame)
                val fp = DeviceFingerprintUtil.generateDeviceFingerPrint(
                    region = region,
                    context = application,
                )
                _uiState.value = _uiState.value.copy(deviceFp = fp.deviceFP)
            } catch (_: Exception) {
                // device FP is optional for HoYoLab
            }

            if (_uiState.value.importAllUIDs) {
                fetchAllAccountsForAllGames(
                    regionType = RegionType.HOYOLAB,
                    cookieStr = buildHoYoLabCookieHeader(cookie),
                )
            } else {
                fetchAccountsForSingleGame(
                    regionType = RegionType.HOYOLAB,
                    game = _uiState.value.selectedGame,
                    cookieStr = buildHoYoLabCookieHeader(cookie),
                )
            }
        }
    }

    private fun buildHoYoLabCookieHeader(cookie: HoYoLabCookieData): String {
        return buildString {
            append("ltoken=${cookie.ltoken}; ")
            append("ltuid=${cookie.ltuid}; ")
            append("cookie_token=${cookie.cookieToken}; ")
            append("account_id=${cookie.accountId}")
        }
    }

    // ============================================================
    // MiYouShe QR Code Login Flow
    // ============================================================

    fun onQrCodeLoginComplete(
        accounts: List<FetchedAccount>,
        stoken: String?,
        cookieToken: String?,
        ltuid: String?,
        deviceFp: String? = null,
    ) {
        _uiState.value = _uiState.value.copy(
            createStep = AddProfileStatus.GotCookie,
            stoken = stoken,
            cookieToken = cookieToken,
            ltuid = ltuid,
            deviceFp = deviceFp,
            isFetchingAccounts = true,
        )

        if (deviceFp == null) {
            viewModelScope.launch {
                try {
                    val region = AccountRegion(RegionType.MIYOUSHE, _uiState.value.selectedGame)
                    val fp = DeviceFingerprintUtil.generateDeviceFingerPrint(
                        region = region,
                        context = application,
                    )
                    _uiState.value = _uiState.value.copy(deviceFp = fp.deviceFP)
                } catch (_: Exception) {
                    _uiState.value = _uiState.value.copy(
                        error = "获取设备指纹失败",
                    )
                }
            }
        }

        _uiState.value = _uiState.value.copy(
            fetchedAccounts = accounts,
            isFetchingAccounts = false,
            createStep = AddProfileStatus.GotProfile,
        )

        if (accounts.isNotEmpty()) {
            val first = accounts.first()
            _uiState.value = _uiState.value.copy(
                selectedFetchedAccount = first,
                profileName = first.nickname.ifBlank { "旅行者" },
            )
        }
    }

    // ============================================================
    // Account Fetching (matches getAllAccountsFetched / getAccountForSelected)
    // ============================================================

    private suspend fun fetchAllAccountsForAllGames(
        regionType: RegionType,
        cookieStr: String,
    ) {
        val gameBizList = when (regionType) {
            RegionType.HOYOLAB -> listOf(
                "hk4e_global" to SupportedGame.GENSHIN_IMPACT,
                "hkrpg_global" to SupportedGame.STAR_RAIL,
                "nap_global" to SupportedGame.ZENLESS_ZONE,
            )
            RegionType.MIYOUSHE -> listOf(
                "hk4e_cn" to SupportedGame.GENSHIN_IMPACT,
                "hkrpg_cn" to SupportedGame.STAR_RAIL,
                "nap_cn" to SupportedGame.ZENLESS_ZONE,
            )
        }

        val apiHost = when (regionType) {
            RegionType.HOYOLAB -> "api-account-os.hoyolab.com"
            RegionType.MIYOUSHE -> "api-takumi.mihoyo.com"
        }

        val allAccounts = mutableListOf<FetchedAccount>()

        for ((biz, _) in gameBizList) {
            try {
                val request = Request.Builder()
                    .url("https://$apiHost/binding/api/getUserGameRolesByCookie?game_biz=$biz")
                    .header("Cookie", cookieStr)
                    .build()
                val response = httpClient.newCall(request).execute()
                val bodyString = response.body?.string() ?: continue
                val adapter = moshi.adapter(UserGameRolesResponse::class.java)
                val result = adapter.fromJson(bodyString) ?: continue
                val rolesData = result.data
                if (result.retCode == 0 && rolesData != null) {
                    allAccounts.addAll(rolesData.list)
                }
            } catch (_: Exception) {
                // skip failed game biz
            }
        }

        _uiState.value = _uiState.value.copy(
            fetchedAccounts = allAccounts,
            isFetchingAccounts = false,
            createStep = AddProfileStatus.GotProfile,
        )

        if (allAccounts.isNotEmpty()) {
            val first = allAccounts.first()
            _uiState.value = _uiState.value.copy(
                selectedFetchedAccount = first,
                profileName = first.nickname.ifBlank { "旅行者" },
            )
        } else {
            _uiState.value = _uiState.value.copy(
                error = "未检测到游戏账号，请重新登录",
                createStep = AddProfileStatus.Pending,
            )
        }
    }

    private suspend fun fetchAccountsForSingleGame(
        regionType: RegionType,
        game: SupportedGame,
        cookieStr: String,
    ) {
        val gameBiz = when (regionType) {
            RegionType.HOYOLAB -> when (game) {
                SupportedGame.GENSHIN_IMPACT -> "hk4e_global"
                SupportedGame.STAR_RAIL -> "hkrpg_global"
                SupportedGame.ZENLESS_ZONE -> "nap_global"
                else -> return
            }
            RegionType.MIYOUSHE -> when (game) {
                SupportedGame.GENSHIN_IMPACT -> "hk4e_cn"
                SupportedGame.STAR_RAIL -> "hkrpg_cn"
                SupportedGame.ZENLESS_ZONE -> "nap_cn"
                else -> return
            }
        }

        val apiHost = when (regionType) {
            RegionType.HOYOLAB -> "api-account-os.hoyolab.com"
            RegionType.MIYOUSHE -> "api-takumi.mihoyo.com"
        }

        try {
            val request = Request.Builder()
                .url("https://$apiHost/binding/api/getUserGameRolesByCookie?game_biz=$gameBiz")
                .header("Cookie", cookieStr)
                .build()
            val response = httpClient.newCall(request).execute()
            val bodyString = response.body?.string() ?: throw Exception("Empty response")
            val adapter = moshi.adapter(UserGameRolesResponse::class.java)
            val result = adapter.fromJson(bodyString)
                ?: throw Exception("Failed to parse response")
            val rolesData = result.data
            if (result.retCode != 0 || rolesData == null) {
                throw Exception(result.message)
            }

            val accounts = rolesData.list
            _uiState.value = _uiState.value.copy(
                fetchedAccounts = accounts,
                isFetchingAccounts = false,
                createStep = AddProfileStatus.GotProfile,
            )

            if (accounts.isNotEmpty()) {
                val first = accounts.first()
                _uiState.value = _uiState.value.copy(
                    selectedFetchedAccount = first,
                    profileName = first.nickname.ifBlank { "旅行者" },
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    error = "该游戏未检测到账号，请重新登录",
                    createStep = AddProfileStatus.Pending,
                )
            }
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                error = "获取账号列表失败: ${e.message}",
                isFetchingAccounts = false,
                createStep = AddProfileStatus.Pending,
            )
        }
    }

    // ============================================================
    // Profile Configuration (matches ProfileConfigViewContents)
    // ============================================================

    fun selectFetchedAccount(account: FetchedAccount) {
        _uiState.value = _uiState.value.copy(
            selectedFetchedAccount = account,
            profileName = account.nickname.ifBlank { "旅行者" },
        )
    }

    fun updateProfileName(name: String) {
        _uiState.value = _uiState.value.copy(profileName = name)
    }

    fun saveProfile() {
        val state = _uiState.value
        val fetched = state.selectedFetchedAccount ?: return

        val game = resolveGame(fetched.gameBiz)
        val region = resolveAccountRegion(fetched)
        val cookie = buildCookieString(state)

        val account = HoYoAccount(
            uuid = UUID.randomUUID().toString(),
            uid = fetched.gameUid,
            name = state.profileName.ifBlank {
                "${game.shortName} · ${fetched.gameUid}"
            },
            region = region,
            cookie = cookie,
            deviceFingerPrint = state.deviceFp ?: "",
            priority = state.accounts.size,
        )

        viewModelScope.launch {
            accountRepository.addAccount(account)
            showToast("账号添加成功", isSuccess = true)
            dismissCreateSheet()
        }
    }

    // ============================================================
    // Edit Profile Flow (matches EditProfileSheetView)
    // ============================================================

    fun showEditSheet(account: HoYoAccount) {
        _uiState.value = _uiState.value.copy(
            showEditSheet = true,
            editingAccount = account,
            profileName = account.name,
        )
    }

    fun dismissEditSheet() {
        _uiState.value = _uiState.value.copy(
            showEditSheet = false,
            editingAccount = null,
            profileName = "",
        )
    }

    fun saveEditedProfile() {
        val state = _uiState.value
        val account = state.editingAccount ?: return

        val updated = account.copy(name = state.profileName)

        viewModelScope.launch {
            accountRepository.updateAccount(updated)
            showToast("账号已更新", isSuccess = true)
            dismissEditSheet()
        }
    }

    // ============================================================
    // Delete (matches deleteItems)
    // ============================================================

    fun deleteAccount(uuid: String) {
        viewModelScope.launch {
            accountRepository.deleteAccount(uuid)
            showToast("账号已删除", isSuccess = true)
        }
    }

    // ============================================================
    // Reorder (matches moveItems)
    // ============================================================

    fun moveAccount(fromIndex: Int, toIndex: Int) {
        val mutableList = _uiState.value.accounts.toMutableList()
        val item = mutableList.removeAt(fromIndex)
        mutableList.add(toIndex, item)

        viewModelScope.launch {
            mutableList.forEachIndexed { index, account ->
                val updated = account.copy(priority = index)
                accountRepository.updateAccount(updated)
            }
        }
    }

    // ============================================================
    // Toast
    // ============================================================

    fun dismissToast() {
        _uiState.value = _uiState.value.copy(
            showSuccessToast = false,
            showErrorToast = false,
            toastMessage = "",
        )
    }

    private fun showToast(message: String, isSuccess: Boolean) {
        _uiState.value = _uiState.value.copy(
            showSuccessToast = isSuccess,
            showErrorToast = !isSuccess,
            toastMessage = message,
        )
    }

    // ============================================================
    // Helpers
    // ============================================================

    private fun resolveGame(gameBiz: String): SupportedGame {
        return when {
            gameBiz.contains("hk4e", ignoreCase = true) -> SupportedGame.GENSHIN_IMPACT
            gameBiz.contains("hkrpg", ignoreCase = true) -> SupportedGame.STAR_RAIL
            gameBiz.contains("nap", ignoreCase = true) -> SupportedGame.ZENLESS_ZONE
            else -> SupportedGame.GENSHIN_IMPACT
        }
    }

    private fun resolveAccountRegion(account: FetchedAccount): AccountRegion {
        val game = resolveGame(account.gameBiz)
        val isCN = account.gameBiz.endsWith("_cn")
        return AccountRegion(
            type = if (isCN) RegionType.MIYOUSHE else RegionType.HOYOLAB,
            game = game,
        )
    }

    private fun buildCookieString(state: AccountUiState): String {
        val regionType = state.selectedRegionType
        return when (regionType) {
            RegionType.HOYOLAB -> buildString {
                state.ltoken?.let {
                    append("ltoken_v2=$it; ")
                    append("ltoken=$it; ")
                }
                state.ltuid?.let {
                    append("ltuid_v2=$it; ")
                    append("ltuid=$it; ")
                    append("account_id_v2=$it; ")
                    append("account_id=$it; ")
                }
                state.cookieToken?.let {
                    append("cookie_token_v2=$it; ")
                    append("cookie_token=$it")
                }
            }
            RegionType.MIYOUSHE -> buildString {
                state.cookieToken?.let {
                    append("cookie_token=$it; ")
                }
                state.stoken?.let {
                    append("stoken=$it; ")
                }
                state.ltuid?.let {
                    append("stuid=$it; ")
                }
                state.mid?.let {
                    append("mid=$it")
                }
            }
        }
    }
}