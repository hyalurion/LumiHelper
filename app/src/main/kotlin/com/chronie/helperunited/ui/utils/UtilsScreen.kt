package com.chronie.helperunited.ui.utils

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.chronie.helperunited.ui.sign.SignUiState
import com.chronie.helperunited.ui.sign.SignViewModel
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType

private val SECTION_HEADER_TOP = 24.dp
private val SECTION_HEADER_BOTTOM = 8.dp
private val SECTION_SPACING = 4.dp

enum class UtilsSubPage(val title: String) {
    Account("账号管理"),
    Characters("角色列表"),
    Gacha("抽卡记录"),
    Materials("今日素材"),
    Challenge("挑战记录"),
    ZZZShowcase("绝区零展柜"),
    GachaAnalytics("抽卡分析"),
    EventCalendar("活动日历"),
}

@Composable
fun UtilsScreen(
    onNavigate: (UtilsSubPage) -> Unit,
) {
    val signVm: SignViewModel = hiltViewModel()
    val signState by signVm.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(SECTION_SPACING),
    ) {
        AppHeader()

        SignInCard(
            signState = signState,
            onSignInAll = { signVm.signInAll() },
            onRefresh = { signVm.loadSignStatus() },
        )

        SectionHeader(text = "抽卡记录")
        Card(
            modifier = Modifier.fillMaxWidth(),
            pressFeedbackType = PressFeedbackType.Sink,
        ) {
            UtilsNavRow(
                icon = "\uD83C\uDFB0",
                title = "抽卡记录",
                subtitle = "导入 UIGF 格式抽卡记录、查看统计",
                onClick = { onNavigate(UtilsSubPage.Gacha) },
            )
            SettingsDivider()
            UtilsNavRow(
                icon = "\uD83D\uDCCA",
                title = "抽卡分析",
                subtitle = "保底统计、稀有度分布、抽卡图表",
                onClick = { onNavigate(UtilsSubPage.GachaAnalytics) },
            )
        }

        SectionHeader(text = "壁纸画廊")
        Card(
            modifier = Modifier.fillMaxWidth(),
            pressFeedbackType = PressFeedbackType.Sink,
        ) {
            UtilsNavRow(
                icon = "\uD83D\uDDBC\uFE0F",
                title = "壁纸画廊",
                subtitle = "浏览游戏官方壁纸和插图",
                onClick = {

                },
            )
            SettingsDivider()
            UtilsNavRow(
                icon = "\uD83D\uDCF1",
                title = "我的壁纸",
                subtitle = "管理自定义壁纸收藏",
                onClick = {

                },
            )
        }

        SectionHeader(text = "快捷功能")
        Card(
            modifier = Modifier.fillMaxWidth(),
            pressFeedbackType = PressFeedbackType.Sink,
        ) {
            UtilsNavRow(
                icon = "\uD83D\uDCC5",
                title = "今日素材",
                subtitle = "查看今天可以刷哪些天赋/武器素材",
                onClick = { onNavigate(UtilsSubPage.Materials) },
            )
            SettingsDivider()
            UtilsNavRow(
                icon = "\uD83D\uDC64",
                title = "账号管理",
                subtitle = "添加和管理 HoYoLAB / 米游社账号",
                onClick = { onNavigate(UtilsSubPage.Account) },
            )
            SettingsDivider()
            UtilsNavRow(
                icon = "\uD83D\uDC65",
                title = "角色列表",
                subtitle = "查看 HoYoLAB 角色信息、天赋、命之座",
                onClick = { onNavigate(UtilsSubPage.Characters) },
            )
            SettingsDivider()
            UtilsNavRow(
                icon = "\uD83C\uDFAE",
                title = "绝区零展柜",
                subtitle = "查看 ZZZ 角色、W-Engine、驱动盘",
                onClick = { onNavigate(UtilsSubPage.ZZZShowcase) },
            )
            SettingsDivider()
            UtilsNavRow(
                icon = "\uD83C\uDF19",
                title = "挑战记录",
                subtitle = "深境螺旋 · 混沌回忆 · 虚构叙事",
                onClick = { onNavigate(UtilsSubPage.Challenge) },
            )
            SettingsDivider()
            UtilsNavRow(
                icon = "\uD83D\uDCC5",
                title = "活动日历",
                subtitle = "查看当前和即将开始的游戏活动",
                onClick = { onNavigate(UtilsSubPage.EventCalendar) },
            )
        }

        SectionHeader(text = "支持的游戏")
        Card(
            modifier = Modifier.fillMaxWidth(),
            pressFeedbackType = PressFeedbackType.Sink,
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                GameRow("\u26A1", "原神", "Genshin Impact", "每日便笺 · 角色展柜 · 抽卡记录")
                Spacer(modifier = Modifier.height(12.dp))
                GameRow("\uD83D\uDE82", "崩坏：星穹铁道", "Honkai: Star Rail", "每日便笺 · 角色展柜 · 抽卡记录")
                Spacer(modifier = Modifier.height(12.dp))
                GameRow("\uD83C\uDFAE", "绝区零", "Zenless Zone Zero", "每日便笺 · 角色展柜 · 抽卡记录")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

// ========== App Header ==========

@Composable
private fun AppHeader() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        pressFeedbackType = PressFeedbackType.Sink,
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "\uD83C\uDF55 Helper United",
                style = MiuixTheme.textStyles.headline1,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Kotlin/Android Edition · 原神 · 星穹铁道 · 绝区零",
                style = MiuixTheme.textStyles.body2,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
        }
    }
}

