package com.hyalurion.lumi.helper.ui.account.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hyalurion.lumi.helper.base.AccountRegion
import com.hyalurion.lumi.helper.base.SupportedGame
import com.hyalurion.lumi.helper.network.CookieTokenData
import com.hyalurion.lumi.helper.network.CookieTokenResponse
import com.hyalurion.lumi.helper.network.FetchedAccount
import com.hyalurion.lumi.helper.network.GenerateQRCodeResponse
import com.hyalurion.lumi.helper.network.LoginApiService
import com.hyalurion.lumi.helper.network.QrCodeStatusData
import com.hyalurion.lumi.helper.network.QueryQrCodeStatusResponse
import com.hyalurion.lumi.helper.network.TokenItem
import com.hyalurion.lumi.helper.util.DeviceFingerprintUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject

data class QrCodeLoginUiState(
    val qrCodeUrl: String? = null,
    val ticket: String? = null,
    val status: QrCodeStatus = QrCodeStatus.Loading,
    val fetchedAccounts: List<FetchedAccount> = emptyList(),
    val cookieToken: String? = null,
    val stoken: String? = null,
    val loginUid: String? = null,
    val deviceFp: String? = null,
    val error: String? = null,
)

enum class QrCodeStatus {
    Loading,
    WaitingForScan,
    Scanned,
    Confirmed,
    Error,
}

