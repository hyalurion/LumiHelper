package com.chronie.helperunited.ui.sign

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chronie.helperunited.data.AccountRepository
import com.chronie.helperunited.data.HoYoLabSignRepository
import com.chronie.helperunited.model.GameSignStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for HoYoLAB auto sign-in feature.
 */
@HiltViewModel
class SignViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val signRepository: HoYoLabSignRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignUiState())
    val uiState: StateFlow<SignUiState> = _uiState.asStateFlow()

    init {
        loadSignStatus()
    }

    /**
     * Load sign-in status for all accounts (without signing in).
     */
    fun loadSignStatus() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val accounts = accountRepository.getAllAccounts()
            if (accounts.isEmpty()) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "请先添加账号",
                )
                return@launch
            }

            val statuses = signRepository.getAllSignStatuses(accounts)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                signStatuses = statuses,
            )
        }
    }

    /**
     * Sign in for all games at once.
     */
    fun signInAll() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSigning = true, error = null)

            val accounts = accountRepository.getAllAccounts()
            if (accounts.isEmpty()) {
                _uiState.value = _uiState.value.copy(
                    isSigning = false,
                    error = "请先添加账号",
                )
                return@launch
            }

            val statuses = signRepository.signInAll(accounts)
            _uiState.value = _uiState.value.copy(
                isSigning = false,
                signStatuses = statuses,
            )
        }
    }
}

data class SignUiState(
    val isLoading: Boolean = false,
    val isSigning: Boolean = false,
    val signStatuses: List<GameSignStatus> = emptyList(),
    val error: String? = null,
)
