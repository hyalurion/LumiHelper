package com.chronie.helperunited.ui.daily

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chronie.helperunited.data.AccountRepository
import com.chronie.helperunited.data.DailyNoteRepository
import com.chronie.helperunited.model.DailyNoteGI
import com.chronie.helperunited.model.DailyNoteHSR
import com.chronie.helperunited.model.DailyNoteZZZ
import com.chronie.helperunited.model.HoYoAccount
import com.chronie.helperunited.base.SupportedGame
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Daily Note screen.
 * Manages fetching and displaying real-time notes (resin, stamina, energy).
 * Ported from PizzaHelperUnited's DailyNote widget logic.
 */
@HiltViewModel
class DailyNoteViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val dailyNoteRepository: DailyNoteRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DailyNoteUiState())
    val uiState: StateFlow<DailyNoteUiState> = _uiState.asStateFlow()

    init {
        loadAccounts()
    }

    private fun loadAccounts() {
        viewModelScope.launch {
            accountRepository.observeAllAccounts().collect { accounts ->
                _uiState.value = _uiState.value.copy(accounts = accounts)
                // Auto-fetch for the first account if none selected
                if (_uiState.value.selectedAccount == null && accounts.isNotEmpty()) {
                    selectAccount(accounts.first())
                }
            }
        }
    }

    fun selectAccount(account: HoYoAccount) {
        _uiState.value = _uiState.value.copy(selectedAccount = account)
        fetchDailyNote(account)
    }

    fun refresh() {
        val account = _uiState.value.selectedAccount ?: return
        fetchDailyNote(account)
    }

    private fun fetchDailyNote(account: HoYoAccount) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            when (account.game) {
                SupportedGame.GENSHIN_IMPACT -> {
                    dailyNoteRepository.fetchDailyNoteGI(account)
                        .onSuccess { note ->
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                dailyNoteGI = note,
                                dailyNoteHSR = null,
                                dailyNoteZZZ = null,
                                error = null,
                            )
                        }
                        .onFailure { e ->
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = e.message ?: "获取每日便笺失败",
                            )
                        }
                }
                SupportedGame.STAR_RAIL -> {
                    dailyNoteRepository.fetchDailyNoteHSR(account)
                        .onSuccess { note ->
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                dailyNoteGI = null,
                                dailyNoteHSR = note,
                                dailyNoteZZZ = null,
                                error = null,
                            )
                        }
                        .onFailure { e ->
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = e.message ?: "获取每日便笺失败",
                            )
                        }
                }
                SupportedGame.ZENLESS_ZONE -> {
                    dailyNoteRepository.fetchDailyNoteZZZ(account)
                        .onSuccess { note ->
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                dailyNoteGI = null,
                                dailyNoteHSR = null,
                                dailyNoteZZZ = note,
                                error = null,
                            )
                        }
                        .onFailure { e ->
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = e.message ?: "获取每日便笺失败",
                            )
                        }
                }
            }
        }
    }
}

data class DailyNoteUiState(
    val isLoading: Boolean = false,
    val accounts: List<HoYoAccount> = emptyList(),
    val selectedAccount: HoYoAccount? = null,
    val dailyNoteGI: DailyNoteGI? = null,
    val dailyNoteHSR: DailyNoteHSR? = null,
    val dailyNoteZZZ: DailyNoteZZZ? = null,
    val error: String? = null,
)