// ========== Sign In ==========

@Composable
private fun SignInCard(
    signState: SignUiState,
    onSignInAll: () -> Unit,
    onRefresh: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth(), pressFeedbackType = PressFeedbackType.Sink) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = "\uD83D\uDCDD", style = MiuixTheme.textStyles.headline2)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "HoYoLAB 签到",
                        style = MiuixTheme.textStyles.title2,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "每日签到获取奖励",
                        style = MiuixTheme.textStyles.main,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    )
                }
                Button(
                    onClick = onRefresh,
                    enabled = !signState.isLoading && !signState.isSigning,
                ) {
                    Text("刷新")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onSignInAll,
                    enabled = !signState.isLoading && !signState.isSigning,
                ) {
                    Text(if (signState.isSigning) "签到中..." else "一键签到")
                }
            }

            if (signState.error != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = signState.error,
                    style = MiuixTheme.textStyles.main,
                    color = MiuixTheme.colorScheme.error,
                )
            }

            AnimatedVisibility(
                visible = signState.signStatuses.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))
                    signState.signStatuses.forEach { status ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = if (status.isSigned) "\u2705" else "\u274C",
                                style = MiuixTheme.textStyles.body2,
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = status.game.displayName,
                                style = MiuixTheme.textStyles.body2,
                                fontWeight = FontWeight.Bold,
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            if (status.error != null) {
                                Text(
                                    text = status.error!!,
                                    style = MiuixTheme.textStyles.main,
                                    color = MiuixTheme.colorScheme.error,
                                )
                            } else {
                                Text(
                                    text = if (status.isSigned) "已签到 (${status.totalSignDays}天)"
                                    else "未签到",
                                    style = MiuixTheme.textStyles.main,
                                    color = if (status.isSigned) MiuixTheme.colorScheme.primary
                                    else MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                )
                            }
                        }
                    }
                }
            }

            if (signState.isLoading) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

// ========== Utils Row ==========

@Composable
private fun UtilsNavRow(
    icon: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = icon, style = MiuixTheme.textStyles.headline2)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MiuixTheme.textStyles.body1,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = subtitle,
                style = MiuixTheme.textStyles.body2,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
        }
    }
}

// ========== Shared ==========

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MiuixTheme.textStyles.title2,
        fontWeight = FontWeight.Bold,
        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        modifier = Modifier.padding(top = SECTION_HEADER_TOP, bottom = SECTION_HEADER_BOTTOM),
    )
}

@Composable
private fun SettingsDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(1.dp)
            .background(MiuixTheme.colorScheme.surfaceVariant),
    )
}

@Composable
private fun GameRow(emoji: String, name: String, subtitle: String, features: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = emoji, style = MiuixTheme.textStyles.headline2)
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = name,
                style = MiuixTheme.textStyles.title2,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = subtitle,
                style = MiuixTheme.textStyles.main,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
            Text(
                text = features,
                style = MiuixTheme.textStyles.main,
                color = MiuixTheme.colorScheme.primary,
            )
        }
    }
}