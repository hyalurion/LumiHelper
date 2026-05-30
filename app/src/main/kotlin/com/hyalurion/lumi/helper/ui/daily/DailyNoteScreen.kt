package com.hyalurion.lumi.helper.ui.daily

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hyalurion.lumi.helper.model.DailyNoteGI
import com.hyalurion.lumi.helper.model.DailyNoteHSR
import com.hyalurion.lumi.helper.model.DailyNoteZZZ
import com.hyalurion.lumi.helper.model.ExpeditionGI
import com.hyalurion.lumi.helper.model.ExpeditionHSR
import com.hyalurion.lumi.helper.model.HoYoAccount
import com.hyalurion.lumi.helper.model.TransformerGI
import kotlinx.coroutines.delay
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.utils.PressFeedbackType
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * Daily Note screen - shows real-time resin/stamina/energy for all games.
 * Ported from PizzaHelperUnited's DailyNote widget and main app logic.
 */
@Composable
fun DailyNoteScreen(
    viewModel: DailyNoteViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Account selector
        if (uiState.accounts.isNotEmpty()) {
            AccountSelector(
                accounts = uiState.accounts,
                selectedAccount = uiState.selectedAccount,
                onSelect = { viewModel.selectAccount(it) },
            )
        }

        // Refresh button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "实时便笺",
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

        // Content
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null -> {
                Card(modifier = Modifier.fillMaxWidth(), pressFeedbackType = PressFeedbackType.Sink) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "提示",
                            style = MiuixTheme.textStyles.title2,
                            fontWeight = FontWeight.Bold,
                            color = MiuixTheme.colorScheme.primary,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = uiState.error ?: "",
                            style = MiuixTheme.textStyles.body2,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "请先在「账号管理」中添加 HoYoLAB / 米游社账号",
                            style = MiuixTheme.textStyles.main,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        )
                    }
                }
            }
            uiState.dailyNoteGI != null -> {
                GenshinDailyNoteCard(uiState.dailyNoteGI!!)
            }
            uiState.dailyNoteHSR != null -> {
                StarRailDailyNoteCard(uiState.dailyNoteHSR!!)
            }
            uiState.dailyNoteZZZ != null -> {
                ZZZDailyNoteCard(uiState.dailyNoteZZZ!!)
            }
        }
    }
}

@Composable
private fun AccountSelector(
    accounts: List<HoYoAccount>,
    selectedAccount: HoYoAccount?,
    onSelect: (HoYoAccount) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth(), pressFeedbackType = PressFeedbackType.Sink) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "选择账号",
                style = MiuixTheme.textStyles.title3,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                accounts.forEach { account ->
                    val isSelected = account == selectedAccount
                    Button(onClick = { onSelect(account) }) {
                        Text(
                            text = "${account.game.shortName} · ${account.uid}",
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        )
                    }
                }
            }
        }
    }
}

// ========== Genshin Impact Daily Note ==========

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GenshinDailyNoteCard(note: DailyNoteGI) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Resin
        ProgressCard(
            icon = "⚡",
            title = "原粹树脂",
            current = note.currentResin,
            max = note.maxResin,
            subtitle = if (note.isResinFull) "已满！" else "恢复中...",
            extra = note.resinFullTime?.let { "预计 ${formatTimeRemaining(it.epochSecond - System.currentTimeMillis() / 1000)}" },
        )

        // Daily Commissions
        Card(modifier = Modifier.fillMaxWidth(), pressFeedbackType = PressFeedbackType.Sink) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = "📋", style = MiuixTheme.textStyles.headline2)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "每日委托",
                        style = MiuixTheme.textStyles.title2,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "${note.finishedTaskNum}/${note.totalTaskNum} 完成" +
                            if (note.isExtraTaskRewardReceived) " · 奖励已领" else "",
                        style = MiuixTheme.textStyles.body2,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    )
                }
            }
        }

        // Realm Currency
        ProgressCard(
            icon = "🏠",
            title = "洞天宝钱",
            current = note.currentHomeCoin,
            max = note.maxHomeCoin,
            subtitle = if (note.currentHomeCoin >= note.maxHomeCoin) "已满！" else "积累中...",
        )

        // Expeditions with countdown
        ExpeditionCardGI(note)

        // Parametric Transformer
        TransformerCard(note.transformer)

        // Weekly Boss
        Card(modifier = Modifier.fillMaxWidth(), pressFeedbackType = PressFeedbackType.Sink) {
            Row(modifier = Modifier.padding(16.dp)) {
                Text(text = "🐉", style = MiuixTheme.textStyles.headline2)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "周本折扣",
                        style = MiuixTheme.textStyles.title2,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "${note.remainResinDiscountNum}/${note.resinDiscountNumLimit} 剩余",
                        style = MiuixTheme.textStyles.body2,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    )
                }
            }
        }
    }
}

