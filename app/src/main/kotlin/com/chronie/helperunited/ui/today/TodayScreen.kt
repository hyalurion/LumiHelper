package com.chronie.helperunited.ui.today

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.chronie.helperunited.model.DailyNoteGI
import com.chronie.helperunited.model.DailyNoteHSR
import com.chronie.helperunited.model.DailyNoteZZZ
import com.chronie.helperunited.model.ExpeditionGI
import com.chronie.helperunited.model.ExpeditionHSR
import com.chronie.helperunited.model.GameEvent
import com.chronie.helperunited.model.HoYoAccount
import com.chronie.helperunited.model.TransformerGI
import com.chronie.helperunited.model.VersionInfo
import com.chronie.helperunited.ui.daily.DailyNoteViewModel
import com.chronie.helperunited.ui.daily.DailyNoteUiState
import com.chronie.helperunited.ui.event.EventCalendarUiState
import com.chronie.helperunited.ui.event.EventCalendarViewModel
import com.chronie.helperunited.ui.materials.MaterialDomain
import com.chronie.helperunited.ui.materials.TodayMaterialsUiState
import com.chronie.helperunited.ui.materials.TodayMaterialsViewModel
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.LinearProgressIndicator
import top.yukonga.miuix.kmp.basic.PullToRefresh
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.rememberPullToRefreshState
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType

private val SECTION_SPACING = 20.dp

private enum class TodayGameFilter(val label: String, val shortLabel: String) {
    ALL("全部", "All"),
    GENSHIN("原神", "GI"),
    STAR_RAIL("星穹", "HSR"),
    ZZZ("绝区零", "ZZZ"),
}

/**
 * Today dashboard screen — integrates four features:
 * 1. Real-time notes (resin, stamina, energy)
 * 2. Today's upgrade materials (domains)
 * 3. Current game events
 * 4. Official game feeds
 *
 * Ported from PizzaHelperUnited's TodayTabPage.
 */
@Composable
fun TodayScreen(
    onNavigateToMaterials: () -> Unit,
    onNavigateToEvents: () -> Unit,
) {
    val dailyNoteVM: DailyNoteViewModel = hiltViewModel()
    val materialsVM: TodayMaterialsViewModel = hiltViewModel()
    val eventsVM: EventCalendarViewModel = hiltViewModel()

    val dailyUiState by dailyNoteVM.uiState.collectAsState()
    val materialsUiState by materialsVM.uiState.collectAsState()
    val eventsUiState by eventsVM.uiState.collectAsState()

    var selectedGame by remember { mutableStateOf(TodayGameFilter.ALL) }
    val isRefreshing = remember(dailyUiState.isLoading, eventsUiState.isLoading) {
        dailyUiState.isLoading || eventsUiState.isLoading
    }

    val pullToRefreshState = rememberPullToRefreshState()

    LaunchedEffect(isRefreshing) {
        pullToRefreshState.isRefreshing = isRefreshing
    }

    PullToRefresh(
        isRefreshing = isRefreshing,
        onRefresh = {
            dailyNoteVM.refresh()
            eventsVM.refresh()
        },
        pullToRefreshState = pullToRefreshState,
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(SECTION_SPACING),
        ) {
            TodayHeader(
                onRefresh = {
                    dailyNoteVM.refresh()
                    eventsVM.refresh()
                },
                isLoading = isRefreshing,
            )

            GamePickerRow(
                selected = selectedGame,
                onSelect = { selectedGame = it },
            )

            GameFilterSection(
                accounts = dailyUiState.accounts,
                selectedAccount = dailyUiState.selectedAccount,
                onSelect = { dailyNoteVM.selectAccount(it) },
            )

            if (dailyUiState.accounts.isEmpty()) {
                NoAccountHint()
            }

            DailyNoteContent(
                uiState = dailyUiState,
                viewModel = dailyNoteVM,
            )

            TodayMaterialsSection(
                uiState = materialsUiState,
                onClick = onNavigateToMaterials,
            )

            EventsSection(
                uiState = eventsUiState,
                onClick = onNavigateToEvents,
            )

            OfficialFeedPlaceholder()
        }
    }
}

// ========== Header ==========

