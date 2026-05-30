package com.chronie.helperunited.ui.ledger

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.chronie.helperunited.model.GILedgerDataGroup
import com.chronie.helperunited.model.HSRLedgerDataGroup
import com.chronie.helperunited.model.LedgerData4GI
import com.chronie.helperunited.model.LedgerData4HSR
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.PullToRefresh
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.rememberPullToRefreshState
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType

@Composable
fun LedgerScreen(
    viewModel: LedgerViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val pullToRefreshState = rememberPullToRefreshState()

    LaunchedEffect(uiState.isLoading) {
        if (uiState.isLoading) {
            pullToRefreshState.isRefreshing = true
        } else {
            pullToRefreshState.isRefreshing = false
        }
    }

    PullToRefresh(
        isRefreshing = uiState.isLoading,
        onRefresh = { viewModel.refresh() },
        pullToRefreshState = pullToRefreshState,
        modifier = Modifier.fillMaxSize(),
    ) {
        if (uiState.accounts.isEmpty()) {
            NoAccountHint()
            return@PullToRefresh
        }

        if (uiState.selectedAccount == null) {
            return@PullToRefresh
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            AccountSelector(
                accounts = uiState.accounts,
                selected = uiState.selectedAccount!!,
                onSelect = { viewModel.selectAccount(it) },
            )

            MonthNavigator(
                year = uiState.selectedYear,
                month = uiState.selectedMonth,
                onPrevious = { viewModel.previousMonth() },
                onNext = { viewModel.nextMonth() },
                availableMonths = uiState.availableMonths,
            )

            if (uiState.error != null) {
                ErrorCard(uiState.error!!)
            }

            when {
                uiState.ledgerGI != null -> GILedgerContent(uiState.ledgerGI!!)
                uiState.ledgerHSR != null -> HSRLedgerContent(uiState.ledgerHSR!!)
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

// ========== Account Selector ==========

@Composable
private fun AccountSelector(
    accounts: List<com.chronie.helperunited.model.HoYoAccount>,
    selected: com.chronie.helperunited.model.HoYoAccount,
    onSelect: (com.chronie.helperunited.model.HoYoAccount) -> Unit,
) {
    if (accounts.size <= 1) return
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        accounts.forEach { account ->
            val isSelected = account.uuid == selected.uuid
            Card(
                modifier = Modifier.clickable { onSelect(account) },
                pressFeedbackType = PressFeedbackType.Sink,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = account.game.displayName,
                        style = MiuixTheme.textStyles.main,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) MiuixTheme.colorScheme.primary
                        else MiuixTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = account.name,
                        style = MiuixTheme.textStyles.main,
                        color = if (isSelected) MiuixTheme.colorScheme.primary
                        else MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    )
                }
            }
        }
    }
}

// ========== Month Navigator ==========

@Composable
private fun MonthNavigator(
    year: Int,
    month: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    availableMonths: List<Int>,
) {
    val now = java.util.Calendar.getInstance()
    val currentYearMonth = now.get(java.util.Calendar.YEAR) * 100 + now.get(java.util.Calendar.MONTH) + 1
    val selectedYearMonth = year * 100 + month
    val canGoNext = selectedYearMonth < currentYearMonth

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Button(onClick = onPrevious) {
            Text("\u2039")
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = "${year}年${month}月",
            style = MiuixTheme.textStyles.title1,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.width(16.dp))
        Button(
            onClick = onNext,
            enabled = canGoNext,
        ) {
            Text("\u203A")
        }
    }
}

// ========== Genshin Impact Ledger ==========

@Composable
private fun GILedgerContent(data: LedgerData4GI) {
    val monthData = data.monthData

    SectionLabel("月度收入概览")

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SummaryCard(
            modifier = Modifier.weight(1f),
            emoji = "\uD83D\uDC8E",
            label = "原石",
            current = monthData.currentPrimogems,
            previous = monthData.lastPrimogems,
            rate = monthData.primogemsRate ?: monthData.primogemRate,
        )
        SummaryCard(
            modifier = Modifier.weight(1f),
            emoji = "\uD83E\uDE99",
            label = "摩拉",
            current = monthData.currentMora,
            previous = monthData.lastMora,
            rate = monthData.moraRate,
        )
    }

    if (monthData.groupBy.isNotEmpty()) {
        Spacer(modifier = Modifier.height(4.dp))
        SectionLabel("收入分类")
        CategoriesCardGI(monthData.groupBy)
    }

    if (data.dayData.currentPrimogems > 0 || data.dayData.currentMora > 0) {
        Spacer(modifier = Modifier.height(4.dp))
        SectionLabel("今日收入")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            DaySummaryCard(
                modifier = Modifier.weight(1f),
                emoji = "\uD83D\uDC8E",
                label = "原石",
                value = data.dayData.currentPrimogems,
            )
            DaySummaryCard(
                modifier = Modifier.weight(1f),
                emoji = "\uD83E\uDE99",
                label = "摩拉",
                value = data.dayData.currentMora,
            )
        }
    }
}

// ========== Star Rail Ledger ==========