// ========== Star Rail Daily Note ==========

@Composable
private fun StarRailDailyNoteCard(note: DailyNoteHSR) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Stamina
        ProgressCard(
            icon = "⚡",
            title = "开拓力",
            current = note.currentStamina,
            max = note.maxStamina,
            subtitle = if (note.isStaminaFull) "已满！" else "恢复中...",
            extra = note.staminaFullTime?.let { "预计 ${formatTimeRemaining(it.epochSecond - System.currentTimeMillis() / 1000)}" },
        )

        // Reserve Stamina
        if (note.currentReserveStamina > 0) {
            Card(modifier = Modifier.fillMaxWidth(), pressFeedbackType = PressFeedbackType.Sink) {
                Row(modifier = Modifier.padding(16.dp)) {
                    Text(text = "🔋", style = MiuixTheme.textStyles.headline2)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "后备开拓力",
                            style = MiuixTheme.textStyles.title2,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "${note.currentReserveStamina}",
                            style = MiuixTheme.textStyles.body2,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        )
                    }
                }
            }
        }

        // Expeditions with countdown
        ExpeditionCardHSR(note)

        // Simulated Universe
        Card(modifier = Modifier.fillMaxWidth(), pressFeedbackType = PressFeedbackType.Sink) {
            Row(modifier = Modifier.padding(16.dp)) {
                Text(text = "🌌", style = MiuixTheme.textStyles.headline2)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "模拟宇宙积分",
                        style = MiuixTheme.textStyles.title2,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "${note.currentTrainScore}/${note.maxTrainScore}",
                        style = MiuixTheme.textStyles.body2,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    )
                }
            }
        }
    }
}

// ========== ZZZ Daily Note ==========

@Composable
private fun ZZZDailyNoteCard(note: DailyNoteZZZ) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Energy
        ProgressCard(
            icon = "⚡",
            title = "电量",
            current = note.energy.progress,
            max = note.energy.maxProgress,
            subtitle = "每6分钟恢复 ${note.energy.restore}",
        )

        // Vitality
        ProgressCard(
            icon = "💪",
            title = "活力",
            current = note.vitality.current,
            max = note.vitality.max,
            subtitle = if (note.vitality.current >= note.vitality.max) "已满！" else "恢复中...",
        )

        // Bounty Commission
        Card(modifier = Modifier.fillMaxWidth(), pressFeedbackType = PressFeedbackType.Sink) {
            Row(modifier = Modifier.padding(16.dp)) {
                Text(text = "📋", style = MiuixTheme.textStyles.headline2)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "悬赏委托",
                        style = MiuixTheme.textStyles.title2,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "${note.bountyCommission.num}/${note.bountyCommission.total}",
                        style = MiuixTheme.textStyles.body2,
                    )
                }
            }
        }

        // Survey Points
        Card(modifier = Modifier.fillMaxWidth(), pressFeedbackType = PressFeedbackType.Sink) {
            Row(modifier = Modifier.padding(16.dp)) {
                Text(text = "🔍", style = MiuixTheme.textStyles.headline2)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "调查点数",
                        style = MiuixTheme.textStyles.title2,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "${note.surveyPoints.num}/${note.surveyPoints.total}",
                        style = MiuixTheme.textStyles.body2,
                    )
                }
            }
        }
    }
}

