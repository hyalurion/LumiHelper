package com.chronie.helperunited.ui.gacha

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chronie.helperunited.data.GachaRepository
import com.chronie.helperunited.data.UIGFManager
import com.chronie.helperunited.model.GachaEntry
import com.chronie.helperunited.model.GachaProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Gacha screen.
 * Manages gacha record queries, import/export, and state.
 * Ported from PizzaHelperUnited's GachaKit.
 */
@HiltViewModel
class GachaViewModel @Inject constructor(
    private val gachaRepository: GachaRepository,
    private val uigfManager: UIGFManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(GachaUiState())
    val uiState: StateFlow<GachaUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<GachaEvent>()
    val events: SharedFlow<GachaEvent> = _events.asSharedFlow()

    init {
        loadProfiles()
    }

    private fun loadProfiles() {
        viewModelScope.launch {
            val profiles = gachaRepository.getAllProfiles()
            _uiState.value = _uiState.value.copy(profiles = profiles)
        }
    }

    /**
     * Fetch gacha records for a UID and game.
     */
    fun fetchGachaRecords(uid: String, game: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val entries = gachaRepository.getEntries(uid, game)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    entries = entries,
                    selectedProfile = GachaProfile(uid, game),
                    error = if (entries.isEmpty()) "没有找到抽卡记录，请先导入或获取" else null,
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
     * Import gacha records from UIGF JSON string.
     */
    fun importUIGF(json: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            uigfManager.importFromJson(json)
                .onSuccess { count ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = null,
                    )
                    _events.emit(GachaEvent.ShowMessage("成功导入 $count 条记录"))
                    loadProfiles()
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
     * Import gacha records from a content URI (file picker result).
     */
    fun importFromUri(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            uigfManager.importFromUri(uri)
                .onSuccess { count ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = null)
                    _events.emit(GachaEvent.ShowMessage("成功导入 $count 条记录"))
                    loadProfiles()
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
     * Export gacha records and trigger share intent.
     */
    fun exportUIGF(uid: String, game: String) {
        viewModelScope.launch {
            try {
                val intent = uigfManager.createShareIntent(uid, game)
                _events.emit(GachaEvent.ShareExport(intent))
            } catch (e: Exception) {
                _events.emit(GachaEvent.ShowMessage("导出失败: ${e.message}"))
            }
        }
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

    /**
     * Get statistics for the current entries.
     */
    fun getStatistics(): GachaStatistics {
        val entries = _uiState.value.entries
        return GachaStatistics.fromEntries(entries)
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

/**
 * One-shot events from the Gacha ViewModel.
 */
sealed class GachaEvent {
    data class ShowMessage(val message: String) : GachaEvent()
    data class ShareExport(val intent: Intent) : GachaEvent()
}

/**
 * Gacha statistics.
 */
data class GachaStatistics(
    val totalPulls: Int = 0,
    val fiveStarCount: Int = 0,
    val fourStarCount: Int = 0,
    val threeStarCount: Int = 0,
    val fiveStarRate: Double = 0.0,
    val fourStarRate: Double = 0.0,
    val pityCount: Int = 0, // Pulls since last 5-star
) {
    companion object {
        fun fromEntries(entries: List<GachaEntry>): GachaStatistics {
            val total = entries.size
            val fiveStar = entries.count { it.rankType == 5 }
            val fourStar = entries.count { it.rankType == 4 }
            val threeStar = entries.count { it.rankType == 3 }

            // Calculate pity (pulls since last 5-star)
            val lastFiveStarIndex = entries.indexOfFirst { it.rankType == 5 }
            val pity = if (lastFiveStarIndex >= 0) lastFiveStarIndex else total

            return GachaStatistics(
                totalPulls = total,
                fiveStarCount = fiveStar,
                fourStarCount = fourStar,
                threeStarCount = threeStar,
                fiveStarRate = if (total > 0) fiveStar * 100.0 / total else 0.0,
                fourStarRate = if (total > 0) fourStar * 100.0 / total else 0.0,
                pityCount = pity,
            )
        }
    }
}
