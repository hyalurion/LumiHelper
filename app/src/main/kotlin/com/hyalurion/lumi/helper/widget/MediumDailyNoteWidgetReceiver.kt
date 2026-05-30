package com.hyalurion.lumi.helper.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

/**
 * Widget receiver for the Medium (4x2) Daily Note widget.
 */
class MediumDailyNoteWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MediumDailyNoteWidget()
}
