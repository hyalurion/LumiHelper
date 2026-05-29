package com.chronie.helperunited.ui.challenge

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.chronie.helperunited.model.AbyssAvatar
import com.chronie.helperunited.model.AbyssBattle
import com.chronie.helperunited.model.AbyssFloor
import com.chronie.helperunited.model.ChallengeAvatar
import com.chronie.helperunited.model.ChallengeFloor
import com.chronie.helperunited.model.ChallengeNode
import com.chronie.helperunited.model.ChallengeResponse
import com.chronie.helperunited.model.HoYoAccount
import com.chronie.helperunited.model.HsrChallengeType
import com.chronie.helperunited.model.SpiralAbyssResponse
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.utils.PressFeedbackType
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * Challenge Record screen - shows Spiral Abyss (GI) and
 * Memory of Chaos / Pure Fiction (HSR) data.
 */
@Composable
fun ChallengeScreen(
    viewModel: ChallengeViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Account selector
        if (uiState.accounts.isNotEmpty()) {
            ChallengeAccountSelector(
                accounts = uiState.accounts,
                selectedAccount = uiState.selectedAccount,
                onSelect = { viewModel.selectAccount(it) },
            )
        }

        // Header with refresh
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "挑战记录",
                style = MiuixTheme.textStyles.headline2,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            Button(
                onClick = { viewModel.refresh() },
                enabled = !uiState.isLoading,
            ) {
                Text("刷新")
            }
        }

        // Content
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null -> {
                ErrorCard(uiState.error!!)
            }
            uiState.spiralAbyss != null -> {
                SpiralAbyssContent(uiState.spiralAbyss!!)
            }
            uiState.challengeHSR != null -> {
                HsrChallengeContent(
                    data = uiState.challengeHSR!!,
                    selectedType = uiState.selectedHsrChallengeType,
                    onTypeSelect = { viewModel.selectHsrChallengeType(it) },
                )
            }
        }
    }
}

// ========== Account Selector ==========

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChallengeAccountSelector(
    accounts: List<HoYoAccount>,
    selectedAccount: HoYoAccount?,
    onSelect: (HoYoAccount) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth(), pressFeedbackType = PressFeedbackType.Sink) {
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

// ========== Error Card ==========

@Composable
private fun ErrorCard(message: String) {
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
                text = message,
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

// ========== Spiral Abyss (GI) ==========

@Composable
private fun SpiralAbyssContent(data: SpiralAbyssResponse) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Summary card
        AbyssSummaryCard(data)

        // Floor breakdown
        val floors = data.floors.filter { it.isUnlock }
        if (floors.isEmpty()) {
            Card(modifier = Modifier.fillMaxWidth(), pressFeedbackType = PressFeedbackType.Sink) {
                Text(
                    text = "暂无深境螺旋数据",
                    modifier = Modifier.padding(16.dp),
                    style = MiuixTheme.textStyles.body2,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
        } else {
            floors.forEach { floor ->
                AbyssFloorCard(floor)
            }
        }
    }
}

@Composable
private fun AbyssSummaryCard(data: SpiralAbyssResponse) {
    Card(modifier = Modifier.fillMaxWidth(), pressFeedbackType = PressFeedbackType.Sink) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🌙 深境螺旋",
                style = MiuixTheme.textStyles.headline2,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                SummaryItem("最深记录", data.maxFloor)
                SummaryItem("战斗次数", "${data.totalBattleTimes}")
                SummaryItem("胜利次数", "${data.totalWinTimes}")
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Top damage dealers
            if (data.damageRank.isNotEmpty()) {
                Text(
                    text = "最高伤害",
                    style = MiuixTheme.textStyles.title3,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = data.damageRank.firstOrNull()?.let {
                        "角色 #${it.avatarId} - ${formatDamage(it.value)}"
                    } ?: "暂无数据",
                    style = MiuixTheme.textStyles.body2,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
        }
    }
}

@Composable
private fun AbyssFloorCard(floor: AbyssFloor) {
    Card(modifier = Modifier.fillMaxWidth(), pressFeedbackType = PressFeedbackType.Sink) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Floor header with star count
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "第${floor.index}层",
                    style = MiuixTheme.textStyles.title2,
                    fontWeight = FontWeight.Bold,
                )
                StarDisplay(current = floor.star, max = floor.maxStar)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Chambers
            floor.levels.forEach { level ->
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "  第${level.index}间",
                            style = MiuixTheme.textStyles.body2,
                            fontWeight = FontWeight.Medium,
                        )
                        StarDisplay(current = level.star, max = level.maxStar)
                    }

                    // Battle halves
                    level.battles.forEach { battle ->
                        BattleRow(battle)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun BattleRow(battle: AbyssBattle) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, top = 2.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "上半" + if (battle.index == 1) "" else "(下)",
            style = MiuixTheme.textStyles.main,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            modifier = Modifier.width(48.dp),
        )
        // Avatar icons represented as text
        battle.avatars.forEach { avatar ->
            CharacterChip(avatar.id, avatar.level, avatar.rarity)
        }
    }
}

