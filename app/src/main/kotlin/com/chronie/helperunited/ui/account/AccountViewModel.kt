package com.chronie.helperunited.ui.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chronie.helperunited.base.AccountRegion
import com.chronie.helperunited.base.RegionType
import com.chronie.helperunited.base.SupportedGame
import com.chronie.helperunited.data.AccountRepository
import com.chronie.helperunited.model.HoYoAccount
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * ViewModel for account management.
 * Handles adding, editing, and deleting HoYoverse accounts.
 * Ported from PizzaHelperUnited's account management logic.
 */
@HiltViewModel
class AccountViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AccountUiState())
    val uiState: StateFlow<AccountUiState> = _uiState.asStateFlow()

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

    /**
     * Add a new account.
     * @param uid Game UID
     * @param game Supported game
     * @param cookie HoYoLAB/Miyoushe cookie
     * @param name Optional display name
     * @param isGlobal Whether this is a HoYoLab (global) or Miyoushe (CN) account
     */
    fun addAccount(
        uid: String,
        game: SupportedGame,
        cookie: String,
        name: String = "",
        isGlobal: Boolean = true,
    ) {
        viewModelScope.launch {
            val region = if (isGlobal) {
                AccountRegion(RegionType.HOYOLAB, game)
            } else {
                AccountRegion(RegionType.MIYOUSHE, game)
            }
            val account = HoYoAccount(
                uuid = UUID.randomUUID().toString(),
                uid = uid,
                name = name.ifBlank { "${game.shortName} · $uid" },
                region = region,
                cookie = cookie,
            )
            accountRepository.addAccount(account)
            _uiState.value = _uiState.value.copy(showAddDialog = false)
        }
    }

    /**
     * Delete an account.
     */
    fun deleteAccount(uuid: String) {
        viewModelScope.launch {
            accountRepository.deleteAccount(uuid)
        }
    }

    /**
     * Update account name.
     */
    fun updateAccountName(uuid: String, newName: String) {
        viewModelScope.launch {
            val account = accountRepository.getAccount(uuid) ?: return@launch
            accountRepository.updateAccount(account.copy(name = newName))
        }
    }

    fun setShowAddDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showAddDialog = show)
    }
}

data class AccountUiState(
    val accounts: List<HoYoAccount> = emptyList(),
    val showAddDialog: Boolean = false,
)
