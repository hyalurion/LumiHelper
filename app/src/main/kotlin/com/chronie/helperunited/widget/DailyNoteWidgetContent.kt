package com.chronie.helperunited.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.components.Scaffold
import androidx.glance.appwidget.components.Title
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey

/**
 * Widget content composable.
 * Displays a compact daily note card on the home screen.
 */
@Composable
fun DailyNoteWidgetContent() {
    val prefs = currentState<Preferences>()
    val game = prefs[stringPreferencesKey("game")] ?: "gi"
    val uid = prefs[stringPreferencesKey("uid")] ?: "---"
    val current = prefs[intPreferencesKey("current")] ?: 0
    val max = prefs[intPreferencesKey("max")] ?: 0
    val label = prefs[stringPreferencesKey("label")] ?: "树脂"
    val updatedAt = prefs[longPreferencesKey("updated_at")] ?: 0L

    val isFull = current >= max && max > 0
    val progress = if (max > 0) current.toFloat() / max else 0f

    GlanceTheme {
        Scaffold(
            titleBar = {
                Title(
                    title = when (game) {
                        "gi" -> "⚡ 原粹树脂"
                        "hsr" -> "⚡ 开拓力"
                        "zzz" -> "⚡ 电量"
                        else -> label
                    },
                )
            },
        ) {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Main value
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Text(
                        text = "$current",
                        style = TextStyle(
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = GlanceTheme.colors.onSurface,
                        ),
                    )
                    Spacer(modifier = GlanceModifier.width(4.dp))
                    Text(
                        text = "/ $max",
                        style = TextStyle(
                            fontSize = 16.sp,
                            color = GlanceTheme.colors.onSurfaceVariant,
                        ),
                    )
                }

                Spacer(modifier = GlanceModifier.height(8.dp))

                // Status
                Text(
                    text = if (isFull) "✅ 已满！" else "恢复中...",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = if (isFull) FontWeight.Bold else FontWeight.Normal,
                        color = if (isFull) GlanceTheme.colors.primary
                        else GlanceTheme.colors.onSurfaceVariant,
                    ),
                )

                Spacer(modifier = GlanceModifier.height(4.dp))

                // UID and update time
                Text(
                    text = "UID: $uid",
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = GlanceTheme.colors.onSurfaceVariant,
                    ),
                )
            }
        }
    }
}
