package com.chronie.helperunited.ui.enka

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chronie.helperunited.data.EnkaRepository
import com.chronie.helperunited.model.EnkaProfileSummarized
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Enka showcase screen.
 * Manages the state of character showcase queries.
 */
@HiltViewModel
class EnkaShowcaseViewModel @Inject constructor(
    private val enkaRepository: EnkaRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(EnkaShowcaseUiState())
    val uiState: StateFlow<EnkaShowcaseUiState> = _uiState.asStateFlow()

    /**
     * Query character showcase for a given UID.
     */
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
                        error = exception.message ?: "查询失败",
                    )
                }
        }
    }

    /**
     * Clear the current showcase data.
     */
    fun clearShowcase() {
        _uiState.value = EnkaShowcaseUiState()
    }

    /**
     * Get list of cached UIDs.
     */
    fun getCachedUids(): List<String> {
        return enkaRepository.getCachedUids()
    }
}

/**
 * UI state for the Enka showcase screen.
 */
data class EnkaShowcaseUiState(
    val isLoading: Boolean = false,
    val profile: EnkaProfileSummarized? = null,
    val error: String? = null,
)
