package com.hyalurion.lumi.helper.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

/**
 * Widget receiver for the Small (2x1) Daily Note widget.
 */
class SmallDailyNoteWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = SmallDailyNoteWidget()
}
