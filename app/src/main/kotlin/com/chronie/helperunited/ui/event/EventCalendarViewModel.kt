package com.chronie.helperunited.ui.event

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chronie.helperunited.data.EventRepository
import com.chronie.helperunited.model.GameEvent
import com.chronie.helperunited.model.VersionInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Event Calendar screen.
 * Manages event data, filtering, and version countdowns.
 */
@HiltViewModel
class EventCalendarViewModel @Inject constructor(
    private val eventRepository: EventRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(EventCalendarUiState())
    val uiState: StateFlow<EventCalendarUiState> = _uiState.asStateFlow()

    init {
        observeData()
        refresh()
    }

    private fun observeData() {
        viewModelScope.launch {
            combine(
                eventRepository.events,
                eventRepository.versionInfo,
                eventRepository.isLoading,
                eventRepository.error,
            ) { events, versions, loading, error ->
                EventCalendarUiState(
                    events = events,
                    versionInfo = versions,
                    isLoading = loading,
                    error = error,
                )
            }.collectLatest { state ->
                _uiState.value = state
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            eventRepository.refresh()
        }
    }

    fun setGameFilter(game: String?) {
        _uiState.value = _uiState.value.copy(selectedGame = game)
    }

    /**
     * Get filtered events based on selected game.
     */
    fun getFilteredEvents(): List<GameEvent> {
        val state = _uiState.value
        val game = state.selectedGame
        return if (game == null) {
            state.events
        } else {
            state.events.filter { it.game == game }
        }
    }

    /**
     * Get active events.
     */
    fun getActiveEvents(): List<GameEvent> {
        return getFilteredEvents().filter { it.isActive }
    }

    /**
     * Get upcoming events.
     */
    fun getUpcomingEvents(): List<GameEvent> {
        return getFilteredEvents().filter { it.isUpcoming }
    }

    /**
     * Get ended events (recent).
     */
    fun getEndedEvents(): List<GameEvent> {
        return getFilteredEvents().filter { it.hasEnded }.takeLast(10)
    }

    /**
     * Get version info for filtered games.
     */
    fun getFilteredVersionInfo(): List<VersionInfo> {
        val state = _uiState.value
        val game = state.selectedGame
        return if (game == null) {
            state.versionInfo.filter { it.isActive }
        } else {
            state.versionInfo.filter { it.game == game && it.isActive }
        }
    }
}

data class EventCalendarUiState(
    val events: List<GameEvent> = emptyList(),
    val versionInfo: List<VersionInfo> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedGame: String? = null,  // null = all games
)
