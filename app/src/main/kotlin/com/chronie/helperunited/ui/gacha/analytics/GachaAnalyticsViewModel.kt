package com.chronie.helperunited.ui.gacha.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chronie.helperunited.data.GachaAnalytics
import com.chronie.helperunited.data.GachaRepository
import com.chronie.helperunited.data.GachaSessionHolder
import com.chronie.helperunited.model.GachaProfile
import com.chronie.helperunited.model.GachaTypeAnalytics
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Gacha Analytics screen.
 * Loads entries and computes per-type chart analytics.
 */
@HiltViewModel
class GachaAnalyticsViewModel @Inject constructor(
    private val gachaRepository: GachaRepository,
    private val gachaAnalytics: GachaAnalytics,
    private val sessionHolder: GachaSessionHolder,
) : ViewModel() {

    private val _uiState = MutableStateFlow(GachaAnalyticsUiState())
    val uiState: StateFlow<GachaAnalyticsUiState> = _uiState.asStateFlow()

    init {
        // Auto-load from the current session profile
        sessionHolder.currentProfile?.let { loadAnalytics(it) }
            ?: run {
                _uiState.value = _uiState.value.copy(
                    error = "请先在抽卡记录页面选择一个账号",
                )
            }
    }

    /**
     * Load analytics for a given profile.
     */
    fun loadAnalytics(profile: GachaProfile) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val entries = gachaRepository.getEntries(profile.uid, profile.game)
                if (entries.isEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "没有抽卡记录，请先导入",
                    )
                    return@launch
                }

                val analytics = gachaAnalytics.computeAnalytics(entries, profile.game)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    profile = profile,
                    allAnalytics = analytics,
                    selectedTypeIndex = 0,
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "加载分析数据失败: ${e.message}",
                )
            }
        }
    }

    /**
     * Switch to a different gacha type tab.
     */
    fun selectType(index: Int) {
        _uiState.value = _uiState.value.copy(selectedTypeIndex = index)
    }
}

/**
 * UI state for the Gacha Analytics screen.
 */
data class GachaAnalyticsUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val profile: GachaProfile? = null,
    val allAnalytics: List<GachaTypeAnalytics> = emptyList(),
    val selectedTypeIndex: Int = 0,
) {
    val selectedAnalytics: GachaTypeAnalytics?
        get() = allAnalytics.getOrNull(selectedTypeIndex)
}
