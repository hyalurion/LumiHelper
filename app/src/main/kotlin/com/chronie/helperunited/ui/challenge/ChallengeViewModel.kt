package com.chronie.helperunited.ui.challenge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chronie.helperunited.base.SupportedGame
import com.chronie.helperunited.data.AccountRepository
import com.chronie.helperunited.data.ChallengeRepository
import com.chronie.helperunited.model.ChallengeResponse
import com.chronie.helperunited.model.HoYoAccount
import com.chronie.helperunited.model.HsrChallengeType
import com.chronie.helperunited.model.SpiralAbyssResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Challenge Record screen.
 * Manages fetching and displaying Spiral Abyss (GI) and
 * Memory of Chaos / Pure Fiction (HSR) data.
 */
@HiltViewModel
class ChallengeViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val challengeRepository: ChallengeRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChallengeUiState())
    val uiState: StateFlow<ChallengeUiState> = _uiState.asStateFlow()

    init {
        loadAccounts()
    }

    private fun loadAccounts() {
        viewModelScope.launch {
            accountRepository.observeAllAccounts().collect { accounts ->
                _uiState.value = _uiState.value.copy(accounts = accounts)
                // Auto-select first account if none selected
                if (_uiState.value.selectedAccount == null && accounts.isNotEmpty()) {
                    selectAccount(accounts.first())
                }
            }
        }
    }

    fun selectAccount(account: HoYoAccount) {
        _uiState.value = _uiState.value.copy(
            selectedAccount = account,
            spiralAbyss = null,
            challengeHSR = null,
            error = null,
        )
        fetchChallengeData(account)
    }

    fun refresh() {
        val account = _uiState.value.selectedAccount ?: return
        fetchChallengeData(account)
    }

    fun selectHsrChallengeType(type: HsrChallengeType) {
        val account = _uiState.value.selectedAccount ?: return
        _uiState.value = _uiState.value.copy(
            selectedHsrChallengeType = type,
            challengeHSR = null,
        )
        fetchHsrChallenge(account, type)
    }

    private fun fetchChallengeData(account: HoYoAccount) {
        when (account.game) {
            SupportedGame.GENSHIN_IMPACT -> fetchSpiralAbyss(account)
            SupportedGame.STAR_RAIL -> fetchHsrChallenge(
                account,
                _uiState.value.selectedHsrChallengeType,
            )
            SupportedGame.ZENLESS_ZONE -> {
                // ZZZ Shiyu Defense not yet supported
                _uiState.value = _uiState.value.copy(
                    error = "暂不支持绝区零式舆防卫战记录",
                )
            }
        }
    }

    private fun fetchSpiralAbyss(account: HoYoAccount) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            challengeRepository.fetchSpiralAbyss(account, scheduleType = 1)
                .onSuccess { data ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        spiralAbyss = data,
                        challengeHSR = null,
                        error = null,
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "获取深境螺旋数据失败",
                    )
                }
        }
    }

    private fun fetchHsrChallenge(account: HoYoAccount, type: HsrChallengeType) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            challengeRepository.fetchChallengeHSR(account, type)
                .onSuccess { data ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        spiralAbyss = null,
                        challengeHSR = data,
                        error = null,
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "获取挑战记录失败",
                    )
                }
        }
    }
}

data class ChallengeUiState(
    val isLoading: Boolean = false,
    val accounts: List<HoYoAccount> = emptyList(),
    val selectedAccount: HoYoAccount? = null,
    val spiralAbyss: SpiralAbyssResponse? = null,
    val challengeHSR: ChallengeResponse? = null,
    val selectedHsrChallengeType: HsrChallengeType = HsrChallengeType.MEMORY_OF_CHAOS,
    val error: String? = null,
)
