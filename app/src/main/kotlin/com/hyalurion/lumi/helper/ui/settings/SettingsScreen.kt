package com.hyalurion.lumi.helper.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hyalurion.lumi.helper.worker.WorkScheduler
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Switch
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType

private val SECTION_HEADER_TOP = 24.dp
private val SECTION_HEADER_BOTTOM = 8.dp
private val SECTION_SPACING = 4.dp

@Composable
fun SettingsScreen(
    onNavigateToAccount: () -> Unit = {},
    onNavigateToPrivacy: () -> Unit = {},
) {
    val context = LocalContext.current
    var updateCheckResult by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(SECTION_SPACING),
    ) {
        AppInfoHeader()

        UpdateCheckSection(
            updateCheckResult = updateCheckResult,
            onCheckUpdate = {
                updateCheckResult = "正在检查更新..."
                updateCheckResult = "已是最新版本"
            },
        )

        SectionHeader(text = "账号与资料管理")
        Card(
            modifier = Modifier.fillMaxWidth(),
            pressFeedbackType = PressFeedbackType.Sink,
        ) {
            SettingsNavRow(
                icon = "\uD83D\uDC64",
                title = "账号管理",
                subtitle = "添加和管理 HoYoLAB / 米游社账号",
                onClick = onNavigateToAccount,
            )
            SettingsDivider()
            SettingsNavRow(
                icon = "\u2753",
                title = "常见问题",
                subtitle = "查看常见问题与解决方案",
                onClick = {
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://github.com/pizza-studio/PizzaHelperUnited/wiki/FAQ")
                    )
                    context.startActivity(intent)
                },
            )
        }

        SectionHeader(text = "功能设置")
        Card(
            modifier = Modifier.fillMaxWidth(),
            pressFeedbackType = PressFeedbackType.Sink,
        ) {
            BackgroundRefreshRow()
            SettingsDivider()
            SettingsButtonRow(
                icon = "\uD83D\uDDC4\uFE0F",
                title = "缓存管理",
                subtitle = "清理 Enka 数据库和图片缓存",
                buttonText = "清理",
                onClick = {

                },
            )
        }

        SectionHeader(text = "其他")
        Card(
            modifier = Modifier.fillMaxWidth(),
            pressFeedbackType = PressFeedbackType.Sink,
        ) {
            SettingsNavRow(
                icon = "\uD83D\uDD12",
                title = "隐私政策",
                subtitle = "查看应用隐私政策和用户协议",
                onClick = onNavigateToPrivacy,
            )
            SettingsDivider()
            SettingsNavRow(
                icon = "\uD83D\uDCDC",
                title = "开源许可",
                subtitle = "AGPL-3.0 · 查看开源库许可证",
                onClick = {
                    try {
                        val intent = Intent(
                            context,
                            Class.forName("com.google.android.gms.oss.licenses.OssLicensesMenuActivity")
                        )
                        context.startActivity(intent)
                    } catch (_: Exception) {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://github.com/pizza-studio/PizzaHelperUnited/blob/main/LICENSE")
                        )
                        context.startActivity(intent)
                    }
                },
            )
            SettingsDivider()
            SettingsNavRow(
                icon = "\u2B50",
                title = "评价应用",
                subtitle = "在 Google Play 上给我们评分",
                onClick = {
                    try {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=com.hyalurion.lumi.helper")
                        )
                        context.startActivity(intent)
                    } catch (_: Exception) {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://play.google.com/store/apps/details?id=com.hyalurion.lumi.helper")
                        )
                        context.startActivity(intent)
                    }
                },
            )
        }

        SectionHeader(text = "关于")
        Card(
            modifier = Modifier.fillMaxWidth(),
            pressFeedbackType = PressFeedbackType.Sink,
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Original project: (c) 2024 Pizza Studio",
                    style = MiuixTheme.textStyles.body2,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Enka.Network · MIUIX UI Library",
                    style = MiuixTheme.textStyles.body2,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

// ========== App Info Header ==========

@Composable
private fun AppInfoHeader() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        pressFeedbackType = PressFeedbackType.Sink,
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "\uD83C\uDF55 Helper United",
                style = MiuixTheme.textStyles.headline2,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Kotlin/Android Edition",
                style = MiuixTheme.textStyles.body2,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Version 1.0 (Build 1)",
                style = MiuixTheme.textStyles.main,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
        }
    }
}

// ========== Update Check ==========

@Composable
private fun UpdateCheckSection(
    updateCheckResult: String?,
    onCheckUpdate: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        pressFeedbackType = PressFeedbackType.Sink,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "检查更新",
                    style = MiuixTheme.textStyles.body1,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = "当前版本: 1.0",
                    style = MiuixTheme.textStyles.body2,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
            Button(onClick = onCheckUpdate) {
                Text("检查")
            }
        }
        AnimatedVisibility(
            visible = updateCheckResult != null,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            updateCheckResult?.let { result ->
                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp)) {
                    Text(
                        text = result,
                        style = MiuixTheme.textStyles.main,
                        color = MiuixTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

// ========== Background Refresh ==========

@Composable
private fun BackgroundRefreshRow() {
    val context = LocalContext.current
    var enabled by remember { mutableStateOf(WorkScheduler.isEnabled(context)) }
    var intervalMinutes by remember { mutableLongStateOf(WorkScheduler.getIntervalMinutes(context)) }
    var thresholdPercent by remember { mutableIntStateOf(WorkScheduler.getThresholdPercent(context)) }

    val intervalOptions = listOf(15L, 30L, 60L)
    val intervalLabels = listOf("15 min", "30 min", "1 h")
    val thresholdOptions = listOf(70, 80, 90)
    val thresholdLabels = listOf("70%", "80%", "90%")

    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "\uD83D\uDD14  后台刷新",
                    style = MiuixTheme.textStyles.body1,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = "定期检查树脂/开拓力/电量并发送通知",
                    style = MiuixTheme.textStyles.body2,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Switch(
                checked = enabled,
                onCheckedChange = {
                    enabled = it
                    WorkScheduler.setEnabled(context, it)
                },
            )
        }

        AnimatedVisibility(visible = enabled, enter = fadeIn(), exit = fadeOut()) {
            Column {
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "刷新间隔",
                    style = MiuixTheme.textStyles.body2,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    intervalOptions.forEachIndexed { index, minutes ->
                        val isSelected = intervalMinutes == minutes
                        Button(
                            onClick = {
                                intervalMinutes = minutes
                                WorkScheduler.setIntervalMinutes(context, minutes)
                            },
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(
                                text = intervalLabels[index],
                                color = if (isSelected) MiuixTheme.colorScheme.primary
                                else MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "通知阈值",
                    style = MiuixTheme.textStyles.body2,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "资源达到最大值百分比时发送通知",
                    style = MiuixTheme.textStyles.main,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    thresholdOptions.forEachIndexed { index, percent ->
                        val isSelected = thresholdPercent == percent
                        Button(
                            onClick = {
                                thresholdPercent = percent
                                WorkScheduler.setThresholdPercent(context, percent)
                            },
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(
                                text = thresholdLabels[index],
                                color = if (isSelected) MiuixTheme.colorScheme.primary
                                else MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            )
                        }
                    }
                }
            }
        }
    }
}

// ========== Shared Components ==========

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
private fun SettingsNavRow(
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

@Composable
private fun SettingsButtonRow(
    icon: String,
    title: String,
    subtitle: String,
    buttonText: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
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
        Button(onClick = onClick) {
            Text(buttonText)
        }
    }
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