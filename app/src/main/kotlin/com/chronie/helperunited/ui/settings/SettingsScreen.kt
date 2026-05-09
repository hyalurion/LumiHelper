package com.chronie.helperunited.ui.settings

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.MiuixCard
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * Settings / About screen.
 * Shows app info, version, and useful links.
 * Ported from PizzaHelperUnited's PZAboutKit.
 */
@Composable
fun SettingsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // App Info
        MiuixCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "🍕 Helper United",
                    style = MiuixTheme.textStyles.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Kotlin/Android Edition",
                    style = MiuixTheme.textStyles.bodyMedium,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "版本 1.0.0 (Build 1)",
                    style = MiuixTheme.textStyles.bodySmall,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
        }

        // Supported Games
        MiuixCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "支持的游戏",
                    style = MiuixTheme.textStyles.titleMedium,
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
        MiuixCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "功能",
                    style = MiuixTheme.textStyles.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(12.dp))
                FeatureRow("⚡", "每日便笺", "实时查看树脂/开拓力/电量")
                FeatureRow("🎭", "角色展柜", "Enka.Network 角色详情")
                FeatureRow("🎰", "抽卡记录", "记录分析、UIGF 导入导出")
                FeatureRow("👤", "账号管理", "多账号支持")
            }
        }

        // Links
        MiuixCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "相关链接",
                    style = MiuixTheme.textStyles.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(12.dp))
                LinkRow("GitHub", "pizza-studio/PizzaHelperUnited")
                LinkRow("App Store", "The Latte Helper")
                LinkRow("Enka.Network", "角色展柜数据源")
            }
        }

        // Credits
        MiuixCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "致谢",
                    style = MiuixTheme.textStyles.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "• Pizza Studio 团队：原始 iOS 应用开发",
                    style = MiuixTheme.textStyles.bodySmall,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
                Text(
                    text = "• Enka.Network：角色展柜数据 API",
                    style = MiuixTheme.textStyles.bodySmall,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
                Text(
                    text = "• MIUIX：UI 组件库",
                    style = MiuixTheme.textStyles.bodySmall,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
        }

        // License
        MiuixCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "开源许可",
                    style = MiuixTheme.textStyles.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "本项目基于 AGPL-3.0 许可证开源",
                    style = MiuixTheme.textStyles.bodySmall,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "原始项目: (c) 2024 Pizza Studio",
                    style = MiuixTheme.textStyles.bodySmall,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
        }
    }
}

@Composable
private fun GameRow(emoji: String, name: String, subtitle: String, status: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = emoji, style = MiuixTheme.textStyles.headlineSmall)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = MiuixTheme.textStyles.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = subtitle,
                style = MiuixTheme.textStyles.bodySmall,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
        }
        Text(
            text = status,
            style = MiuixTheme.textStyles.bodySmall,
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
        Text(text = emoji, style = MiuixTheme.textStyles.titleMedium)
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                style = MiuixTheme.textStyles.bodyMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = description,
                style = MiuixTheme.textStyles.bodySmall,
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
            style = MiuixTheme.textStyles.bodyMedium,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = subtitle,
            style = MiuixTheme.textStyles.bodySmall,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        )
    }
}
