package com.chronie.helperunited.ui.gacha

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chronie.helperunited.model.GachaEntry
import com.chronie.helperunited.model.GachaProfile
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.utils.PressFeedbackType
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * Gacha records screen.
 * Supports viewing, importing (UIGF file picker), and exporting gacha records.
 * Ported from PizzaHelperUnited's GachaKit frontend.
 */
@Composable
fun GachaScreen(
    profiles: List<GachaProfile>,
    entries: List<GachaEntry>,
    isLoading: Boolean,
    error: String?,
    selectedProfile: GachaProfile?,
    onProfileSelect: (GachaProfile) -> Unit,
    onFetch: (String, String) -> Unit,
    onImport: (String) -> Unit,
    onExport: (String, String) -> Unit,
    onImportUri: ((android.net.Uri) -> Unit)? = null,
    onExportShare: ((String, String) -> Unit)? = null,
    onDeleteProfile: ((GachaProfile) -> Unit)? = null,
    onNavigateToAnalytics: (() -> Unit)? = null,
) {
    var uidInput by remember { mutableStateOf("") }
    var gameInput by remember { mutableStateOf("gi") }

    // File picker for UIGF import
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { onImportUri?.invoke(it) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        // Input Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            pressFeedbackType = PressFeedbackType.Sink,
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "抽卡记录",
                    style = MiuixTheme.textStyles.title2,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Game Selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    GameChip("原神", gameInput == "gi") { gameInput = "gi" }
                    GameChip("星穹铁道", gameInput == "hsr") { gameInput = "hsr" }
                    GameChip("绝区零", gameInput == "zzz") { gameInput = "zzz" }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // UID Input + Fetch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextField(
                        value = uidInput,
                        onValueChange = { uidInput = it },
                        label = "输入游戏 UID",
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { if (uidInput.isNotBlank()) onFetch(uidInput, gameInput) },
                        enabled = !isLoading && uidInput.isNotBlank(),
                    ) {
                        Text("查询")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Import/Export buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        onClick = { filePickerLauncher.launch("application/json") },
                        enabled = !isLoading,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("📥 导入 UIGF")
                    }
                    Button(
                        onClick = {
                            selectedProfile?.let {
                                onExportShare?.invoke(it.uid, it.game)
                            }
                        },
                        enabled = selectedProfile != null,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("📤 导出 UIGF")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Analytics button
                if (entries.isNotEmpty() && onNavigateToAnalytics != null) {
                    Button(
                        onClick = { onNavigateToAnalytics() },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("📊 查看抽卡分析图表")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Profile List
        if (profiles.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                pressFeedbackType = PressFeedbackType.Sink,
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "已保存的记录",
                        style = MiuixTheme.textStyles.title3,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        profiles.forEach { profile ->
                            ProfileChip(
                                profile = profile,
                                isSelected = profile == selectedProfile,
                                onClick = { onProfileSelect(profile) },
                                onDelete = { onDeleteProfile?.invoke(profile) },
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Content
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
            error != null -> {
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
                        Text(text = error, style = MiuixTheme.textStyles.body2)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "💡 您可以导入 UIGF 格式的抽卡记录文件",
                            style = MiuixTheme.textStyles.main,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        )
                    }
                }
            }
            entries.isNotEmpty() -> {
                // Statistics
                GachaStatisticsCard(entries)
                Spacer(modifier = Modifier.height(12.dp))

                // Entry List (show limited for performance)
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(entries.take(200)) { entry ->
                        GachaEntryCard(entry)
                    }
                    if (entries.size > 200) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                pressFeedbackType = PressFeedbackType.Sink,
                            ) {
                                Text(
                                    text = "还有 ${entries.size - 200} 条记录未显示...",
                                    modifier = Modifier.padding(16.dp),
                                    style = MiuixTheme.textStyles.main,
                                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GameChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Button(onClick = onClick) {
        Text(
            text = label,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
        )
    }
}

@Composable
private fun ProfileChip(
    profile: GachaProfile,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDelete: (() -> Unit)? = null,
) {
    Button(onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "${profile.uid} (${profile.game.uppercase()})",
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GachaStatisticsCard(entries: List<GachaEntry>) {
    val total = entries.size
    val fiveStar = entries.count { it.rankType == 5 }
    val fourStar = entries.count { it.rankType == 4 }
    val threeStar = entries.count { it.rankType == 3 }

    // Pity calculation: pulls since last 5-star
    val lastFiveIdx = entries.indexOfFirst { it.rankType == 5 }
    val pity = if (lastFiveIdx >= 0) lastFiveIdx else total

    Card(
        modifier = Modifier.fillMaxWidth(),
        pressFeedbackType = PressFeedbackType.Sink,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "📊 统计",
                style = MiuixTheme.textStyles.title2,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(12.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                StatItem("总抽数", "$total")
                StatItem("★★★★★", "$fiveStar")
                StatItem("★★★★", "$fourStar")
                StatItem("★★★", "$threeStar")
                if (total > 0) {
                    StatItem("五星率", "${"%.1f".format(fiveStar * 100.0 / total)}%")
                }
                StatItem("保底计数", "$pity")
            }

            // Pity warning
            if (pity >= 70) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "⚠️ 距离保底还有 ${90 - pity} 抽",
                    style = MiuixTheme.textStyles.main,
                    color = MiuixTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MiuixTheme.textStyles.headline3,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = label,
            style = MiuixTheme.textStyles.main,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        )
    }
}

@Composable
private fun GachaEntryCard(entry: GachaEntry) {
    val rarityColor = when (entry.rankType) {
        5 -> MiuixTheme.colorScheme.primary
        4 -> MiuixTheme.colorScheme.onSurfaceContainerVariant
        else -> MiuixTheme.colorScheme.onSurface
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        pressFeedbackType = PressFeedbackType.Sink,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.name ?: "未知",
                    style = MiuixTheme.textStyles.title3,
                    fontWeight = FontWeight.Bold,
                    color = rarityColor,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${entry.itemType ?: ""} · ${entry.time}",
                    style = MiuixTheme.textStyles.main,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
            Text(
                text = when (entry.rankType) {
                    5 -> "★★★★★"
                    4 -> "★★★★"
                    else -> "★★★"
                },
                style = MiuixTheme.textStyles.body2,
                color = rarityColor,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
