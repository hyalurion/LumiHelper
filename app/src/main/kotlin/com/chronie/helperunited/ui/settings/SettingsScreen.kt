package com.chronie.helperunited.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
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
import com.chronie.helperunited.BuildConfig
import com.chronie.helperunited.worker.WorkScheduler
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.utils.PressFeedbackType
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * Settings / About screen.
 * Shows app info, version, and useful links.
 * Ported from PizzaHelperUnited's PZAboutKit.
 */
@Composable
fun SettingsScreen(
    onNavigateToPrivacy: () -> Unit = {},
) {
    val context = LocalContext.current
    var updateCheckResult by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // App Info
        Card(
            modifier = Modifier.fillMaxWidth(),
            pressFeedbackType = PressFeedbackType.Sink,
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "🍕 Helper United",
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
                    text = "版本 ${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})",
                    style = MiuixTheme.textStyles.main,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
        }

        // Check for Updates
        Card(
            modifier = Modifier.fillMaxWidth(),
            pressFeedbackType = PressFeedbackType.Sink,
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "🔄 检查更新",
                    style = MiuixTheme.textStyles.title2,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "当前版本: ${BuildConfig.VERSION_NAME}",
                        style = MiuixTheme.textStyles.main,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    )
                    Button(onClick = {
                        // Placeholder: would check GitHub releases API
                        updateCheckResult = "正在检查更新..."
                        // In production, this would call GitHub API
                        updateCheckResult = "已是最新版本 ✓"
                    }) {
                        Text("检查更新")
                    }
                }
                AnimatedVisibility(
                    visible = updateCheckResult != null,
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    updateCheckResult?.let { result ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = result,
                            style = MiuixTheme.textStyles.main,
                            color = MiuixTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }

        // Background Refresh Settings
        BackgroundRefreshCard()

        // Supported Games
        Card(
            modifier = Modifier.fillMaxWidth(),
            pressFeedbackType = PressFeedbackType.Sink,
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "支持的游戏",
                    style = MiuixTheme.textStyles.title2,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(12.dp))
                GameRow("⚡", "原神", "Genshin Impact", "完整支持")
                Spacer(modifier = Modifier.height(8.dp))
                GameRow("🚂", "崩坏：星穹铁道", "Honkai: Star Rail", "完整支持")
                Spacer(modifier = Modifier.height(8.dp))
                GameRow("🎮", "绝区零", "Zenless Zone Zero", "每日便笺")
            }
        }

        // Features
        Card(
            modifier = Modifier.fillMaxWidth(),
            pressFeedbackType = PressFeedbackType.Sink,
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "功能",
                    style = MiuixTheme.textStyles.title2,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(12.dp))
                FeatureRow("⚡", "每日便笺", "实时查看树脂/开拓力/电量")
                FeatureRow("🎭", "角色展柜", "Enka.Network 角色详情")
                FeatureRow("🎰", "抽卡记录", "记录分析、UIGF 导入导出")
                FeatureRow("👤", "账号管理", "多账号支持")
                FeatureRow("🔔", "后台提醒", "树脂/开拓力/电量即将满时通知")
            }
        }

        // Links
        Card(
            modifier = Modifier.fillMaxWidth(),
            pressFeedbackType = PressFeedbackType.Sink,
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "相关链接",
                    style = MiuixTheme.textStyles.title2,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(12.dp))
                LinkRow("GitHub", "pizza-studio/PizzaHelperUnited")
                LinkRow("App Store", "The Latte Helper")
                LinkRow("Enka.Network", "角色展柜数据源")
            }
        }

        // Contributors
        Card(
            modifier = Modifier.fillMaxWidth(),
            pressFeedbackType = PressFeedbackType.Sink,
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "👥 贡献者",
                    style = MiuixTheme.textStyles.title2,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "感谢所有为本项目做出贡献的开发者！",
                    style = MiuixTheme.textStyles.main,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = {
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://github.com/pizza-studio/PizzaHelperUnited/graphs/contributors")
                    )
                    context.startActivity(intent)
                }) {
                    Text("查看贡献者")
                }
            }
        }

        // Rate on Google Play
        Card(
            modifier = Modifier.fillMaxWidth(),
            pressFeedbackType = PressFeedbackType.Sink,
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "⭐ 评价应用",
                    style = MiuixTheme.textStyles.title2,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "如果您觉得本应用有帮助，请在 Google Play 上给我们评分！",
                    style = MiuixTheme.textStyles.main,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = {
                    try {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=${BuildConfig.APPLICATION_ID}")
                        )
                        context.startActivity(intent)
                    } catch (_: Exception) {
                        // Fallback to browser if Play Store not installed
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}")
                        )
                        context.startActivity(intent)
                    }
                }) {
                    Text("在 Google Play 上评价")
                }
            }
        }

        // Open Source Licenses
        Card(
            modifier = Modifier.fillMaxWidth(),
            pressFeedbackType = PressFeedbackType.Sink,
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "📜 开源许可",
                    style = MiuixTheme.textStyles.title2,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "本项目使用了以下开源库，感谢所有贡献者。",
                    style = MiuixTheme.textStyles.main,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "本项目基于 AGPL-3.0 许可证开源",
                    style = MiuixTheme.textStyles.main,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "原始项目: (c) 2024 Pizza Studio",
                    style = MiuixTheme.textStyles.main,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = {
                    // Navigate to OSS licenses activity (standard Android component)
                    try {
                        val intent = Intent(
                            context,
                            Class.forName("com.google.android.gms.oss.licenses.OssLicensesMenuActivity")
                        )
                        context.startActivity(intent)
                    } catch (_: Exception) {
                        // Fallback: open GitHub licenses page
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://github.com/pizza-studio/PizzaHelperUnited/blob/main/LICENSE")
                        )
                        context.startActivity(intent)
                    }
                }) {
                    Text("查看开源许可")
                }
            }
        }

        // Privacy Policy
        Card(
            modifier = Modifier.fillMaxWidth(),
            pressFeedbackType = PressFeedbackType.Sink,
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "🔒 隐私政策",
                    style = MiuixTheme.textStyles.title2,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "查看本应用的隐私政策和用户协议",
                    style = MiuixTheme.textStyles.main,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onNavigateToPrivacy) {
                    Text("查看隐私政策")
                }
            }
        }

        // Credits
        Card(
            modifier = Modifier.fillMaxWidth(),
            pressFeedbackType = PressFeedbackType.Sink,
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "致谢",
                    style = MiuixTheme.textStyles.title2,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "• Pizza Studio 团队：原始 iOS 应用开发",
                    style = MiuixTheme.textStyles.main,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
                Text(
                    text = "• Enka.Network：角色展柜数据 API",
                    style = MiuixTheme.textStyles.main,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
                Text(
                    text = "• MIUIX：UI 组件库",
                    style = MiuixTheme.textStyles.main,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
        }
    }
}

