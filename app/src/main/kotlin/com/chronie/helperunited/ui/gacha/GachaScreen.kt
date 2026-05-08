package com.chronie.helperunited.ui.gacha

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
import top.yukonga.miuix.kmp.basic.MiuixCard
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * Gacha records screen using MIUIX components.
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
) {
    var uidInput by remember { mutableStateOf("") }
    var gameInput by remember { mutableStateOf("gi") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        // Input Card
        MiuixCard(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
            ) {
                Text(
                    text = "获取抽卡记录",
                    style = MiuixTheme.textStyles.titleMedium,
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

                // UID Input
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
                        Text("获取")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Profile List
        if (profiles.isNotEmpty()) {
            MiuixCard(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                ) {
                    Text(
                        text = "已保存的记录",
                        style = MiuixTheme.textStyles.titleSmall,
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
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Content
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
            error != null -> {
                MiuixCard(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                    ) {
                        Text(
                            text = "提示",
                            style = MiuixTheme.textStyles.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MiuixTheme.colorScheme.primary,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = error,
                            style = MiuixTheme.textStyles.bodyMedium,
                        )
                    }
                }
            }
            entries.isNotEmpty() -> {
                // Statistics
                GachaStatistics(entries)

                Spacer(modifier = Modifier.height(16.dp))

                // Entry List
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(entries.take(100)) { entry ->
                        GachaEntryCard(entry)
                    }
                    if (entries.size > 100) {
                        item {
                            MiuixCard(
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(
                                    text = "还有 ${entries.size - 100} 条记录...",
                                    modifier = Modifier.padding(16.dp),
                                    style = MiuixTheme.textStyles.bodySmall,
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
    Button(
        onClick = onClick,
    ) {
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
) {
    Button(
        onClick = onClick,
    ) {
        Text(
            text = "${profile.uid} (${profile.game.uppercase()})",
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GachaStatistics(entries: List<GachaEntry>) {
    val total = entries.size
    val fiveStar = entries.count { it.rankType == 5 }
    val fourStar = entries.count { it.rankType == 4 }
    val threeStar = entries.count { it.rankType == 3 }

    MiuixCard(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = "统计",
                style = MiuixTheme.textStyles.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(12.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                StatItem("总抽数", total.toString())
                StatItem("五星", fiveStar.toString())
                StatItem("四星", fourStar.toString())
                StatItem("三星", threeStar.toString())
                if (total > 0) {
                    StatItem("五星率", "${(fiveStar * 100.0 / total).format(1)}%")
                }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value,
            style = MiuixTheme.textStyles.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = label,
            style = MiuixTheme.textStyles.bodySmall,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        )
    }
}

@Composable
private fun GachaEntryCard(entry: GachaEntry) {
    val rarityColor = when (entry.rankType) {
        5 -> MiuixTheme.colorScheme.primary
        4 -> MiuixTheme.colorScheme.tertiary
        else -> MiuixTheme.colorScheme.onSurface
    }

    MiuixCard(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = entry.name ?: "Unknown",
                    style = MiuixTheme.textStyles.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = rarityColor,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${entry.itemType ?: ""} · ${entry.time}",
                    style = MiuixTheme.textStyles.bodySmall,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
            Text(
                text = when (entry.rankType) {
                    5 -> "★★★★★"
                    4 -> "★★★★"
                    else -> "★★★"
                },
                style = MiuixTheme.textStyles.bodyMedium,
                color = rarityColor,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

private fun Double.format(digits: Int) = "%.${digits}f".format(this)
