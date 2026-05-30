package com.hyalurion.lumi.helper.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

/**
 * Default daily note widget (backward compatible).
 * Uses MEDIUM size layout.
 * For explicit sizing, use SmallDailyNoteWidget, MediumDailyNoteWidget, or LargeDailyNoteWidget.
 */
class DailyNoteWidget : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            DailyNoteWidgetContent(size = WidgetSize.MEDIUM)
        }
    }

    companion object {
        val KEY_UID = stringPreferencesKey("uid")
        val KEY_CURRENT = intPreferencesKey("current")
        val KEY_MAX = intPreferencesKey("max")
        val KEY_LABEL = stringPreferencesKey("label")
        val KEY_UPDATED_AT = longPreferencesKey("updated_at")
    }
}
