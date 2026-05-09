package com.chronie.helperunited.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionRunCallback
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
import com.chronie.helperunited.base.SupportedGame

/**
 * Size-aware widget content composable.
 * Renders different layouts based on the widget size category.
 */
@Composable
fun DailyNoteWidgetContent(size: WidgetSize = WidgetSize.MEDIUM) {
    val prefs = currentState<Preferences>()
    val game = prefs[stringPreferencesKey("game")] ?: prefs[WidgetPrefs.KEY_WIDGET_GAME] ?: "gi"
    val uid = prefs[DailyNoteWidget.KEY_UID] ?: "---"
    val current = prefs[DailyNoteWidget.KEY_CURRENT] ?: 0
    val max = prefs[DailyNoteWidget.KEY_MAX] ?: 0
    val label = prefs[DailyNoteWidget.KEY_LABEL] ?: getResourceLabel(game)
    val updatedAt = prefs[DailyNoteWidget.KEY_UPDATED_AT] ?: 0L
    val errorMsg = prefs[DailyNoteWidgetWorker.KEY_ERROR_MSG]

    val isFull = current >= max && max > 0
    val progress = if (max > 0) current.toFloat() / max else 0f

    // Wrap entire widget in clickable for tap-to-refresh
    val tapModifier = GlanceModifier.clickable(
        actionRunCallback<WidgetRefreshAction>()
    )

    when (size) {
        WidgetSize.SMALL -> SmallContent(
            game = game,
            current = current,
            max = max,
            isFull = isFull,
            progress = progress,
            errorMsg = errorMsg,
            tapModifier = tapModifier,
        )
        WidgetSize.MEDIUM -> MediumContent(
            game = game,
            uid = uid,
            current = current,
            max = max,
            isFull = isFull,
            progress = progress,
            prefs = prefs,
            errorMsg = errorMsg,
            tapModifier = tapModifier,
        )
        WidgetSize.LARGE -> LargeContent(
            game = game,
            uid = uid,
            current = current,
            max = max,
            isFull = isFull,
            progress = progress,
            prefs = prefs,
            errorMsg = errorMsg,
            tapModifier = tapModifier,
        )
    }
}

// ========== SMALL WIDGET (2x1) ==========

@Composable
private fun SmallContent(
    game: String,
    current: Int,
    max: Int,
    isFull: Boolean,
    progress: Float,
    errorMsg: String?,
    tapModifier: GlanceModifier,
) {
    GlanceTheme {
        Scaffold(
            titleBar = {
                Title(
                    title = getResourceLabel(game),
                )
            },
        ) {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .then(tapModifier),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (errorMsg != null && errorMsg.isNotEmpty()) {
                    Text(
                        text = "⚠️ $errorMsg",
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = GlanceTheme.colors.error,
                        ),
                    )
                } else {
                    // Main number
                    Row(
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        Text(
                            text = "$current",
                            style = TextStyle(
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isFull) GlanceTheme.colors.primary
                                else GlanceTheme.colors.onSurface,
                            ),
                        )
                        Spacer(modifier = GlanceModifier.width(4.dp))
                        Text(
                            text = "/$max",
                            style = TextStyle(
                                fontSize = 14.sp,
                                color = GlanceTheme.colors.onSurfaceVariant,
                            ),
                        )
                    }

                    Spacer(modifier = GlanceModifier.height(6.dp))

                    // Progress bar (simple text-based)
                    ProgressBar(progress = progress, isFull = isFull)

                    Spacer(modifier = GlanceModifier.height(4.dp))

                    // Status
                    Text(
                        text = if (isFull) "✅ 已满！" else "恢复中…",
                        style = TextStyle(
                            fontSize = 11.sp,
                            color = if (isFull) GlanceTheme.colors.primary
                            else GlanceTheme.colors.onSurfaceVariant,
                        ),
                    )
                }
            }
        }
    }
}

// ========== MEDIUM WIDGET (4x2) ==========

