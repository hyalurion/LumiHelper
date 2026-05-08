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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import top.yukonga.miuix.kmp.basic.MiuixCard

/**
 * Gacha records screen.
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
    var showImportDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        // UID Input
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = uidInput,
                onValueChange = { uidInput = it },
                label = { Text("UID") },
                modifier = Modifier.weight(1f),
                singleLine = true,
            )
            Spacer(modifier = Modifier.width(8.dp))
            TextButton(
                onClick = { if (uidInput.isNotBlank()) onFetch(uidInput, gameInput) },
                enabled = !isLoading && uidInput.isNotBlank(),
            ) {
                Text("获取")
            }
        }

        // Game Selection
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            GameChip("原神", gameInput == "gi") { gameInput = "gi" }
            GameChip("星穹铁道", gameInput == "hsr") { gameInput = "hsr" }
            GameChip("绝区零", gameInput == "zzz") { gameInput = "zzz" }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Profile List
        if (profiles.isNotEmpty()) {
            Text(
                text = "已保存的记录",
                style = MaterialTheme.typography.titleSmall,
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
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                    ),
                ) {
                    Text(
                        text = error,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                    )
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
                            Text(
                                text = "还有 ${entries.size - 100} 条记录...",
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GameChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    TextButton(onClick = onClick) {
        Text(
            text = label,
            color = if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun ProfileChip(
    profile: GachaProfile,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    TextButton(onClick = onClick) {
        Text(
            text = "${profile.uid} (${profile.game.uppercase()})",
            color = if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurface,
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
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
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
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun GachaEntryCard(entry: GachaEntry) {
    val rarityColor = when (entry.rankType) {
        5 -> MaterialTheme.colorScheme.primary
        4 -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.onSurface
    }

    MiuixCard(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = entry.name ?: "Unknown",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = rarityColor,
                )
                Text(
                    text = "${entry.itemType ?: ""} · ${entry.time}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = when (entry.rankType) {
                    5 -> "★★★★★"
                    4 -> "★★★★"
                    else -> "★★★"
                },
                style = MaterialTheme.typography.bodySmall,
                color = rarityColor,
            )
        }
    }
}

private fun Double.format(digits: Int) = "%.${digits}f".format(this)
