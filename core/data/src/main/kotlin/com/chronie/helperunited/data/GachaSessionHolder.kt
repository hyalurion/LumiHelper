package com.chronie.helperunited.data

import com.chronie.helperunited.model.GachaProfile
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Holds the currently selected gacha profile in memory.
 * Used to pass context between GachaScreen and GachaAnalyticsScreen.
 */
@Singleton
class GachaSessionHolder @Inject constructor() {
    var currentProfile: GachaProfile? = null
        private set

    fun setProfile(profile: GachaProfile) {
        currentProfile = profile
    }
}