@Composable
private fun TodayHeader(
    onRefresh: () -> Unit,
    isLoading: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "今日",
            style = MiuixTheme.textStyles.headline2,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
        )
        Button(
            onClick = onRefresh,
            enabled = !isLoading,
        ) {
            Text("刷新")
        }
    }
}

// ========== Game Picker ==========

@Composable
private fun GamePickerRow(
    selected: TodayGameFilter,
    onSelect: (TodayGameFilter) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TodayGameFilter.entries.forEach { filter ->
            val isSelected = filter == selected
            Button(
                onClick = { onSelect(filter) },
                modifier = if (isSelected) Modifier.background(
                    color = MiuixTheme.colorScheme.primary.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(20.dp),
                ) else Modifier,
            ) {
                Text(
                    text = filter.label,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) MiuixTheme.colorScheme.primary
                    else MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
        }
    }
}

// ========== Official Feed Placeholder ==========

@Composable
private fun OfficialFeedPlaceholder() {
    SectionLabel("官方资讯")

    Card(
        modifier = Modifier.fillMaxWidth(),
        pressFeedbackType = PressFeedbackType.Sink,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "\uD83D\uDCF0",
                style = MiuixTheme.textStyles.headline2,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "官方资讯",
                style = MiuixTheme.textStyles.title2,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "游戏新闻和公告即将上线，敬请期待",
                style = MiuixTheme.textStyles.body2,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
        }
    }
}

// ========== Game Filter ==========

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GameFilterSection(
    accounts: List<HoYoAccount>,
    selectedAccount: HoYoAccount?,
    onSelect: (HoYoAccount) -> Unit,
) {
    if (accounts.isEmpty()) return

    Card(
        modifier = Modifier.fillMaxWidth(),
        pressFeedbackType = PressFeedbackType.Sink,
    ) {
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

// ========== No Account Hint ==========

@Composable
private fun NoAccountHint() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        pressFeedbackType = PressFeedbackType.Sink,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "提示",
                style = MiuixTheme.textStyles.title2,
                fontWeight = FontWeight.Bold,
                color = MiuixTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "请先在「账号管理」中添加 HoYoLAB / 米游社账号，以便查看实时便笺数据。",
                style = MiuixTheme.textStyles.main,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
        }
    }
}

// ========== Daily Note Content ==========

@Composable
private fun DailyNoteContent(
    uiState: DailyNoteUiState,
    viewModel: DailyNoteViewModel,
) {
    SectionLabel("实时便笺")

    when {
        uiState.isLoading -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }
        uiState.error != null -> {
            Card(
                modifier = Modifier.fillMaxWidth(),
                pressFeedbackType = PressFeedbackType.Sink,
            ) {
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
        else -> {
            if (uiState.accounts.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    pressFeedbackType = PressFeedbackType.Sink,
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "选择一个账号以查看实时便笺",
                            style = MiuixTheme.textStyles.main,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        )
                    }
                }
            }
        }
    }
}

// ========== [1] Today Materials Section ==========