@Composable
private fun MediumContent(
    game: String,
    uid: String,
    current: Int,
    max: Int,
    isFull: Boolean,
    progress: Float,
    prefs: Preferences,
    errorMsg: String?,
    tapModifier: GlanceModifier,
) {
    GlanceTheme {
        Scaffold(
            titleBar = {
                Title(title = getResourceLabel(game))
            },
        ) {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .padding(12.dp)
                    .then(tapModifier),
            ) {
                if (errorMsg != null && errorMsg.isNotEmpty()) {
                    Text(
                        text = "⚠️ $errorMsg",
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = GlanceTheme.colors.error,
                        ),
                    )
                    return@Column
                }

                // Top row: resource number + status
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Text(
                        text = "$current",
                        style = TextStyle(
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isFull) GlanceTheme.colors.primary
                            else GlanceTheme.colors.onSurface,
                        ),
                    )
                    Spacer(modifier = GlanceModifier.width(4.dp))
                    Text(
                        text = "/ $max",
                        style = TextStyle(
                            fontSize = 14.sp,
                            color = GlanceTheme.colors.onSurfaceVariant,
                        ),
                    )
                    Spacer(modifier = GlanceModifier.width(8.dp))
                    Text(
                        text = if (isFull) "✅ 已满！" else "恢复中…",
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontWeight = if (isFull) FontWeight.Bold else FontWeight.Normal,
                            color = if (isFull) GlanceTheme.colors.primary
                            else GlanceTheme.colors.onSurfaceVariant,
                        ),
                    )
                }

                Spacer(modifier = GlanceModifier.height(4.dp))
                ProgressBar(progress = progress, isFull = isFull)
                Spacer(modifier = GlanceModifier.height(8.dp))

                // Summary row: commissions + expeditions
                when (game) {
                    "gi" -> MediumContentGI(prefs)
                    "hsr" -> MediumContentHSR(prefs)
                    "zzz" -> MediumContentZZZ(prefs)
                }

                Spacer(modifier = GlanceModifier.defaultWeight())

                // UID at bottom
                Text(
                    text = "UID: $uid",
                    style = TextStyle(
                        fontSize = 10.sp,
                        color = GlanceTheme.colors.onSurfaceVariant,
                    ),
                )
            }
        }
    }
}

@Composable
private fun MediumContentGI(prefs: Preferences) {
    val commDone = prefs[DailyNoteWidgetWorker.KEY_COMMISSIONS_DONE] ?: 0
    val commTotal = prefs[DailyNoteWidgetWorker.KEY_COMMISSIONS_TOTAL] ?: 4
    val expDone = prefs[DailyNoteWidgetWorker.KEY_EXPEDITIONS_DONE] ?: 0
    val expTotal = prefs[DailyNoteWidgetWorker.KEY_EXPEDITIONS_TOTAL] ?: 0

    Row(modifier = GlanceModifier.fillMaxWidth()) {
        SummaryChip(
            icon = "📋",
            label = "委托",
            value = "$commDone/$commTotal",
            isDone = commDone >= commTotal,
        )
        Spacer(modifier = GlanceModifier.width(12.dp))
        SummaryChip(
            icon = "🗺️",
            label = "派遣",
            value = "$expDone/$expTotal",
            isDone = expTotal > 0 && expDone >= expTotal,
        )
    }
}

@Composable
private fun MediumContentHSR(prefs: Preferences) {
    val expDone = prefs[DailyNoteWidgetWorker.KEY_EXPEDITIONS_DONE] ?: 0
    val expTotal = prefs[DailyNoteWidgetWorker.KEY_EXPEDITIONS_TOTAL] ?: 0
    val reserve = prefs[DailyNoteWidgetWorker.KEY_RESERVE_STAMINA] ?: 0

    Row(modifier = GlanceModifier.fillMaxWidth()) {
        SummaryChip(
            icon = "🗺️",
            label = "探索",
            value = "$expDone/$expTotal",
            isDone = expTotal > 0 && expDone >= expTotal,
        )
        Spacer(modifier = GlanceModifier.width(12.dp))
        SummaryChip(
            icon = "🔋",
            label = "后备",
            value = "$reserve",
            isDone = false,
        )
    }
}

@Composable
private fun MediumContentZZZ(prefs: Preferences) {
    val bounty = prefs[DailyNoteWidgetWorker.KEY_BOUNTY_CURRENT] ?: 0
    val bountyMax = prefs[DailyNoteWidgetWorker.KEY_BOUNTY_MAX] ?: 0
    val vitality = prefs[DailyNoteWidgetWorker.KEY_VITALITY_CURRENT] ?: 0
    val vitalityMax = prefs[DailyNoteWidgetWorker.KEY_VITALITY_MAX] ?: 0

    Row(modifier = GlanceModifier.fillMaxWidth()) {
        SummaryChip(
            icon = "💪",
            label = "活力",
            value = "$vitality/$vitalityMax",
            isDone = vitality >= vitalityMax && vitalityMax > 0,
        )
        Spacer(modifier = GlanceModifier.width(12.dp))
        SummaryChip(
            icon = "🎯",
            label = "悬赏",
            value = "$bounty/$bountyMax",
            isDone = bounty >= bountyMax && bountyMax > 0,
        )
    }
}

@Composable
private fun SummaryChip(
    icon: String,
    label: String,
    value: String,
    isDone: Boolean,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "$icon $label",
            style = TextStyle(
                fontSize = 12.sp,
                color = GlanceTheme.colors.onSurfaceVariant,
            ),
        )
        Spacer(modifier = GlanceModifier.width(4.dp))
        Text(
            text = value,
            style = TextStyle(
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDone) GlanceTheme.colors.primary
                else GlanceTheme.colors.onSurface,
            ),
        )
    }
}

