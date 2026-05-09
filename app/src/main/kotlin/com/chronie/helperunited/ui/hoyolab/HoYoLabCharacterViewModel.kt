package com.chronie.helperunited.ui.hoyolab

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chronie.helperunited.base.SupportedGame
import com.chronie.helperunited.data.AccountRepository
import com.chronie.helperunited.data.HoYoLabCharacterRepository
import com.chronie.helperunited.data.SortOption
import com.chronie.helperunited.model.HoYoAccount
import com.chronie.helperunited.model.HoYoLabCharacterDisplay
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the HoYoLAB character list screen.
 * Manages fetching, caching, filtering, and sorting of characters.
 */
@HiltViewModel
class HoYoLabCharacterViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val characterRepository: HoYoLabCharacterRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HoYoLabCharacterUiState())
    val uiState: StateFlow<HoYoLabCharacterUiState> = _uiState.asStateFlow()

    init {
        loadAccounts()
    }

    private fun loadAccounts() {
        viewModelScope.launch {
            accountRepository.observeAllAccounts().collect { accounts ->
                _uiState.value = _uiState.value.copy(accounts = accounts)
                // Auto-select the first account if none selected
                if (_uiState.value.selectedAccount == null && accounts.isNotEmpty()) {
                    selectAccount(accounts.first())
                }
            }
        }
    }

    fun selectAccount(account: HoYoAccount) {
        _uiState.value = _uiState.value.copy(
            selectedAccount = account,
            selectedElement = null,
            selectedRarity = null,
        )
        fetchCharacters(account)
    }

    fun refresh() {
        val account = _uiState.value.selectedAccount ?: return
        fetchCharacters(account)
    }

    fun filterByElement(element: String?) {
        val current = _uiState.value
        val newElement = if (current.selectedElement == element) null else element
        _uiState.value = current.copy(selectedElement = newElement)
        applyFiltersAndSort()
    }

    fun filterByRarity(rarity: Int?) {
        val current = _uiState.value
        val newRarity = if (current.selectedRarity == rarity) null else rarity
        _uiState.value = current.copy(selectedRarity = newRarity)
        applyFiltersAndSort()
    }

    fun setSortOption(sortOption: SortOption) {
        _uiState.value = _uiState.value.copy(sortOption = sortOption)
        applyFiltersAndSort()
    }

    private fun fetchCharacters(account: HoYoAccount) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val result = when (account.game) {
                SupportedGame.GENSHIN_IMPACT -> characterRepository.fetchCharactersGI(account)
                SupportedGame.STAR_RAIL -> characterRepository.fetchCharactersHSR(account)
                SupportedGame.ZENLESS_ZONE -> {
                    // ZZZ character list not yet supported
                    Result.failure(Exception("绝区零角色列表暂不支持"))
                }
            }

            result
                .onSuccess { characters ->
                    val elements = characters.map { it.element }.distinct().filter { it.isNotBlank() }
                    val rarities = characters.map { it.rarity }.distinct().sortedDescending()

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        allCharacters = characters,
                        availableElements = elements,
                        availableRarities = rarities,
                        error = null,
                    )
                    applyFiltersAndSort()
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "获取角色列表失败",
                    )
                }
        }
    }

    private fun applyFiltersAndSort() {
        val state = _uiState.value
        var list = state.allCharacters

        // Filter by element
        if (state.selectedElement != null) {
            list = list.filter { it.element.equals(state.selectedElement, ignoreCase = true) }
        }

        // Filter by rarity
        if (state.selectedRarity != null) {
            list = list.filter { it.rarity == state.selectedRarity }
        }

        // Sort
        list = when (state.sortOption) {
            SortOption.LEVEL -> list.sortedWith(
                compareByDescending<HoYoLabCharacterDisplay> { it.level }
                    .thenByDescending { it.rarity }
            )
            SortOption.CONSTELLATION -> list.sortedWith(
                compareByDescending<HoYoLabCharacterDisplay> { it.constellation }
                    .thenByDescending { it.level }
            )
            SortOption.FRIENDSHIP -> list.sortedWith(
                compareByDescending<HoYoLabCharacterDisplay> { it.friendship }
                    .thenByDescending { it.level }
            )
            SortOption.RARITY -> list.sortedWith(
                compareByDescending<HoYoLabCharacterDisplay> { it.rarity }
                    .thenByDescending { it.level }
            )
        }

        _uiState.value = _uiState.value.copy(filteredCharacters = list)
    }
}

data class HoYoLabCharacterUiState(
    val isLoading: Boolean = false,
    val accounts: List<HoYoAccount> = emptyList(),
    val selectedAccount: HoYoAccount? = null,
    val allCharacters: List<HoYoLabCharacterDisplay> = emptyList(),
    val filteredCharacters: List<HoYoLabCharacterDisplay> = emptyList(),
    val availableElements: List<String> = emptyList(),
    val availableRarities: List<Int> = emptyList(),
    val selectedElement: String? = null,
    val selectedRarity: Int? = null,
    val sortOption: SortOption = SortOption.LEVEL,
    val error: String? = null,
)
