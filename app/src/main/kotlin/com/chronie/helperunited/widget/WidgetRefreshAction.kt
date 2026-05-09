package com.chronie.helperunited.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback

/**
 * Action callback for tap-to-refresh on widgets.
 * When the user taps the widget, it triggers a data re-fetch.
 */
class WidgetRefreshAction : ActionCallback {

    companion object {
        val PARAM_GLANCE_ID = ActionParameters.Key<String>("glance_id")
    }

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        // Re-trigger the widget to fetch fresh data
        // This causes provideGlance to be called again
        DailyNoteWidgetWorker.enqueueFetch(context, glanceId)
    }
}