@Composable
private fun TodayMaterialsSection(
    uiState: TodayMaterialsUiState,
    onClick: () -> Unit,
) {
    SectionLabel("今日素材")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        pressFeedbackType = PressFeedbackType.Sink,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "📅",
                    style = MiuixTheme.textStyles.headline2,
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "今日可刷素材",
                        style = MiuixTheme.textStyles.title2,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = if (uiState.isSunday) "周日：全部开放"
                        else materialDayLabel(uiState.dayOfWeek),
                        style = MiuixTheme.textStyles.body2,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    )
                }
                Text(
                    text = "查看 →",
                    style = MiuixTheme.textStyles.main,
                    color = MiuixTheme.colorScheme.primary,
                )
            }

            if (uiState.talentDomains.isNotEmpty() || uiState.weaponDomains.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                MaterialDomainSummary(uiState.talentDomains, "天赋")
                MaterialDomainSummary(uiState.weaponDomains, "武器")
            }

            if (uiState.isSunday) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "🎉 周日所有素材副本全部开放",
                    style = MiuixTheme.textStyles.main,
                    color = MiuixTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun MaterialDomainSummary(
    domains: List<MaterialDomain>,
    label: String,
) {
    if (domains.isEmpty()) return
    Row(modifier = Modifier.padding(vertical = 2.dp)) {
        Text(
            text = "$label: ",
            style = MiuixTheme.textStyles.main,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = domains.joinToString("、") { it.name },
            style = MiuixTheme.textStyles.main,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

// ========== [2] Events Section ==========

@Composable
private fun EventsSection(
    uiState: EventCalendarUiState,
    onClick: () -> Unit,
) {
    SectionLabel("当前活动")

    when {
        uiState.isLoading -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }
        uiState.error != null -> {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onClick() },
                pressFeedbackType = PressFeedbackType.Sink,
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "活动日历",
                        style = MiuixTheme.textStyles.title2,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "⚠️ ${uiState.error}",
                        style = MiuixTheme.textStyles.main,
                        color = MiuixTheme.colorScheme.error,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "点击刷新 →",
                        style = MiuixTheme.textStyles.main,
                        color = MiuixTheme.colorScheme.primary,
                    )
                }
            }
        }
        else -> {
            val activeVersionList = uiState.versionInfo.filter { it.isActive }
            val activeEventList = uiState.events.filter { it.isActive }
            val hasData = activeVersionList.isNotEmpty() || activeEventList.isNotEmpty()

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onClick() },
                pressFeedbackType = PressFeedbackType.Sink,
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "🎮",
                            style = MiuixTheme.textStyles.headline2,
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "活动日历",
                                style = MiuixTheme.textStyles.title2,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = if (hasData) "${activeEventList.size} 个进行中"
                                else "暂无活动数据",
                                style = MiuixTheme.textStyles.body2,
                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            )
                        }
                        Text(
                            text = "查看 →",
                            style = MiuixTheme.textStyles.main,
                            color = MiuixTheme.colorScheme.primary,
                        )
                    }

                    if (activeVersionList.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        activeVersionList.take(2).forEach { version ->
                            VersionSummaryRow(version)
                        }
                    }

                    if (activeEventList.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        activeEventList.take(3).forEach { event ->
                            EventSummaryRow(event)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VersionSummaryRow(version: VersionInfo) {
    Row(
        modifier = Modifier.padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(Color(0xFF4CAF50)),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = version.displayVersion,
            style = MiuixTheme.textStyles.main,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = formatMonthsRemaining(version.countdownSeconds),
            style = MiuixTheme.textStyles.main,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        )
    }
}

@Composable
private fun EventSummaryRow(event: GameEvent) {
    Row(
        modifier = Modifier.padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(Color(0xFFFFD700)),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "${event.type.icon} ${event.name}",
            style = MiuixTheme.textStyles.main,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        val remaining = event.remainingSeconds
        when {
            remaining <= 0 -> {
                Text(
                    text = "已结束",
                    style = MiuixTheme.textStyles.main,
                    color = MiuixTheme.colorScheme.error,
                )
            }
            remaining < 86400 -> {
                val hours = remaining / 3600
                Text(
                    text = "${hours}小时",
                    style = MiuixTheme.textStyles.main,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
            else -> {
                val days = remaining / 86400
                Text(
                    text = "${days}天",
                    style = MiuixTheme.textStyles.main,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
        }
    }
}

// ========== Section Label ==========

@Composable
private fun SectionLabel(title: String) {
    Text(
        text = title,
        style = MiuixTheme.textStyles.title1,
        fontWeight = FontWeight.Bold,
    )
}

// ========== [3] Daily Note Cards ==========

// ---------- Genshin Impact ----------

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GenshinDailyNoteCard(note: DailyNoteGI) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ProgressCard(
            icon = "⚡",
            label = "原粹树脂",
            current = note.currentResin,
            max = note.maxResin,
            subtitle = if (note.isResinFull) "已满！" else "恢复中...",
            extra = note.resinFullTime?.let {
                "预计 ${formatTimeRemaining(it.epochSecond - System.currentTimeMillis() / 1000)}"
            },
        )

        DailyCommissionCard(
            icon = "📋",
            label = "每日委托",
            finished = note.finishedTaskNum,
            total = note.totalTaskNum,
            extra = if (note.isExtraTaskRewardReceived) "奖励已领" else null,
        )

        ProgressCard(
            icon = "🏠",
            label = "洞天宝钱",
            current = note.currentHomeCoin,
            max = note.maxHomeCoin,
            subtitle = if (note.currentHomeCoin >= note.maxHomeCoin) "已满！" else "积累中...",
        )

        ExpeditionCardGI(note)

        TransformerCard(note.transformer)

        StatusCard(
            icon = "🐉",
            label = "周本折扣",
            value = "${note.remainResinDiscountNum}/${note.resinDiscountNumLimit} 剩余",
        )
    }
}

// ---------- Star Rail ----------

@Composable
private fun StarRailDailyNoteCard(note: DailyNoteHSR) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ProgressCard(
            icon = "⚡",
            label = "开拓力",
            current = note.currentStamina,
            max = note.maxStamina,
            subtitle = if (note.isStaminaFull) "已满！" else "恢复中...",
            extra = note.staminaFullTime?.let {
                "预计 ${formatTimeRemaining(it.epochSecond - System.currentTimeMillis() / 1000)}"
            },
        )

        if (note.currentReserveStamina > 0) {
            StatusCard(
                icon = "🔋",
                label = "后备开拓力",
                value = "${note.currentReserveStamina}",
            )
        }

        ExpeditionCardHSR(note)

        StatusCard(
            icon = "🌌",
            label = "模拟宇宙积分",
            value = "${note.currentTrainScore}/${note.maxTrainScore}",
        )
    }
}

// ---------- ZZZ ----------

@Composable
private fun ZZZDailyNoteCard(note: DailyNoteZZZ) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ProgressCard(
            icon = "⚡",
            label = "电量",
            current = note.energy.progress,
            max = note.energy.maxProgress,
            subtitle = "每6分钟恢复 ${note.energy.restore}",
        )

        ProgressCard(
            icon = "💪",
            label = "活力",
            current = note.vitality.current,
            max = note.vitality.max,
            subtitle = if (note.vitality.current >= note.vitality.max) "已满！" else "恢复中...",
        )

        StatusCard(
            icon = "📋",
            label = "悬赏委托",
            value = "${note.bountyCommission.num}/${note.bountyCommission.total}",
        )

        StatusCard(
            icon = "🔍",
            label = "调查点数",
            value = "${note.surveyPoints.num}/${note.surveyPoints.total}",
        )
    }
}