@Composable
private fun CharacterChip(id: Int, level: Int, rarity: Int) {
    val starText = "★".repeat(rarity.coerceIn(1, 5))
    Card(modifier = Modifier.padding(end = 4.dp), pressFeedbackType = PressFeedbackType.Sink) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "#$id",
                style = MiuixTheme.textStyles.main,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Lv.$level",
                style = MiuixTheme.textStyles.main,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
            Text(
                text = starText,
                style = MiuixTheme.textStyles.main,
                color = MiuixTheme.colorScheme.primary,
            )
        }
    }
}

// ========== HSR Challenge (Memory of Chaos / Pure Fiction) ==========

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HsrChallengeContent(
    data: ChallengeResponse,
    selectedType: HsrChallengeType,
    onTypeSelect: (HsrChallengeType) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Challenge type switcher
        Card(modifier = Modifier.fillMaxWidth(), pressFeedbackType = PressFeedbackType.Sink) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "挑战类型",
                    style = MiuixTheme.textStyles.title3,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    HsrChallengeType.entries.forEach { type ->
                        val isSelected = type == selectedType
                        Button(onClick = { onTypeSelect(type) }) {
                            Text(
                                text = type.displayName,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            )
                        }
                    }
                }
            }
        }

        // Summary
        HsrChallengeSummaryCard(data, selectedType)

        // Groups/floors
        if (data.groups.isEmpty() && !data.hasData) {
            Card(modifier = Modifier.fillMaxWidth(), pressFeedbackType = PressFeedbackType.Sink) {
                Text(
                    text = "暂无${selectedType.displayName}数据",
                    modifier = Modifier.padding(16.dp),
                    style = MiuixTheme.textStyles.body2,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
        } else {
            data.groups.forEach { group ->
                if (group.floors.isNotEmpty()) {
                    HsrChallengeGroupCard(group)
                }
            }
        }
    }
}

@Composable
private fun HsrChallengeSummaryCard(
    data: ChallengeResponse,
    selectedType: HsrChallengeType,
) {
    val icon = when (selectedType) {
        HsrChallengeType.MEMORY_OF_CHAOS -> "🌀"
        HsrChallengeType.PURE_FICTION -> "📖"
    }

    Card(modifier = Modifier.fillMaxWidth(), pressFeedbackType = PressFeedbackType.Sink) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "$icon ${selectedType.displayName}",
                style = MiuixTheme.textStyles.headline2,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (data.groups.isNotEmpty()) {
                val latest = data.groups.first()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    SummaryItem("战斗次数", "${data.totalBattleNum}")
                    SummaryItem("总星数", "${latest.star}/${latest.maxStar}")
                }
            } else {
                Text(
                    text = "战斗次数: ${data.totalBattleNum}",
                    style = MiuixTheme.textStyles.body2,
                )
            }
        }
    }
}

@Composable
private fun HsrChallengeGroupCard(group: com.chronie.helperunited.model.ChallengeGroup) {
    Card(modifier = Modifier.fillMaxWidth(), pressFeedbackType = PressFeedbackType.Sink) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "第${group.scheduleId}期",
                    style = MiuixTheme.textStyles.title2,
                    fontWeight = FontWeight.Bold,
                )
                StarDisplay(current = group.star, max = group.maxStar)
            }

            Spacer(modifier = Modifier.height(12.dp))

            group.floors.forEach { floor ->
                HsrFloorRow(floor)
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun HsrFloorRow(floor: ChallengeFloor) {
    Column(modifier = Modifier.padding(vertical = 2.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = floor.name,
                style = MiuixTheme.textStyles.body2,
                fontWeight = FontWeight.Medium,
            )
            StarDisplay(current = floor.star, max = floor.maxStar)
        }

        // Node 1 & Node 2 lineups
        floor.node1?.let { node ->
            ChallengeNodeRow("上半", node)
        }
        floor.node2?.let { node ->
            ChallengeNodeRow("下半", node)
        }
    }
}

@Composable
private fun ChallengeNodeRow(label: String, node: ChallengeNode) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 2.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MiuixTheme.textStyles.main,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            modifier = Modifier.width(36.dp),
        )
        node.avatars.forEach { avatar ->
            HsrCharacterChip(avatar)
        }
    }
}

@Composable
private fun HsrCharacterChip(avatar: ChallengeAvatar) {
    val starText = "★".repeat(avatar.rarity.coerceIn(1, 5))
    Card(modifier = Modifier.padding(end = 4.dp), pressFeedbackType = PressFeedbackType.Sink) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = avatar.name ?: "#${avatar.id}",
                style = MiuixTheme.textStyles.main,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Lv.${avatar.level} E${avatar.rank}",
                style = MiuixTheme.textStyles.main,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
            Text(
                text = starText,
                style = MiuixTheme.textStyles.main,
                color = MiuixTheme.colorScheme.primary,
            )
        }
    }
}

// ========== Shared Components ==========

@Composable
private fun SummaryItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MiuixTheme.textStyles.title2,
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
private fun StarDisplay(current: Int, max: Int) {
    Row {
        repeat(max) { index ->
            Text(
                text = if (index < current) "⭐" else "☆",
                style = MiuixTheme.textStyles.main,
            )
        }
    }
}

private fun formatDamage(value: Int): String {
    return when {
        value >= 1_000_000 -> String.format("%.1fM", value / 1_000_000.0)
        value >= 1_000 -> String.format("%.1fK", value / 1_000.0)
        else -> "$value"
    }
}
