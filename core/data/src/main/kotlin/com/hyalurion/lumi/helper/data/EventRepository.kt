package com.hyalurion.lumi.helper.data

import com.hyalurion.lumi.helper.model.GameEvent
import com.hyalurion.lumi.helper.model.VersionInfo
import com.hyalurion.lumi.helper.network.EventDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for game events and version info.
 * Combines API data (HoYoLab news) with bundled fallback data.
 */
@Singleton
class EventRepository @Inject constructor(
    private val eventDataSource: EventDataSource,
) {
    private val _events = MutableStateFlow<List<GameEvent>>(emptyList())
    private val _versionInfo = MutableStateFlow<List<VersionInfo>>(emptyList())
    private val _isLoading = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)

    /** All loaded events. */
    val events: Flow<List<GameEvent>> = _events.asStateFlow()

    /** All version info. */
    val versionInfo: Flow<List<VersionInfo>> = _versionInfo.asStateFlow()

    /** Loading state. */
    val isLoading: Flow<Boolean> = _isLoading.asStateFlow()

    /** Error message, if any. */
    val error: Flow<String?> = _error.asStateFlow()

    /**
     * Refresh events from API, falling back to bundled data.
     */
    suspend fun refresh() {
        _isLoading.value = true
        _error.value = null

        // Load bundled version info (always available)
        val bundledVersions = eventDataSource.loadBundledVersionInfo()
        _versionInfo.value = bundledVersions

        // Try API first
        val apiResult = eventDataSource.fetchEventsFromApi()
        apiResult.fold(
            onSuccess = { apiEvents ->
                // Merge with bundled events
                val bundledEvents = eventDataSource.loadBundledEvents()
                val merged = (apiEvents + bundledEvents).distinctBy { it.id }
                _events.value = merged.sortedBy { it.startEpoch }
                _isLoading.value = false
            },
            onFailure = { e ->
                // Fall back to bundled data
                val bundledEvents = eventDataSource.loadBundledEvents()
                _events.value = bundledEvents.sortedBy { it.startEpoch }
                _error.value = if (bundledEvents.isEmpty()) {
                    "无法获取活动数据: ${e.message}"
                } else {
                    null // Bundled data available, suppress error
                }
                _isLoading.value = false
            }
        )
    }

    /**
     * Get currently active events, optionally filtered by game.
     */
    fun getActiveEvents(game: String? = null): Flow<List<GameEvent>> {
        return _events.map { events ->
            events.filter { event ->
                event.isActive && (game == null || event.game == game)
            }
        }
    }

    /**
     * Get upcoming events, optionally filtered by game.
     */
    fun getUpcomingEvents(game: String? = null): Flow<List<GameEvent>> {
        return _events.map { events ->
            events.filter { event ->
                event.isUpcoming && (game == null || event.game == game)
            }
        }
    }

    /**
     * Get version info for a specific game.
     */
    fun getVersionInfo(game: String): Flow<VersionInfo?> {
        return _versionInfo.map { versions ->
            versions
                .filter { it.game == game && it.isActive }
                .maxByOrNull { it.startEpoch }
        }
    }

    /**
     * Get all active version info.
     */
    fun getActiveVersionInfo(): Flow<List<VersionInfo>> {
        return _versionInfo.map { versions ->
            versions.filter { it.isActive }
        }
    }
}
