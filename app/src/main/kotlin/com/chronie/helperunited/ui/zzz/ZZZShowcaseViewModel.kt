package com.chronie.helperunited.ui.zzz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chronie.helperunited.data.ZZZShowcaseRepository
import com.chronie.helperunited.model.HoYoAccount
import com.chronie.helperunited.model.ZZZProfileSummarized
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the ZZZ agent showcase screen.
 * Manages fetching and displaying ZZZ agent data.
 */
@HiltViewModel
class ZZZShowcaseViewModel @Inject constructor(
    private val zzzShowcaseRepository: ZZZShowcaseRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ZZZShowcaseUiState())
    val uiState: StateFlow<ZZZShowcaseUiState> = _uiState.asStateFlow()

    /**
     * Fetch ZZZ agent showcase for a given account.
     */
    fun fetchShowcase(account: HoYoAccount) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            zzzShowcaseRepository.fetchShowcase(account)
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
                        error = exception.message ?: "查询失败",
                    )
                }
        }
    }

    /**
     * Clear current data.
     */
    fun clearShowcase() {
        _uiState.value = ZZZShowcaseUiState()
    }
}

/**
 * UI state for the ZZZ showcase screen.
 */
data class ZZZShowcaseUiState(
    val isLoading: Boolean = false,
    val profile: ZZZProfileSummarized? = null,
    val error: String? = null,
)