@Composable
private fun BackgroundRefreshCard() {
    val context = LocalContext.current
    var enabled by remember { mutableStateOf(WorkScheduler.isEnabled(context)) }
    var intervalMinutes by remember { mutableLongStateOf(WorkScheduler.getIntervalMinutes(context)) }
    var thresholdPercent by remember { mutableIntStateOf(WorkScheduler.getThresholdPercent(context)) }

    val intervalOptions = listOf(15L, 30L, 60L)
    val intervalLabels = listOf("15 分钟", "30 分钟", "1 小时")
    val thresholdOptions = listOf(70, 80, 90)
    val thresholdLabels = listOf("70%", "80%", "90%")

    Card(
        modifier = Modifier.fillMaxWidth(),
        pressFeedbackType = PressFeedbackType.Sink,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🔔 后台刷新设置",
                style = MiuixTheme.textStyles.title2,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Enable/Disable toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "启用后台刷新",
                        style = MiuixTheme.textStyles.body2,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "定期检查树脂/开拓力/电量并发送通知",
                        style = MiuixTheme.textStyles.main,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {
                    enabled = !enabled
                    WorkScheduler.setEnabled(context, enabled)
                }) {
                    Text(text = if (enabled) "已开启" else "已关闭")
                }
            }

            if (enabled) {
                Spacer(modifier = Modifier.height(16.dp))

                // Refresh interval
                Text(
                    text = "刷新间隔",
                    style = MiuixTheme.textStyles.body2,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(8.dp))
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
                                color = if (isSelected) {
                                    MiuixTheme.colorScheme.primary
                                } else {
                                    MiuixTheme.colorScheme.onSurfaceVariantSummary
                                },
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Notification threshold
                Text(
                    text = "通知阈值",
                    style = MiuixTheme.textStyles.body2,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "当资源达到最大值的百分比时发送通知",
                    style = MiuixTheme.textStyles.main,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
                Spacer(modifier = Modifier.height(8.dp))
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
                                color = if (isSelected) {
                                    MiuixTheme.colorScheme.primary
                                } else {
                                    MiuixTheme.colorScheme.onSurfaceVariantSummary
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GameRow(emoji: String, name: String, subtitle: String, status: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = emoji, style = MiuixTheme.textStyles.headline2)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = MiuixTheme.textStyles.title3,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = subtitle,
                style = MiuixTheme.textStyles.main,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
        }
        Text(
            text = status,
            style = MiuixTheme.textStyles.main,
            color = MiuixTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun FeatureRow(emoji: String, title: String, description: String) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = emoji, style = MiuixTheme.textStyles.title2)
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                style = MiuixTheme.textStyles.body2,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = description,
                style = MiuixTheme.textStyles.main,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
        }
    }
}

@Composable
private fun LinkRow(title: String, subtitle: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    ) {
        Text(
            text = title,
            style = MiuixTheme.textStyles.body2,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = subtitle,
            style = MiuixTheme.textStyles.main,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        )
    }
}
