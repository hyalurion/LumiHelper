package com.hyalurion.lumi.helper.ui.enka

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hyalurion.lumi.helper.data.AccountRepository
import com.hyalurion.lumi.helper.data.ChallengeRepository
import com.hyalurion.lumi.helper.data.EnkaRepository
import com.hyalurion.lumi.helper.model.ChallengeResponse
import com.hyalurion.lumi.helper.model.EnkaProfileSummarized
import com.hyalurion.lumi.helper.model.HoYoAccount
import com.hyalurion.lumi.helper.model.SpiralAbyssResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AsyncStatus<out T> {
    data object Standby : AsyncStatus<Nothing>()
    data object Loading : AsyncStatus<Nothing>()
    data class Success<T>(val data: T) : AsyncStatus<T>()
    data class Error(val message: String) : AsyncStatus<Nothing>()
}

@HiltViewModel
class EnkaShowcaseViewModel @Inject constructor(
    private val enkaRepository: EnkaRepository,
    private val accountRepository: AccountRepository,
    private val challengeRepository: ChallengeRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(EnkaShowcaseUiState())
    val uiState: StateFlow<EnkaShowcaseUiState> = _uiState.asStateFlow()

    init {
        loadAccounts()
    }

    fun loadAccounts() {
        viewModelScope.launch {
            val accounts = accountRepository.getAllAccounts()
            _uiState.value = _uiState.value.copy(accounts = accounts)
            if (accounts.isNotEmpty() && _uiState.value.selectedAccount == null) {
                selectAccount(accounts.first())
            }
        }
    }

    fun selectAccount(account: HoYoAccount) {
        _uiState.value = _uiState.value.copy(
            selectedAccount = account,
            isLoading = false,
            profile = null,
            error = null,
            battleReportStatus = AsyncStatus.Standby,
            ledgerStatus = AsyncStatus.Standby,
        )
        queryShowcase(account.uid)
        fetchBattleReport(account)
        fetchLedger(account)
    }

    fun queryShowcase(uid: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null,
            )
            enkaRepository.fetchShowcase(uid)
                .onSuccess { profile ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        profile = profile,
                        error = null,
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Query failed",
                    )
                }
        }
    }

    fun fetchBattleReport(account: HoYoAccount) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(battleReportStatus = AsyncStatus.Loading)

            when (account.game) {
                com.hyalurion.lumi.helper.base.SupportedGame.GENSHIN_IMPACT -> {
                    challengeRepository.fetchSpiralAbyss(account)
                        .onSuccess { data ->
                            _uiState.value = _uiState.value.copy(
                                battleReportStatus = AsyncStatus.Success(data),
                            )
                        }
                        .onFailure { e ->
                            _uiState.value = _uiState.value.copy(
                                battleReportStatus = AsyncStatus.Error(
                                    e.message ?: "Failed to fetch Abyss data"
                                ),
                            )
                        }
                }
                com.hyalurion.lumi.helper.base.SupportedGame.STAR_RAIL -> {
                    challengeRepository.fetchChallengeHSR(account)
                        .onSuccess { data ->
                            _uiState.value = _uiState.value.copy(
                                battleReportStatus = AsyncStatus.Success(data),
                            )
                        }
                        .onFailure { e ->
                            _uiState.value = _uiState.value.copy(
                                battleReportStatus = AsyncStatus.Error(
                                    e.message ?: "Failed to fetch challenge data"
                                ),
                            )
                        }
                }
                else -> {
                    _uiState.value = _uiState.value.copy(battleReportStatus = AsyncStatus.Standby)
                }
            }
        }
    }

    fun fetchLedger(account: HoYoAccount) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(ledgerStatus = AsyncStatus.Loading)

            when (account.game) {
                com.hyalurion.lumi.helper.base.SupportedGame.GENSHIN_IMPACT -> {
                    challengeRepository.fetchLedgerGI(account)
                        .onSuccess { data ->
                            _uiState.value = _uiState.value.copy(
                                ledgerStatus = AsyncStatus.Success(data),
                            )
                        }
                        .onFailure { e ->
                            _uiState.value = _uiState.value.copy(
                                ledgerStatus = AsyncStatus.Error(
                                    e.message ?: "Failed to fetch ledger data"
                                ),
                            )
                        }
                }
                com.hyalurion.lumi.helper.base.SupportedGame.STAR_RAIL -> {
                    challengeRepository.fetchLedgerHSR(account)
                        .onSuccess { data ->
                            _uiState.value = _uiState.value.copy(
                                ledgerStatus = AsyncStatus.Success(data),
                            )
                        }
                        .onFailure { e ->
                            _uiState.value = _uiState.value.copy(
                                ledgerStatus = AsyncStatus.Error(
                                    e.message ?: "Failed to fetch ledger data"
                                ),
                            )
                        }
                }
                else -> {
                    _uiState.value = _uiState.value.copy(ledgerStatus = AsyncStatus.Standby)
                }
            }
        }
    }

    fun refresh() {
        val account = _uiState.value.selectedAccount ?: return
        queryShowcase(account.uid)
        fetchBattleReport(account)
        fetchLedger(account)
    }

    fun clearShowcase() {
        _uiState.value = EnkaShowcaseUiState()
    }

    fun getCachedUids(): List<String> {
        return enkaRepository.getCachedUids()
    }
}

data class EnkaShowcaseUiState(
    val accounts: List<HoYoAccount> = emptyList(),
    val selectedAccount: HoYoAccount? = null,
    val isLoading: Boolean = false,
    val profile: EnkaProfileSummarized? = null,
    val error: String? = null,
    val battleReportStatus: AsyncStatus<*> = AsyncStatus.Standby,
    val ledgerStatus: AsyncStatus<*> = AsyncStatus.Standby,
)