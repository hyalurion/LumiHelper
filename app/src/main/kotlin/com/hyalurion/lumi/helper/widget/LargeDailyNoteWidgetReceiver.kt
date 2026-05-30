package com.hyalurion.lumi.helper.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

/**
 * Widget receiver for the Large (4x4) Daily Note widget.
 */
class LargeDailyNoteWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = LargeDailyNoteWidget()
}