@HiltViewModel
class QrCodeLoginViewModel @Inject constructor(
    private val application: Application,
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(QrCodeLoginUiState())
    val uiState: StateFlow<QrCodeLoginUiState> = _uiState.asStateFlow()

    private val apiClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val loginApi: LoginApiService = Retrofit.Builder()
        .baseUrl("https://passport-api.mihoyo.com/")
        .client(apiClient)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()
        .create(LoginApiService::class.java)

    private val accountApi: LoginApiService = Retrofit.Builder()
        .baseUrl("https://api-takumi.mihoyo.com/")
        .client(apiClient)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()
        .create(LoginApiService::class.java)

    private var pollingJob: Job? = null
    private var deviceId: String = UUID.randomUUID().toString().replace("-", "")

    fun startQRLogin() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    status = QrCodeStatus.Loading,
                    error = null,
                )
                val response = loginApi.createQRLogin(
                    mapOf("app_id" to "4", "device" to deviceId)
                )
                if (response.retCode != 0 || response.data == null) {
                    _uiState.value = _uiState.value.copy(
                        status = QrCodeStatus.Error,
                        error = "创建二维码失败: ${response.message}",
                    )
                    return@launch
                }
                val data = response.data!!
                _uiState.value = _uiState.value.copy(
                    qrCodeUrl = data.url,
                    ticket = data.ticket,
                    status = QrCodeStatus.WaitingForScan,
                )
                startPolling(data.ticket)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    status = QrCodeStatus.Error,
                    error = "网络错误: ${e.message}",
                )
            }
        }
    }

    private fun startPolling(ticket: String) {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (isActive) {
                delay(3000L)
                try {
                    val response = loginApi.queryQRLoginStatus(
                        mapOf("app_id" to "4", "device" to deviceId, "ticket" to ticket)
                    )
                    val data = response.data ?: continue
                    when (data.status) {
                        "Scanned" -> {
                            if (_uiState.value.status != QrCodeStatus.Scanned) {
                                _uiState.value = _uiState.value.copy(status = QrCodeStatus.Scanned)
                            }
                        }
                        "Confirmed" -> {
                            _uiState.value = _uiState.value.copy(
                                status = QrCodeStatus.Confirmed,
                                stoken = data.tokens?.firstOrNull()?.token,
                            )
                            pollingJob?.cancel()
                            onQRCodeConfirmed(data)
                            return@launch
                        }
                        "Expired" -> {
                            _uiState.value = _uiState.value.copy(
                                status = QrCodeStatus.Error,
                                error = "二维码已过期，请重新生成",
                            )
                            pollingJob?.cancel()
                            return@launch
                        }
                        else -> { /* Created - still waiting */ }
                    }
                } catch (_: Exception) {
                    // polling error, continue
                }
            }
        }
    }

    private suspend fun onQRCodeConfirmed(statusData: QrCodeStatusData) {
        try {
            val parsed = parseGameToken(statusData)
            if (parsed == null) {
                _uiState.value = _uiState.value.copy(error = "解析登录信息失败")
                return
            }

            val (stoken, loginUid) = parsed
            val cookieToken = getCookieToken(stoken, loginUid)
            if (cookieToken == null) {
                _uiState.value = _uiState.value.copy(error = "获取Cookie Token失败")
                return
            }

            val deviceFp = generateDeviceFP()

            _uiState.value = _uiState.value.copy(
                stoken = stoken,
                cookieToken = cookieToken.cookieToken,
                loginUid = loginUid,
                deviceFp = deviceFp?.deviceFP,
            )

            fetchAccountList(stoken, cookieToken.cookieToken)
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                status = QrCodeStatus.Error,
                error = "处理登录结果失败: ${e.message}",
            )
        }
    }

    private fun parseGameToken(statusData: QrCodeStatusData): Pair<String, String>? {
        val stokenItem = statusData.tokens?.find { it.tokenType == 2 }
            ?: statusData.tokens?.firstOrNull()
        val stoken = stokenItem?.token ?: return null
        val loginUid = statusData.userInfo?.aid ?: statusData.userInfo?.uid
            ?: statusData.userInfo?.accountId ?: return null
        return stoken to loginUid
    }

    private suspend fun getCookieToken(
        stoken: String,
        loginUid: String,
    ): CookieTokenData? {
        val request = Request.Builder()
            .url("https://api-takumi.mihoyo.com/auth/api/getCookieAccountInfoBySToken")
            .header("Cookie", "stoken=$stoken;stuid=$loginUid")
            .build()
        val response = apiClient.newCall(request).execute()
        val bodyString = response.body?.string() ?: return null
        val moshi = com.squareup.moshi.Moshi.Builder()
            .addLast(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
            .build()
        val adapter = moshi.adapter(CookieTokenResponse::class.java)
        val result = adapter.fromJson(bodyString) ?: return null
        return if (result.retCode == 0) result.data else null
    }

    private suspend fun generateDeviceFP(): DeviceFingerprintUtil.FingerPrintResult? {
        return try {
            DeviceFingerprintUtil.generateDeviceFingerPrint(
                region = AccountRegion(
                    com.hyalurion.lumi.helper.base.RegionType.MIYOUSHE,
                    SupportedGame.GENSHIN_IMPACT,
                ),
                context = application,
            )
        } catch (_: Exception) {
            null
        }
    }

    private suspend fun fetchAccountList(stoken: String, cookieToken: String) {
        val gameBizList = listOf(
            "hk4e_cn" to SupportedGame.GENSHIN_IMPACT,
            "hkrpg_cn" to SupportedGame.STAR_RAIL,
            "nap_cn" to SupportedGame.ZENLESS_ZONE,
        )
        val allAccounts = mutableListOf<FetchedAccount>()
        for ((biz, _) in gameBizList) {
            try {
                val request = Request.Builder()
                    .url("https://api-takumi.mihoyo.com/binding/api/getUserGameRolesByCookie?game_biz=$biz")
                    .header("Cookie", "stoken=$stoken;stuid=${cookieToken};cookie_token=$cookieToken")
                    .build()
                val response = apiClient.newCall(request).execute()
                val bodyString = response.body?.string() ?: continue
                val moshi = com.squareup.moshi.Moshi.Builder()
                    .addLast(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
                    .build()
                val adapter = moshi.adapter(
                    com.hyalurion.lumi.helper.network.UserGameRolesResponse::class.java
                )
                val result = adapter.fromJson(bodyString) ?: continue
                val rolesData = result.data
                if (result.retCode == 0 && rolesData != null) {
                    allAccounts.addAll(rolesData.list)
                }
            } catch (_: Exception) {
                // skip failed game
            }
        }
        _uiState.value = _uiState.value.copy(fetchedAccounts = allAccounts)
    }

    fun reCreateQRCode() {
        pollingJob?.cancel()
        _uiState.value = _uiState.value.copy(
            qrCodeUrl = null,
            ticket = null,
            status = QrCodeStatus.Loading,
            error = null,
        )
        startQRLogin()
    }

    fun checkScanned() {
        if (_uiState.value.status == QrCodeStatus.Scanned) {
            _uiState.value = _uiState.value.copy(status = QrCodeStatus.Loading)
        }
    }

    fun checkScannedManually(
        onNotScanned: () -> Unit,
    ) {
        val ticket = _uiState.value.ticket ?: return
        pollingJob?.cancel()
        _uiState.value = _uiState.value.copy(status = QrCodeStatus.Loading)
        viewModelScope.launch {
            try {
                val response = loginApi.queryQRLoginStatus(
                    mapOf("app_id" to "4", "device" to deviceId, "ticket" to ticket)
                )
                val data = response.data
                if (data != null && data.status == "Confirmed") {
                    _uiState.value = _uiState.value.copy(
                        status = QrCodeStatus.Confirmed,
                        stoken = data.tokens?.firstOrNull()?.token,
                    )
                    onQRCodeConfirmed(data)
                } else {
                    onNotScanned()
                    _uiState.value = _uiState.value.copy(
                        status = QrCodeStatus.WaitingForScan,
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    status = QrCodeStatus.Error,
                    error = "检查扫码状态失败: ${e.message}",
                )
            }
        }
    }

    fun reset() {
        pollingJob?.cancel()
        _uiState.value = QrCodeLoginUiState()
    }

    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
    }
}