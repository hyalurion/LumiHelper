package com.chronie.helperunited.ui.zzz

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.chronie.helperunited.model.ZZZAgentSummarized
import com.chronie.helperunited.model.ZZZDriveDiscSummarized
import com.chronie.helperunited.model.ZZZElementType
import com.chronie.helperunited.model.ZZZProfileSummarized
import com.chronie.helperunited.model.ZZZWEngineSummarized
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.utils.PressFeedbackType
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * ZZZ agent showcase screen using MIUIX components.
 * Displays agent list with W-Engine, Drive Discs, and skill info.
 */
@Composable
fun ZZZShowcaseScreen(
    profile: ZZZProfileSummarized?,
    isLoading: Boolean,
    error: String?,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
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
                            text = error,
                            style = MiuixTheme.textStyles.body2,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "💡 请在账号管理中添加 ZZZ 账号后重试",
                            style = MiuixTheme.textStyles.main,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        )
                    }
                }
            }
            profile != null -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // Profile Header
                    item {
                        ZZZProfileHeader(profile)
                    }

                    // Agent Cards
                    items(profile.agentList) { agent ->
                        ZZZAgentCard(agent)
                    }
                }
            }
            else -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Card(modifier = Modifier.fillMaxWidth(), pressFeedbackType = PressFeedbackType.Sink) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = "🎮",
                                style = MiuixTheme.textStyles.headline1,
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "绝区零角色展柜",
                                style = MiuixTheme.textStyles.title1,
                                fontWeight = FontWeight.Bold,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "请先在账号管理中添加 ZZZ 账号",
                                style = MiuixTheme.textStyles.body2,
                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ZZZProfileHeader(profile: ZZZProfileSummarized) {
    Card(modifier = Modifier.fillMaxWidth(), pressFeedbackType = PressFeedbackType.Sink) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AsyncImage(
                model = profile.headIconUrl,
                contentDescription = "头像",
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MiuixTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.Crop,
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = profile.nickname.ifEmpty { "绳匠" },
                    style = MiuixTheme.textStyles.headline3,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Lv.${profile.level} · ${profile.agentList.size} 位代理人",
                    style = MiuixTheme.textStyles.body2,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ZZZAgentCard(agent: ZZZAgentSummarized) {
    val elementColor = when (agent.elementType) {
        ZZZElementType.FIRE -> MiuixTheme.colorScheme.error
        ZZZElementType.ICE -> MiuixTheme.colorScheme.primary
        ZZZElementType.ELECTRIC -> MiuixTheme.colorScheme.onSurfaceContainerVariant
        ZZZElementType.ETHER -> MiuixTheme.colorScheme.secondary
        ZZZElementType.PHYSICAL -> MiuixTheme.colorScheme.onSurface
    }

    Card(modifier = Modifier.fillMaxWidth(), pressFeedbackType = PressFeedbackType.Sink) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Agent Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = agent.iconUrl,
                    contentDescription = agent.name,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MiuixTheme.colorScheme.surfaceVariant),
                    contentScale = ContentScale.Crop,
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = agent.name,
                        style = MiuixTheme.textStyles.title1,
                        fontWeight = FontWeight.Bold,
                    )
                    if (agent.fullName != null && agent.fullName != agent.name) {
                        Text(
                            text = agent.fullName,
                            style = MiuixTheme.textStyles.main,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatChip("Lv.${agent.level}")
                        StatChip("${agent.elementType.icon} ${agent.elementType.displayName}")
                        StatChip(starText(agent.rarity))
                        if (agent.rank > 0) {
                            StatChip("影画 ${agent.rank}")
                        }
                    }
                }
            }

            // W-Engine
            agent.wEngine?.let { wEngine ->
                Spacer(modifier = Modifier.height(12.dp))
                WEngineSection(wEngine)
            }

            // Drive Discs
            if (agent.driveDiscs.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                DriveDiscSection(agent.driveDiscs)
            }
        }
    }
}

@Composable
private fun WEngineSection(wEngine: ZZZWEngineSummarized) {
    Card(modifier = Modifier.fillMaxWidth(), pressFeedbackType = PressFeedbackType.Sink) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AsyncImage(
                model = wEngine.iconUrl,
                contentDescription = wEngine.name,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MiuixTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.Crop,
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "🔫 ${wEngine.name}",
                    style = MiuixTheme.textStyles.title3,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Lv.${wEngine.level} · 精炼 ${wEngine.enhancementLevel} · ${starText(wEngine.rarity)}",
                    style = MiuixTheme.textStyles.main,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DriveDiscSection(driveDiscs: List<ZZZDriveDiscSummarized>) {
    Column {
        Text(
            text = "💿 驱动盘",
            style = MiuixTheme.textStyles.title3,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            driveDiscs.sortedBy { it.slot }.forEach { disc ->
                DriveDiscItem(disc)
            }
        }
    }
}

@Composable
private fun DriveDiscItem(disc: ZZZDriveDiscSummarized) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(56.dp),
    ) {
        AsyncImage(
            model = disc.iconUrl,
            contentDescription = disc.name,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MiuixTheme.colorScheme.surfaceVariant),
            contentScale = ContentScale.Crop,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "+${disc.level}",
            style = MiuixTheme.textStyles.main,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "#${disc.slot}",
            style = MiuixTheme.textStyles.main,
            textAlign = TextAlign.Center,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        )
    }
}

@Composable
private fun StatChip(text: String) {
    Card(modifier = Modifier.fillMaxWidth(), pressFeedbackType = PressFeedbackType.Sink) {
        Text(
            text = text,
            style = MiuixTheme.textStyles.main,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}

private fun starText(rarity: Int): String = when (rarity) {
    5 -> "S ★★★★★"
    4 -> "A ★★★★"
    3 -> "B ★★★"
    else -> "★$rarity"
}
