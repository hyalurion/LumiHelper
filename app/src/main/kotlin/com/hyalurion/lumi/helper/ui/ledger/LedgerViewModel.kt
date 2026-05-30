package com.hyalurion.lumi.helper.ui.ledger

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hyalurion.lumi.helper.data.AccountRepository
import com.hyalurion.lumi.helper.data.ChallengeRepository
import com.hyalurion.lumi.helper.model.HoYoAccount
import com.hyalurion.lumi.helper.model.LedgerData4GI
import com.hyalurion.lumi.helper.model.LedgerData4HSR
import com.hyalurion.lumi.helper.base.SupportedGame
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class LedgerUiState(
    val accounts: List<HoYoAccount> = emptyList(),
    val selectedAccount: HoYoAccount? = null,
    val isLoading: Boolean = false,
    val ledgerGI: LedgerData4GI? = null,
    val ledgerHSR: LedgerData4HSR? = null,
    val error: String? = null,
    val selectedMonth: Int = Calendar.getInstance().get(Calendar.MONTH) + 1,
    val selectedYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    val availableMonths: List<Int> = emptyList(),
)

@HiltViewModel
class LedgerViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val challengeRepository: ChallengeRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LedgerUiState())
    val uiState: StateFlow<LedgerUiState> = _uiState.asStateFlow()

    init {
        loadAccounts()
    }

    private fun loadAccounts() {
        viewModelScope.launch {
            accountRepository.observeAllAccounts().collect { accounts ->
                _uiState.value = _uiState.value.copy(accounts = accounts)
                if (_uiState.value.selectedAccount == null && accounts.isNotEmpty()) {
                    selectAccount(accounts.first())
                }
            }
        }
    }

    fun selectAccount(account: HoYoAccount) {
        _uiState.value = _uiState.value.copy(
            selectedAccount = account,
            ledgerGI = null,
            ledgerHSR = null,
            error = null,
            availableMonths = emptyList(),
        )
        fetchLedger(account, _uiState.value.selectedMonth)
    }

    fun selectMonthAndYear(year: Int, month: Int) {
        _uiState.value = _uiState.value.copy(selectedYear = year, selectedMonth = month)
        _uiState.value.selectedAccount?.let { account ->
            fetchLedger(account, month)
        }
    }

    fun previousMonth() {
        val state = _uiState.value
        var newMonth = state.selectedMonth - 1
        var newYear = state.selectedYear
        if (newMonth < 1) {
            newMonth = 12
            newYear -= 1
        }
        selectMonthAndYear(newYear, newMonth)
    }

    fun nextMonth() {
        val state = _uiState.value
        val now = Calendar.getInstance()
        val currentYearMonth = now.get(Calendar.YEAR) * 100 + now.get(Calendar.MONTH) + 1
        val nextYearMonth = state.selectedYear * 100 + (state.selectedMonth + 1)
        if (nextYearMonth > currentYearMonth) return
        var newMonth = state.selectedMonth + 1
        var newYear = state.selectedYear
        if (newMonth > 12) {
            newMonth = 1
            newYear += 1
        }
        selectMonthAndYear(newYear, newMonth)
    }

    fun refresh() {
        _uiState.value.selectedAccount?.let { account ->
            fetchLedger(account, _uiState.value.selectedMonth)
        }
    }

    private fun fetchLedger(account: HoYoAccount, month: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            when (account.game) {
                SupportedGame.GENSHIN_IMPACT -> {
                    challengeRepository.fetchLedgerGI(account)
                        .onSuccess { data ->
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                ledgerGI = data,
                                ledgerHSR = null,
                                availableMonths = data.optionalMonth.sortedDescending(),
                            )
                        }
                        .onFailure { e ->
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = e.message ?: "Failed to fetch ledger data",
                            )
                        }
                }
                SupportedGame.STAR_RAIL -> {
                    challengeRepository.fetchLedgerHSR(account)
                        .onSuccess { data ->
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                ledgerHSR = data,
                                ledgerGI = null,
                                availableMonths = data.optionalMonth
                                    .mapNotNull { it.toIntOrNull() }
                                    .sortedDescending(),
                            )
                        }
                        .onFailure { e ->
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = e.message ?: "Failed to fetch ledger data",
                            )
                        }
                }
                else -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Ledger is only available for Genshin Impact and Honkai: Star Rail",
                    )
                }
            }
        }
    }
}