@Composable
private fun HSRLedgerContent(data: LedgerData4HSR) {
    val monthData = data.monthData

    SectionLabel("月度收入概览")

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SummaryCard(
            modifier = Modifier.weight(1f),
            emoji = "\uD83D\uDC8E",
            label = "星琼",
            current = monthData.currentStellarJades,
            previous = monthData.prevStellarJade,
            rate = monthData.stellarJadeRate,
        )
        SummaryCard(
            modifier = Modifier.weight(1f),
            emoji = "\uD83C\uDFAB",
            label = "星轨通票",
            current = monthData.currentPasses,
            previous = monthData.prevPasses,
            rate = monthData.passesRate,
        )
    }

    if (monthData.groupBy.isNotEmpty()) {
        Spacer(modifier = Modifier.height(4.dp))
        SectionLabel("收入分类")
        CategoriesCardHSR(monthData.groupBy)
    }

    if (data.dayData.currentStellarJades > 0 || data.dayData.currentPasses > 0) {
        Spacer(modifier = Modifier.height(4.dp))
        SectionLabel("今日收入")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            DaySummaryCard(
                modifier = Modifier.weight(1f),
                emoji = "\uD83D\uDC8E",
                label = "星琼",
                value = data.dayData.currentStellarJades,
            )
            DaySummaryCard(
                modifier = Modifier.weight(1f),
                emoji = "\uD83C\uDFAB",
                label = "通票",
                value = data.dayData.currentPasses,
            )
        }
    }
}

// ========== Summary Card ==========

@Composable
private fun SummaryCard(
    modifier: Modifier = Modifier,
    emoji: String,
    label: String,
    current: Int,
    previous: Int,
    rate: Int?,
) {
    Card(modifier = modifier, pressFeedbackType = PressFeedbackType.Sink) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = emoji, style = MiuixTheme.textStyles.headline2)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = label,
                    style = MiuixTheme.textStyles.title2,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = current.toString(),
                style = MiuixTheme.textStyles.headline1,
                fontWeight = FontWeight.Bold,
                color = MiuixTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(4.dp))
            val diff = current - previous
            val diffStr = if (diff >= 0) "+${diff}" else "${diff}"
            val rateStr = when {
                rate == null -> ""
                rate >= 0 -> "(+${rate}%)"
                else -> "(${rate}%)"
            }
            val diffColor = if (diff >= 0) MiuixTheme.colorScheme.primary
                else MiuixTheme.colorScheme.error
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "上月: $previous",
                    style = MiuixTheme.textStyles.main,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "$diffStr $rateStr",
                    style = MiuixTheme.textStyles.main,
                    color = diffColor,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

// ========== Day Summary Card ==========

@Composable
private fun DaySummaryCard(
    modifier: Modifier = Modifier,
    emoji: String,
    label: String,
    value: Int,
) {
    Card(modifier = modifier, pressFeedbackType = PressFeedbackType.Sink) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = emoji, style = MiuixTheme.textStyles.body1)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = label,
                    style = MiuixTheme.textStyles.body2,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value.toString(),
                style = MiuixTheme.textStyles.title1,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

// ========== Categories Card (GI) ==========

@Composable
private fun CategoriesCardGI(groups: List<GILedgerDataGroup>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        pressFeedbackType = PressFeedbackType.Sink,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            groups.forEachIndexed { index, group ->
                GICategoryRow(group)
                if (index < groups.size - 1) {
                    SettingsDivider()
                }
            }
        }
    }
}

@Composable
private fun GICategoryRow(group: GILedgerDataGroup) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = group.action,
                style = MiuixTheme.textStyles.body1,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = "${group.percent}% · ${String.format("%.0f", group.num)}",
                style = MiuixTheme.textStyles.main,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
        }
        Text(
            text = "${group.percent}%",
            style = MiuixTheme.textStyles.title2,
            fontWeight = FontWeight.Bold,
            color = MiuixTheme.colorScheme.primary,
        )
    }
}

// ========== Categories Card (HSR) ==========

@Composable
private fun CategoriesCardHSR(groups: List<HSRLedgerDataGroup>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        pressFeedbackType = PressFeedbackType.Sink,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            groups.forEachIndexed { index, group ->
                HSRCategoryRow(group)
                if (index < groups.size - 1) {
                    SettingsDivider()
                }
            }
        }
    }
}

@Composable
private fun HSRCategoryRow(group: HSRLedgerDataGroup) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = group.actionName,
                style = MiuixTheme.textStyles.body1,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = "${group.percent}% · ${String.format("%.0f", group.num)}",
                style = MiuixTheme.textStyles.main,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
        }
        Text(
            text = "${group.percent}%",
            style = MiuixTheme.textStyles.title2,
            fontWeight = FontWeight.Bold,
            color = MiuixTheme.colorScheme.primary,
        )
    }
}

// ========== Shared Components ==========

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MiuixTheme.textStyles.title2,
        fontWeight = FontWeight.Bold,
        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
    )
}

@Composable
private fun SettingsDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .height(1.dp)
            .background(MiuixTheme.colorScheme.surfaceVariant),
    )
}

@Composable
private fun ErrorCard(message: String) {
    Card(modifier = Modifier.fillMaxWidth(), pressFeedbackType = PressFeedbackType.Sink) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "\u26A0\uFE0F",
                style = MiuixTheme.textStyles.title1,
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = message,
                style = MiuixTheme.textStyles.body1,
                color = MiuixTheme.colorScheme.error,
            )
        }
    }
}

@Composable
private fun NoAccountHint() {
    Box(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "\uD83D\uDC64",
                style = MiuixTheme.textStyles.headline1,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "请先添加账号",
                style = MiuixTheme.textStyles.title1,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "月度账本需要绑定 HoYoLAB 账号才能查看",
                style = MiuixTheme.textStyles.body1,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
        }
    }
}