// ========== Expedition Countdown (GI) ==========

@Composable
private fun ExpeditionCardGI(note: DailyNoteGI) {
    Card(modifier = Modifier.fillMaxWidth(), pressFeedbackType = PressFeedbackType.Sink) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "探索派遣",
                style = MiuixTheme.textStyles.title2,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${note.currentExpeditionNum}/${note.maxExpeditionNum} 进行中",
                style = MiuixTheme.textStyles.body2,
            )
            note.expeditions.forEachIndexed { index, exp ->
                Spacer(modifier = Modifier.height(6.dp))
                ExpeditionRowGI(exp, index)
            }
        }
    }
}

@Composable
private fun ExpeditionRowGI(exp: ExpeditionGI, index: Int) {
    if (exp.isFinished) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "✅",
                style = MiuixTheme.textStyles.body2,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "派遣 #${index + 1} 已完成",
                style = MiuixTheme.textStyles.body2,
                color = MiuixTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )
        }
    } else {
        // Live countdown timer - updates every second
        val remainingSeconds = exp.remainingTimeSeconds.toLongOrNull() ?: 0L
        var currentRemaining by remember(exp) { mutableLongStateOf(remainingSeconds) }

        LaunchedEffect(exp) {
            while (currentRemaining > 0) {
                delay(1000L)
                currentRemaining = (currentRemaining - 1).coerceAtLeast(0)
            }
        }

        val isAlmostDone = currentRemaining in 1..300 // < 5 minutes
        val textColor = if (isAlmostDone) MiuixTheme.colorScheme.error
        else MiuixTheme.colorScheme.onSurfaceVariantSummary

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (isAlmostDone) "⏰" else "⏳",
                style = MiuixTheme.textStyles.body2,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "派遣 #${index + 1}",
                style = MiuixTheme.textStyles.body2,
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = formatCountdown(currentRemaining),
                style = MiuixTheme.textStyles.body2,
                fontWeight = if (isAlmostDone) FontWeight.Bold else FontWeight.Normal,
                color = textColor,
            )
        }
    }
}

// ========== Expedition Countdown (HSR) ==========

@Composable
private fun ExpeditionCardHSR(note: DailyNoteHSR) {
    Card(modifier = Modifier.fillMaxWidth(), pressFeedbackType = PressFeedbackType.Sink) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "探索派遣",
                style = MiuixTheme.textStyles.title2,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${note.acceptedExpeditionNum}/${note.totalExpeditionNum} 进行中",
                style = MiuixTheme.textStyles.body2,
            )
            note.expeditions.forEach { exp ->
                Spacer(modifier = Modifier.height(6.dp))
                ExpeditionRowHSR(exp)
            }
        }
    }
}

@Composable
private fun ExpeditionRowHSR(exp: ExpeditionHSR) {
    if (exp.isFinished) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "✅",
                style = MiuixTheme.textStyles.body2,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${exp.name ?: "探索"} 已完成",
                style = MiuixTheme.textStyles.body2,
                color = MiuixTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )
        }
    } else {
        // Live countdown timer - updates every second
        var currentRemaining by remember(exp) { mutableLongStateOf(exp.remainingTimeSeconds.toLong()) }

        LaunchedEffect(exp) {
            while (currentRemaining > 0) {
                delay(1000L)
                currentRemaining = (currentRemaining - 1).coerceAtLeast(0)
            }
        }

        val isAlmostDone = currentRemaining in 1..300 // < 5 minutes
        val textColor = if (isAlmostDone) MiuixTheme.colorScheme.error
        else MiuixTheme.colorScheme.onSurfaceVariantSummary

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (isAlmostDone) "⏰" else "⏳",
                style = MiuixTheme.textStyles.body2,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = exp.name ?: "探索",
                style = MiuixTheme.textStyles.body2,
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = formatCountdown(currentRemaining),
                style = MiuixTheme.textStyles.body2,
                fontWeight = if (isAlmostDone) FontWeight.Bold else FontWeight.Normal,
                color = textColor,
            )
        }
    }
}

