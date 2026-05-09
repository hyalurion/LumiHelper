package com.chronie.helperunited.widget

import android.content.Context
import android.content.SharedPreferences
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.datastore.preferences.core.stringPreferencesKey

/**
 * Widget configuration preferences stored in SharedPreferences.
 * Each widget instance stores its configured game and account UUID.
 */
object WidgetPrefs {

    private const val PREFS_NAME = "widget_prefs"

    // Keys for Glance state (per-widget via DataStore)
    val KEY_WIDGET_GAME = stringPreferencesKey("widget_game")
    val KEY_WIDGET_ACCOUNT_UUID = stringPreferencesKey("widget_account_uuid")

    fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Save widget configuration to Glance state.
     */
    suspend fun saveWidgetConfig(
        context: Context,
        glanceId: GlanceId,
        gameRawValue: String,
        accountUuid: String,
    ) {
        updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { store ->
            store.toMutablePreferences().apply {
                this[KEY_WIDGET_GAME] = gameRawValue
                this[KEY_WIDGET_ACCOUNT_UUID] = accountUuid
            }
        }
    }

    /**
     * Mark widget as configured (so we know not to show config prompt again).
     */
    fun setConfigured(context: Context, appWidgetId: Int, configured: Boolean) {
        prefs(context).edit()
            .putBoolean("configured_$appWidgetId", configured)
            .apply()
    }

    fun isConfigured(context: Context, appWidgetId: Int): Boolean {
        return prefs(context).getBoolean("configured_$appWidgetId", false)
    }
}
