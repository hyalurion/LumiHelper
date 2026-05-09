package com.chronie.helperunited.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.update.UpdateReason
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

/**
 * Daily Note widget for Android home screen.
 * Shows real-time resin/stamina/energy data.
 * Ported from PizzaHelperUnited's PZWidgetsKit.
 */
class DailyNoteWidget : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            DailyNoteWidgetContent()
        }
    }

    companion object {
        val KEY_GAME = stringPreferencesKey("game")
        val KEY_UID = stringPreferencesKey("uid")
        val KEY_CURRENT = intPreferencesKey("current")
        val KEY_MAX = intPreferencesKey("max")
        val KEY_LABEL = stringPreferencesKey("label")
        val KEY_UPDATED_AT = longPreferencesKey("updated_at")
    }
}
