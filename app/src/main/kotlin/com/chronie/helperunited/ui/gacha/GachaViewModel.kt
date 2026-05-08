package com.chronie.helperunited.ui.gacha

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chronie.helperunited.data.GachaRepository
import com.chronie.helperunited.model.GachaEntry
import com.chronie.helperunited.model.GachaProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Gacha screen.
 * Manages gacha record queries and state.
 */
@HiltViewModel
class GachaViewModel @Inject constructor(
    private val gachaRepository: GachaRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(GachaUiState())
    val uiState: StateFlow<GachaUiState> = _uiState.asStateFlow()

    init {
        loadProfiles()
    }

    /**
     * Load all saved gacha profiles.
     */
    private fun loadProfiles() {
        viewModelScope.launch {
            val profiles = gachaRepository.getAllProfiles()
            _uiState.value = _uiState.value.copy(profiles = profiles)
        }
    }

    /**
     * Fetch gacha records for a UID and game.
     * Note: This requires an auth key from the game's web cache.
     */
    fun fetchGachaRecords(uid: String, game: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            // For now, just load from local database
            // TODO: Implement actual gacha API fetching
            try {
                val entries = gachaRepository.getEntries(uid, game)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    entries = entries,
                    selectedProfile = GachaProfile(uid, game),
                    error = if (entries.isEmpty()) "没有找到抽卡记录" else null,
                )
                loadProfiles()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "获取抽卡记录失败",
                )
            }
        }
    }

    /**
     * Select a profile to view.
     */
    fun selectProfile(profile: GachaProfile) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val entries = gachaRepository.getEntries(profile.uid, profile.game)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                entries = entries,
                selectedProfile = profile,
            )
        }
    }

    /**
     * Import gacha records from UIGF JSON.
     */
    fun importUIGF(json: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            gachaRepository.importUIGF(json)
                .onSuccess { count ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = null,
                    )
                    loadProfiles()
                    // Reload entries if a profile is selected
                    _uiState.value.selectedProfile?.let { selectProfile(it) }
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "导入失败: ${exception.message}",
                    )
                }
        }
    }

    /**
     * Export gacha records to UIGF JSON.
     */
    fun exportUIGF(uid: String, game: String): String? {
        // This should be called from a coroutine
        return null
    }

    /**
     * Delete all entries for a profile.
     */
    fun deleteProfile(profile: GachaProfile) {
        viewModelScope.launch {
            gachaRepository.deleteAll(profile.uid, profile.game)
            loadProfiles()
            if (_uiState.value.selectedProfile == profile) {
                _uiState.value = _uiState.value.copy(
                    entries = emptyList(),
                    selectedProfile = null,
                )
            }
        }
    }
}

/**
 * UI state for the Gacha screen.
 */
data class GachaUiState(
    val isLoading: Boolean = false,
    val profiles: List<GachaProfile> = emptyList(),
    val entries: List<GachaEntry> = emptyList(),
    val selectedProfile: GachaProfile? = null,
    val error: String? = null,
)