// ========== Parametric Transformer ==========

@Composable
private fun TransformerCard(transformer: TransformerGI?) {
    Card(modifier = Modifier.fillMaxWidth(), pressFeedbackType = PressFeedbackType.Sink) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = "🔄", style = MiuixTheme.textStyles.headline2)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "参量质变仪",
                    style = MiuixTheme.textStyles.title2,
                    fontWeight = FontWeight.Bold,
                )
                when {
                    transformer == null || !transformer.obtained -> {
                        Text(
                            text = "未获得",
                            style = MiuixTheme.textStyles.body2,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        )
                    }
                    transformer.recoveryTime?.reached == true -> {
                        Text(
                            text = "✅ 可用",
                            style = MiuixTheme.textStyles.body2,
                            color = MiuixTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    transformer.recoveryTime != null -> {
                        val rt = transformer.recoveryTime!!
                        val totalSeconds = rt.day * 86400L + rt.hour * 3600L + rt.minute * 60L + rt.second
                        var remaining by remember(transformer) { mutableLongStateOf(totalSeconds) }

                        LaunchedEffect(transformer) {
                            while (remaining > 0) {
                                delay(1000L)
                                remaining = (remaining - 1).coerceAtLeast(0)
                            }
                        }

                        val days = remaining / 86400
                        val hours = (remaining % 86400) / 3600
                        val minutes = (remaining % 3600) / 60

                        val timeText = when {
                            days > 0 -> "${days}天${hours}小时${minutes}分钟"
                            hours > 0 -> "${hours}小时${minutes}分钟"
                            minutes > 0 -> "${minutes}分钟"
                            else -> "< 1分钟"
                        }

                        Text(
                            text = "冷却中: $timeText",
                            style = MiuixTheme.textStyles.body2,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        )
                    }
                    else -> {
                        Text(
                            text = "状态未知",
                            style = MiuixTheme.textStyles.body2,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        )
                    }
                }
            }
        }
    }
}

// ========== Shared Components ==========

@Composable
private fun ProgressCard(
    icon: String,
    title: String,
    current: Int,
    max: Int,
    subtitle: String,
    extra: String? = null,
) {
    val progress = if (max > 0) current.toFloat() / max else 0f
    val isFull = current >= max

    Card(modifier = Modifier.fillMaxWidth(), pressFeedbackType = PressFeedbackType.Sink) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = icon, style = MiuixTheme.textStyles.headline2)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MiuixTheme.textStyles.title2,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = subtitle,
                        style = MiuixTheme.textStyles.main,
                        color = if (isFull) MiuixTheme.colorScheme.primary
                        else MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$current/$max",
                        style = MiuixTheme.textStyles.headline2,
                        fontWeight = FontWeight.Bold,
                        color = if (isFull) MiuixTheme.colorScheme.primary
                        else MiuixTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MiuixTheme.textStyles.main,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    )
                }
            }
            if (extra != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = extra,
                    style = MiuixTheme.textStyles.main,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
        }
    }
}

private fun formatTimeRemaining(seconds: Long): String {
    if (seconds <= 0) return "已满"
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    return when {
        hours > 0 -> "${hours}小时${minutes}分钟"
        minutes > 0 -> "${minutes}分钟"
        else -> "< 1分钟"
    }
}

/**
 * Format countdown as HH:MM:SS for expedition timers.
 */
private fun formatCountdown(seconds: Long): String {
    if (seconds <= 0) return "00:00:00"
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return "%02d:%02d:%02d".format(h, m, s)
}
