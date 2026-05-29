package com.chronie.helperunited.ui.event

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.chronie.helperunited.model.EventType
import com.chronie.helperunited.model.GameEvent
import com.chronie.helperunited.model.VersionInfo
import kotlinx.coroutines.delay
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.utils.PressFeedbackType
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Event Calendar screen - shows game events with version countdowns.
 * Ported from PizzaHelperUnited's event calendar feature.
 */
@Composable
fun EventCalendarScreen(
    viewModel: EventCalendarViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Header with refresh
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "🎮 活动日历",
                style = MiuixTheme.textStyles.headline2,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            Button(
                onClick = { viewModel.refresh() },
                enabled = !uiState.isLoading,
            ) {
                Text("刷新")
            }
        }

        // Game filter chips
        GameFilterChips(
            selectedGame = uiState.selectedGame,
            onSelect = { viewModel.setGameFilter(it) },
        )

        // Loading indicator
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }

        // Error message
        uiState.error?.let { error ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                pressFeedbackType = PressFeedbackType.Sink,
            ) {
                Text(
                    text = "⚠️ $error",
                    modifier = Modifier.padding(16.dp),
                    color = MiuixTheme.colorScheme.error,
                )
            }
        }

        // Version countdown cards
        val versionInfoList = viewModel.getFilteredVersionInfo()
        if (versionInfoList.isNotEmpty()) {
            Text(
                text = "版本倒计时",
                style = MiuixTheme.textStyles.title1,
                fontWeight = FontWeight.Bold,
            )
            versionInfoList.forEach { versionInfo ->
                VersionCountdownCard(versionInfo = versionInfo)
            }
        }

        // Active events section
        val activeEvents = viewModel.getActiveEvents()
        if (activeEvents.isNotEmpty()) {
            Text(
                text = "🔴 进行中的活动",
                style = MiuixTheme.textStyles.title1,
                fontWeight = FontWeight.Bold,
            )
            activeEvents.forEach { event ->
                EventCard(
                    event = event,
                    onClick = {
                        event.wikiUrl?.let { url ->
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        }
                    },
                )
            }
        }

        // Upcoming events section
        val upcomingEvents = viewModel.getUpcomingEvents()
        if (upcomingEvents.isNotEmpty()) {
            Text(
                text = "🟡 即将开始",
                style = MiuixTheme.textStyles.title1,
                fontWeight = FontWeight.Bold,
            )
            upcomingEvents.forEach { event ->
                EventCard(
                    event = event,
                    onClick = {
                        event.wikiUrl?.let { url ->
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        }
                    },
                )
            }
        }

        // Ended events section
        val endedEvents = viewModel.getEndedEvents()
        if (endedEvents.isNotEmpty()) {
            Text(
                text = "⚫ 已结束",
                style = MiuixTheme.textStyles.title1,
                fontWeight = FontWeight.Bold,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
            endedEvents.forEach { event ->
                EventCard(
                    event = event,
                    onClick = {
                        event.wikiUrl?.let { url ->
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        }
                    },
                    dimmed = true,
                )
            }
        }

        // Empty state
        if (!uiState.isLoading && activeEvents.isEmpty() && upcomingEvents.isEmpty() && endedEvents.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "暂无活动数据\n请点击刷新获取最新信息",
                    style = MiuixTheme.textStyles.body1,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * Game filter chip row.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GameFilterChips(
    selectedGame: String?,
    onSelect: (String?) -> Unit,
) {
    val games = listOf(
        null to "全部",
        "GI" to "⚡原神",
        "HSR" to "🚂星铁",
        "ZZZ" to "🎮绝区零",
    )

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        games.forEach { (gameId, label) ->
            val isSelected = gameId == selectedGame
            Card(
                modifier = Modifier.clickable { onSelect(gameId) },
                pressFeedbackType = PressFeedbackType.Sink,
            ) {
                Text(
                    text = label,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) MiuixTheme.colorScheme.primary
                    else MiuixTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

/**
 * Version countdown card with live timer.
 */
@Composable
private fun VersionCountdownCard(versionInfo: VersionInfo) {
    var remainingSeconds by remember { mutableLongStateOf(versionInfo.countdownSeconds) }

    // Tick every second
    LaunchedEffect(versionInfo) {
        while (remainingSeconds > 0) {
            delay(1000)
            remainingSeconds = versionInfo.countdownSeconds
        }
    }

    val gameEmoji = when (versionInfo.game) {
        "GI" -> "⚡"
        "HSR" -> "🚂"
        "ZZZ" -> "🎮"
        else -> "🎯"
    }

    val gameName = when (versionInfo.game) {
        "GI" -> "原神"
        "HSR" -> "星穹铁道"
        "ZZZ" -> "绝区零"
        else -> versionInfo.game
    }

    val days = remainingSeconds / 86400
    val hours = (remainingSeconds % 86400) / 3600
    val minutes = (remainingSeconds % 3600) / 60
    val secs = remainingSeconds % 60

    Card(
        modifier = Modifier.fillMaxWidth(),
        pressFeedbackType = PressFeedbackType.Sink,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = gameEmoji, style = MiuixTheme.textStyles.headline2)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "$gameName ${versionInfo.displayVersion}",
                        style = MiuixTheme.textStyles.title2,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "版本倒计时",
                        style = MiuixTheme.textStyles.main,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Countdown display
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                CountdownUnit(days.toString(), "天")
                CountdownUnit(hours.toString().padStart(2, '0'), "时")
                CountdownUnit(minutes.toString().padStart(2, '0'), "分")
                CountdownUnit(secs.toString().padStart(2, '0'), "秒")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Date range
            val dateFormatter = DateTimeFormatter.ofPattern("MM/dd HH:mm")
            val startDate = Instant.ofEpochSecond(versionInfo.startEpoch)
                .atZone(ZoneId.systemDefault())
                .format(dateFormatter)
            val endDate = Instant.ofEpochSecond(versionInfo.endEpoch)
                .atZone(ZoneId.systemDefault())
                .format(dateFormatter)

            Text(
                text = "$startDate → $endDate",
                style = MiuixTheme.textStyles.main,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
        }
    }
}

@Composable
private fun CountdownUnit(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MiuixTheme.textStyles.headline1,
            fontWeight = FontWeight.Bold,
            color = MiuixTheme.colorScheme.primary,
        )
        Text(
            text = label,
            style = MiuixTheme.textStyles.main,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        )
    }
}

/**
 * Single event card in the timeline.
 */
@Composable
private fun EventCard(
    event: GameEvent,
    onClick: () -> Unit,
    dimmed: Boolean = false,
) {
    val typeColor = when (event.type) {
        EventType.LIMITED_TIME -> Color(0xFF4CAF50)     // Green
        EventType.BANNER -> Color(0xFFFFD700)           // Gold
        EventType.WEAPON_BANNER -> Color(0xFFFF9800)    // Orange
        EventType.MAINTENANCE -> Color(0xFFFF5722)      // Red-orange
        EventType.VERSION_UPDATE -> Color(0xFF2196F3)   // Blue
        EventType.BATTLE_PASS -> Color(0xFF9C27B0)      // Purple
        EventType.WEB_EVENT -> Color(0xFF00BCD4)        // Cyan
        EventType.CODEX -> Color(0xFF795548)            // Brown
        EventType.OTHER -> Color(0xFF9E9E9E)            // Gray
    }

    val gameEmoji = when (event.game) {
        "GI" -> "⚡"
        "HSR" -> "🚂"
        "ZZZ" -> "🎮"
        else -> "🎯"
    }

    val alpha = if (dimmed) 0.5f else 1f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        pressFeedbackType = PressFeedbackType.Sink,
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            // Timeline dot
            Box(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(typeColor),
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Title row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = gameEmoji, style = MiuixTheme.textStyles.body2)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = event.name,
                        style = MiuixTheme.textStyles.title2,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                // Type badge
                Text(
                    text = "${event.type.icon} ${event.type.displayName}",
                    style = MiuixTheme.textStyles.main,
                    color = typeColor,
                )

                // Time range
                val dateFormatter = DateTimeFormatter.ofPattern("MM/dd HH:mm")
                val start = Instant.ofEpochSecond(event.startEpoch)
                    .atZone(ZoneId.systemDefault())
                    .format(dateFormatter)
                val end = Instant.ofEpochSecond(event.endEpoch)
                    .atZone(ZoneId.systemDefault())
                    .format(dateFormatter)

                Text(
                    text = "⏰ $start → $end",
                    style = MiuixTheme.textStyles.main,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )

                // Remaining time for active/upcoming events
                if (event.isActive || event.isUpcoming) {
                    val remaining = event.remainingSeconds
                    val days = remaining / 86400
                    val hours = (remaining % 86400) / 3600
                    val label = if (event.isActive) "剩余" else "开始于"
                    Text(
                        text = "⏳ $label ${days}天${hours}小时",
                        style = MiuixTheme.textStyles.main,
                        color = if (event.isActive) Color(0xFF4CAF50) else Color(0xFFFFD700),
                    )
                }

                // Description preview
                if (event.description.isNotBlank()) {
                    Text(
                        text = event.description,
                        style = MiuixTheme.textStyles.main,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            // Link indicator
            if (event.wikiUrl != null) {
                Text(
                    text = "🔗",
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
    }
}