// ========== Expedition GI ==========

@Composable
private fun ExpeditionCardGI(note: DailyNoteGI) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        pressFeedbackType = PressFeedbackType.Sink,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "探索派遣",
                style = MiuixTheme.textStyles.title2,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            val runningCount = note.expeditions.count { it.status == "Ongoing" }
            val finishedCount = note.expeditions.count { it.status == "Finished" }
            Text(
                text = "$runningCount 派遣中 · $finishedCount 已完成 · ${note.maxExpeditionNum} 上限",
                style = MiuixTheme.textStyles.body2,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
            val ongoingExpeditions = note.expeditions.filter { it.status == "Ongoing" }
            if (ongoingExpeditions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                val shortest = ongoingExpeditions.minByOrNull {
                    it.remainingTimeSeconds.toIntOrNull() ?: Int.MAX_VALUE
                }
                shortest?.let { exp ->
                    val remaining = exp.remainingTimeSeconds.toIntOrNull() ?: 0
                    val hours = remaining / 3600
                    val minutes = (remaining % 3600) / 60
                    val seconds = remaining % 60
                    Text(
                        text = if (hours > 0) "最近完成: ${hours}时${minutes}分${seconds}秒"
                        else "最近完成: ${minutes}分${seconds}秒",
                        style = MiuixTheme.textStyles.main,
                        color = MiuixTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

// ========== Expedition HSR ==========

@Composable
private fun ExpeditionCardHSR(note: DailyNoteHSR) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        pressFeedbackType = PressFeedbackType.Sink,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "委托派遣",
                style = MiuixTheme.textStyles.title2,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            val runningCount = note.expeditions.count { it.status == "Running" }
            val finishedCount = note.expeditions.count { it.status == "Finished" }
            Text(
                text = "$runningCount 派遣中 · $finishedCount 已完成 · ${note.totalExpeditionNum} 上限",
                style = MiuixTheme.textStyles.body2,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
        }
    }
}

// ========== Transformer ==========

@Composable
private fun TransformerCard(transformer: TransformerGI?) {
    if (transformer == null) return
    Card(
        modifier = Modifier.fillMaxWidth(),
        pressFeedbackType = PressFeedbackType.Sink,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "参量质变仪",
                style = MiuixTheme.textStyles.title2,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            val recovery = transformer.recoveryTime
            if (recovery != null && recovery.reached) {
                Text(
                    text = "✅ 可以使用",
                    style = MiuixTheme.textStyles.body2,
                    color = MiuixTheme.colorScheme.primary,
                )
            } else if (recovery != null) {
                Text(
                    text = "⏳ 剩余 ${recovery.day}天${recovery.hour}时${recovery.minute}分${recovery.second}秒",
                    style = MiuixTheme.textStyles.body2,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            } else {
                Text(
                    text = "已获得：${if (transformer.obtained) "是" else "否"}",
                    style = MiuixTheme.textStyles.body2,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
        }
    }
}

// ========== Shared Components ==========

@Composable
private fun ProgressCard(
    icon: String,
    label: String,
    current: Int,
    max: Int,
    subtitle: String,
    extra: String? = null,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        pressFeedbackType = PressFeedbackType.Sink,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = icon, style = MiuixTheme.textStyles.headline2)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = label,
                        style = MiuixTheme.textStyles.title2,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "$current / $max",
                        style = MiuixTheme.textStyles.body2,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            val progress = if (max > 0) current.toFloat() / max else 0f
            LinearProgressIndicator(
                progress = progress.coerceIn(0f, 1f),
                modifier = Modifier.fillMaxWidth(),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = subtitle,
                    style = MiuixTheme.textStyles.main,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
                if (extra != null) {
                    Text(
                        text = extra,
                        style = MiuixTheme.textStyles.main,
                        color = MiuixTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyCommissionCard(
    icon: String,
    label: String,
    finished: Int,
    total: Int,
    extra: String? = null,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        pressFeedbackType = PressFeedbackType.Sink,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = icon, style = MiuixTheme.textStyles.headline2)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = label,
                    style = MiuixTheme.textStyles.title2,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "$finished/$total 完成" + (extra?.let { " · $it" } ?: ""),
                    style = MiuixTheme.textStyles.body2,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
        }
    }
}

@Composable
private fun StatusCard(
    icon: String,
    label: String,
    value: String,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        pressFeedbackType = PressFeedbackType.Sink,
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Text(text = icon, style = MiuixTheme.textStyles.headline2)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = label,
                    style = MiuixTheme.textStyles.title2,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = value,
                    style = MiuixTheme.textStyles.body2,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
        }
    }
}

// ========== Utilities ==========

private fun formatTimeRemaining(totalSeconds: Long): String {
    val days = totalSeconds / 86400
    val hours = (totalSeconds % 86400) / 3600
    val minutes = (totalSeconds % 3600) / 60
    return when {
        days > 0 -> "${days}天${hours}时${minutes}分"
        hours > 0 -> "${hours}时${minutes}分"
        minutes > 0 -> "${minutes}分"
        else -> "${totalSeconds}秒"
    }
}

private fun formatMonthsRemaining(totalSeconds: Long): String {
    val days = totalSeconds / 86400
    val hours = (totalSeconds % 86400) / 3600
    return when {
        days > 0 -> "${days}天${hours}时"
        hours > 0 -> "${hours}时"
        else -> "即将结束"
    }
}

private fun materialDayLabel(dayOfWeek: Int): String = when (dayOfWeek) {
    java.util.Calendar.MONDAY -> "周一"
    java.util.Calendar.TUESDAY -> "周二"
    java.util.Calendar.WEDNESDAY -> "周三"
    java.util.Calendar.THURSDAY -> "周四"
    java.util.Calendar.FRIDAY -> "周五"
    java.util.Calendar.SATURDAY -> "周六"
    java.util.Calendar.SUNDAY -> "周日"
    else -> ""
}