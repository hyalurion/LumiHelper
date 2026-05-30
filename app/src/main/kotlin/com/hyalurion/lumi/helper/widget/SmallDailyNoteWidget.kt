package com.hyalurion.lumi.helper.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition

/**
 * Small widget (2x1): Shows just the resource number and progress bar.
 */
class SmallDailyNoteWidget : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            DailyNoteWidgetContent(size = WidgetSize.SMALL)
        }
    }
}