// ========== LARGE WIDGET (4x4) ==========

@Composable
private fun LargeContent(
    game: String,
    uid: String,
    current: Int,
    max: Int,
    isFull: Boolean,
    progress: Float,
    prefs: Preferences,
    errorMsg: String?,
    tapModifier: GlanceModifier,
) {
    GlanceTheme {
        Scaffold(
            titleBar = {
                Title(title = getResourceLabel(game))
            },
        ) {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .padding(12.dp)
                    .then(tapModifier),
            ) {
                if (errorMsg != null && errorMsg.isNotEmpty()) {
                    Text(
                        text = "⚠️ $errorMsg",
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = GlanceTheme.colors.error,
                        ),
                    )
                    Text(
                        text = "点击重试",
                        style = TextStyle(
                            fontSize = 11.sp,
                            color = GlanceTheme.colors.onSurfaceVariant,
                        ),
                    )
                    return@Column
                }

                // Main resource display
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Text(
                        text = "$current",
                        style = TextStyle(
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isFull) GlanceTheme.colors.primary
                            else GlanceTheme.colors.onSurface,
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
                    Spacer(modifier = GlanceModifier.defaultWeight())
                    Text(
                        text = if (isFull) "✅ 已满！" else "恢复中…",
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = if (isFull) FontWeight.Bold else FontWeight.Normal,
                            color = if (isFull) GlanceTheme.colors.primary
                            else GlanceTheme.colors.onSurfaceVariant,
                        ),
                    )
                }

                Spacer(modifier = GlanceModifier.height(4.dp))
                ProgressBar(progress = progress, isFull = isFull)
                Spacer(modifier = GlanceModifier.height(12.dp))

                // Game-specific full details
                when (game) {
                    "gi" -> LargeContentGI(prefs)
                    "hsr" -> LargeContentHSR(prefs)
                    "zzz" -> LargeContentZZZ(prefs)
                }

                Spacer(modifier = GlanceModifier.defaultWeight())

                // Footer: UID + update time
                Row(modifier = GlanceModifier.fillMaxWidth()) {
                    Text(
                        text = "UID: $uid",
                        style = TextStyle(
                            fontSize = 10.sp,
                            color = GlanceTheme.colors.onSurfaceVariant,
                        ),
                    )
                    Spacer(modifier = GlanceModifier.defaultWeight())
                    Text(
                        text = formatUpdateTime(prefs[DailyNoteWidget.KEY_UPDATED_AT]),
                        style = TextStyle(
                            fontSize = 10.sp,
                            color = GlanceTheme.colors.onSurfaceVariant,
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun LargeContentGI(prefs: Preferences) {
    val commDone = prefs[DailyNoteWidgetWorker.KEY_COMMISSIONS_DONE] ?: 0
    val commTotal = prefs[DailyNoteWidgetWorker.KEY_COMMISSIONS_TOTAL] ?: 4
    val expDone = prefs[DailyNoteWidgetWorker.KEY_EXPEDITIONS_DONE] ?: 0
    val expTotal = prefs[DailyNoteWidgetWorker.KEY_EXPEDITIONS_TOTAL] ?: 0
    val realmCurrent = prefs[DailyNoteWidgetWorker.KEY_REALM_CURRENCY_CURRENT] ?: 0
    val realmMax = prefs[DailyNoteWidgetWorker.KEY_REALM_CURRENCY_MAX] ?: 0
    val bossRemaining = prefs[DailyNoteWidgetWorker.KEY_WEEKLY_BOSS_REMAINING] ?: 0
    val bossLimit = prefs[DailyNoteWidgetWorker.KEY_WEEKLY_BOSS_LIMIT] ?: 0

    // Commissions
    DetailRow(
        icon = "📋",
        label = "每日委托",
        value = "$commDone/$commTotal",
        isDone = commDone >= commTotal,
    )
    Spacer(modifier = GlanceModifier.height(6.dp))

    // Expeditions
    DetailRow(
        icon = "🗺️",
        label = "探索派遣",
        value = "$expDone/$expTotal 进行中",
        isDone = expTotal > 0 && expDone >= expTotal,
    )
    Spacer(modifier = GlanceModifier.height(6.dp))

    // Realm currency
    val realmProgress = if (realmMax > 0) realmCurrent.toFloat() / realmMax else 0f
    DetailRow(
        icon = "🏠",
        label = "洞天宝钱",
        value = "$realmCurrent/$realmMax",
        isDone = realmCurrent >= realmMax && realmMax > 0,
    )
    Spacer(modifier = GlanceModifier.height(6.dp))

    // Weekly boss
    DetailRow(
        icon = "⚔️",
        label = "周本折扣",
        value = "$bossRemaining/$bossLimit",
        isDone = bossRemaining == 0,
    )
}

@Composable
private fun LargeContentHSR(prefs: Preferences) {
    val expDone = prefs[DailyNoteWidgetWorker.KEY_EXPEDITIONS_DONE] ?: 0
    val expTotal = prefs[DailyNoteWidgetWorker.KEY_EXPEDITIONS_TOTAL] ?: 0
    val reserve = prefs[DailyNoteWidgetWorker.KEY_RESERVE_STAMINA] ?: 0
    val simCurrent = prefs[DailyNoteWidgetWorker.KEY_SIM_UNIVERSE_CURRENT] ?: 0
    val simMax = prefs[DailyNoteWidgetWorker.KEY_SIM_UNIVERSE_MAX] ?: 0

    DetailRow(
        icon = "🗺️",
        label = "探索委托",
        value = "$expDone/$expTotal 进行中",
        isDone = expTotal > 0 && expDone >= expTotal,
    )
    Spacer(modifier = GlanceModifier.height(6.dp))

    DetailRow(
        icon = "🔋",
        label = "后备开拓力",
        value = "$reserve",
        isDone = false,
    )
    Spacer(modifier = GlanceModifier.height(6.dp))

    DetailRow(
        icon = "🌌",
        label = "模拟宇宙积分",
        value = "$simCurrent/$simMax",
        isDone = simCurrent >= simMax && simMax > 0,
    )
}

@Composable
private fun LargeContentZZZ(prefs: Preferences) {
    val vitality = prefs[DailyNoteWidgetWorker.KEY_VITALITY_CURRENT] ?: 0
    val vitalityMax = prefs[DailyNoteWidgetWorker.KEY_VITALITY_MAX] ?: 0
    val bounty = prefs[DailyNoteWidgetWorker.KEY_BOUNTY_CURRENT] ?: 0
    val bountyMax = prefs[DailyNoteWidgetWorker.KEY_BOUNTY_MAX] ?: 0
    val survey = prefs[DailyNoteWidgetWorker.KEY_SURVEY_CURRENT] ?: 0
    val surveyMax = prefs[DailyNoteWidgetWorker.KEY_SURVEY_MAX] ?: 0

    DetailRow(
        icon = "💪",
        label = "活力",
        value = "$vitality/$vitalityMax",
        isDone = vitality >= vitalityMax && vitalityMax > 0,
    )
    Spacer(modifier = GlanceModifier.height(6.dp))

    DetailRow(
        icon = "🎯",
        label = "悬赏委托",
        value = "$bounty/$bountyMax",
        isDone = bounty >= bountyMax && bountyMax > 0,
    )
    Spacer(modifier = GlanceModifier.height(6.dp))

    DetailRow(
        icon = "🔍",
        label = "调查点数",
        value = "$survey/$surveyMax",
        isDone = survey >= surveyMax && surveyMax > 0,
    )
}

@Composable
private fun DetailRow(
    icon: String,
    label: String,
    value: String,
    isDone: Boolean,
) {
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "$icon $label",
            style = TextStyle(
                fontSize = 13.sp,
                color = GlanceTheme.colors.onSurfaceVariant,
            ),
        )
        Spacer(modifier = GlanceModifier.defaultWeight())
        Text(
            text = if (isDone) "✅ $value" else value,
            style = TextStyle(
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDone) GlanceTheme.colors.primary
                else GlanceTheme.colors.onSurface,
            ),
        )
    }
}

// ========== Shared Components ==========

@Composable
private fun ProgressBar(progress: Float, isFull: Boolean) {
    // Simple text-based progress bar since Glance has limited drawing support
    val barLength = 20
    val filled = (progress * barLength).toInt().coerceIn(0, barLength)
    val bar = "█".repeat(filled) + "░".repeat(barLength - filled)
    val percent = (progress * 100).toInt()

    Text(
        text = "$bar $percent%",
        style = TextStyle(
            fontSize = 10.sp,
            color = if (isFull) GlanceTheme.colors.primary
            else GlanceTheme.colors.onSurfaceVariant,
        ),
    )
}

private fun getResourceLabel(game: String): String = when (game) {
    "gi" -> "⚡ 原粹树脂"
    "hsr" -> "⚡ 开拓力"
    "zzz" -> "⚡ 电量"
    else -> "每日便笺"
}

private fun formatUpdateTime(timestamp: Long?): String {
    if (timestamp == null || timestamp == 0L) return ""
    val now = System.currentTimeMillis()
    val diffMin = ((now - timestamp) / 60_000).toInt()
    return when {
        diffMin < 1 -> "刚刚更新"
        diffMin < 60 -> "${diffMin}分钟前"
        diffMin < 1440 -> "${diffMin / 60}小时前"
        else -> "超过1天"
    }
}
