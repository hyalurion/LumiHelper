package com.hyalurion.lumi.helper.ui.enka

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.hyalurion.lumi.helper.base.SupportedGame
import com.hyalurion.lumi.helper.model.ChallengeResponse
import com.hyalurion.lumi.helper.model.EnkaAvatarSummarized
import com.hyalurion.lumi.helper.model.EnkaProfileSummarized
import com.hyalurion.lumi.helper.model.HoYoAccount
import com.hyalurion.lumi.helper.model.LedgerData4GI
import com.hyalurion.lumi.helper.model.LedgerData4HSR
import com.hyalurion.lumi.helper.model.SpiralAbyssResponse
import com.hyalurion.lumi.helper.ui.enka.rating.ArtifactRatingEngine
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType

private val SECTION_SPACING = 16.dp

@Composable
fun EnkaShowcaseScreen(
    onNavigateToCharacters: () -> Unit = {},
    onNavigateToChallenge: () -> Unit = {},
    onNavigateToLedger: () -> Unit = {},
    viewModel: EnkaShowcaseViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var uidInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(SECTION_SPACING),
    ) {
        AccountSelectorSection(
            accounts = uiState.accounts,
            selectedAccount = uiState.selectedAccount,
            onSelect = { viewModel.selectAccount(it) },
        )

        UidQuerySection(
            uidInput = uidInput,
            onUidChange = { uidInput = it },
            onQuery = {
                if (uidInput.isNotBlank()) {
                    viewModel.queryShowcase(uidInput)
                }
            },
            isLoading = uiState.isLoading,
        )

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null -> {
                ErrorCard(uiState.error!!)
            }
            uiState.profile != null -> {
                ProfileHeader(uiState.profile!!)

                uiState.profile!!.avatarList.forEach { avatar ->
                    AvatarCard(avatar)
                }

                BattleReportSection(
                    status = uiState.battleReportStatus,
                    game = uiState.selectedAccount?.game,
                    onNavigateToChallenge = onNavigateToChallenge,
                )

                LedgerSection(
                    status = uiState.ledgerStatus,
                    game = uiState.selectedAccount?.game,
                    onNavigateToLedger = onNavigateToLedger,
                )

                CharacterInventoryNav(
                    onNavigate = onNavigateToCharacters,
                )
            }
            uiState.accounts.isNotEmpty() && uiState.profile == null && !uiState.isLoading -> {
                EmptyHint()
            }
        }
    }
}

// ========== Account Selector ==========

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AccountSelectorSection(
    accounts: List<HoYoAccount>,
    selectedAccount: HoYoAccount?,
    onSelect: (HoYoAccount) -> Unit,
) {
    if (accounts.isEmpty()) return

    Card(
        modifier = Modifier.fillMaxWidth(),
        pressFeedbackType = PressFeedbackType.Sink,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Account",
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

// ========== UID Query ==========

@Composable
private fun UidQuerySection(
    uidInput: String,
    onUidChange: (String) -> Unit,
    onQuery: () -> Unit,
    isLoading: Boolean,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        pressFeedbackType = PressFeedbackType.Sink,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Query Character Showcase",
                style = MiuixTheme.textStyles.title2,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextField(
                    value = uidInput,
                    onValueChange = onUidChange,
                    label = "Enter game UID",
                    modifier = Modifier.weight(1f),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onQuery,
                    enabled = !isLoading && uidInput.isNotBlank(),
                ) {
                    Text("Query")
                }
            }
        }
    }
}

// ========== Error ==========

@Composable
private fun ErrorCard(error: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        pressFeedbackType = PressFeedbackType.Sink,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Error",
                style = MiuixTheme.textStyles.title2,
                fontWeight = FontWeight.Bold,
                color = MiuixTheme.colorScheme.error,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = error,
                style = MiuixTheme.textStyles.body2,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
        }
    }
}

// ========== Empty Hint ==========

@Composable
private fun EmptyHint() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        pressFeedbackType = PressFeedbackType.Sink,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "No showcase data",
                style = MiuixTheme.textStyles.title2,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Select an account above or enter a UID to query the character showcase.",
                style = MiuixTheme.textStyles.main,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
        }
    }
}

// ========== Profile Header ==========

@Composable
private fun ProfileHeader(profile: EnkaProfileSummarized) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        pressFeedbackType = PressFeedbackType.Sink,
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = profile.headIconUrl,
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(MiuixTheme.colorScheme.surfaceVariant),
                    contentScale = ContentScale.Crop,
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = profile.nickname,
                        style = MiuixTheme.textStyles.headline2,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Lv.${profile.level} · WL${profile.worldLevel}",
                        style = MiuixTheme.textStyles.body2,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    )
                    if (profile.finishAchievementNum > 0) {
                        Text(
                            text = "Achievements: ${profile.finishAchievementNum}",
                            style = MiuixTheme.textStyles.main,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        )
                    }
                }
            }

            if (profile.towerFloorIndex != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    pressFeedbackType = PressFeedbackType.Sink,
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "\uD83C\uDF19",
                            style = MiuixTheme.textStyles.title1,
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Spiral Abyss",
                                style = MiuixTheme.textStyles.title3,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = "${profile.towerFloorIndex}-${profile.towerLevelIndex ?: 0}",
                                style = MiuixTheme.textStyles.body1,
                                color = MiuixTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }
        }
    }
}

// ========== Avatar Card ==========

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AvatarCard(avatar: EnkaAvatarSummarized) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        pressFeedbackType = PressFeedbackType.Sink,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = avatar.iconUrl,
                    contentDescription = avatar.name,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MiuixTheme.colorScheme.surfaceVariant),
                    contentScale = ContentScale.Crop,
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = avatar.name,
                        style = MiuixTheme.textStyles.title1,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatChip("Lv.${avatar.level}")
                        StatChip("C${avatar.constellation}")
                        StatChip("F${avatar.friendship}")
                    }
                }
            }

            avatar.weapon?.let { weapon ->
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    pressFeedbackType = PressFeedbackType.Sink,
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        AsyncImage(
                            model = weapon.icon,
                            contentDescription = weapon.name,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop,
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = weapon.name,
                                style = MiuixTheme.textStyles.title3,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = "Lv.${weapon.level} · R${weapon.refinement}",
                                style = MiuixTheme.textStyles.main,
                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            )
                        }
                    }
                }
            }

            if (avatar.artifacts.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Artifacts",
                    style = MiuixTheme.textStyles.title3,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    avatar.artifacts.forEach { artifact ->
                        val rating = remember(artifact.id, artifact.level) {
                            ArtifactRatingEngine.calculateRating(
                                mainStat = artifact.mainStat,
                                subStats = artifact.subStats,
                                level = artifact.level,
                                rarity = artifact.rarity,
                            )
                        }
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.width(56.dp),
                        ) {
                            Box {
                                AsyncImage(
                                    model = artifact.icon,
                                    contentDescription = artifact.name,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MiuixTheme.colorScheme.surfaceVariant),
                                    contentScale = ContentScale.Crop,
                                )
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .background(
                                            color = Color(rating.gradeColor).copy(alpha = 0.9f),
                                            shape = RoundedCornerShape(4.dp),
                                        )
                                        .padding(horizontal = 3.dp, vertical = 1.dp),
                                ) {
                                    Text(
                                        text = rating.grade,
                                        style = MiuixTheme.textStyles.main,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        textAlign = TextAlign.Center,
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "+${artifact.level}",
                                style = MiuixTheme.textStyles.main,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }

            if (avatar.talentLevels.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Talent Levels",
                    style = MiuixTheme.textStyles.title3,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    avatar.talentLevels.entries.forEach { (talentId, level) ->
                        Card(
                            modifier = Modifier.width(56.dp),
                            pressFeedbackType = PressFeedbackType.Sink,
                        ) {
                            Text(
                                text = "T$talentId: $level",
                                style = MiuixTheme.textStyles.main,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

// ========== Battle Report Section ==========

@Composable
private fun BattleReportSection(
    status: AsyncStatus<*>,
    game: SupportedGame?,
    onNavigateToChallenge: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        pressFeedbackType = PressFeedbackType.Sink,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            val title = when (game) {
                SupportedGame.GENSHIN_IMPACT -> "Spiral Abyss"
                SupportedGame.STAR_RAIL -> "Memory of Chaos"
                else -> "Battle Report"
            }

            Text(
                text = title,
                style = MiuixTheme.textStyles.title2,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))

            when (status) {
                is AsyncStatus.Standby -> {
                    Text(
                        text = "No data. Tap to view full challenge records.",
                        style = MiuixTheme.textStyles.main,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    )
                }
                is AsyncStatus.Loading -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                }
                is AsyncStatus.Error -> {
                    Text(
                        text = status.message,
                        style = MiuixTheme.textStyles.main,
                        color = MiuixTheme.colorScheme.error,
                    )
                }
                is AsyncStatus.Success -> {
                    val data = status.data
                    when {
                        data is SpiralAbyssResponse -> {
                            val maxFloor = data.maxFloor
                            val totalStars = data.floors.sumOf { it.star }
                            val maxStars = data.floors.sumOf { it.maxStar }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "\uD83C\uDF19",
                                    style = MiuixTheme.textStyles.title1,
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Floor: $maxFloor",
                                        style = MiuixTheme.textStyles.body1,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    Text(
                                        text = "Stars: $totalStars/$maxStars",
                                        style = MiuixTheme.textStyles.main,
                                        color = MiuixTheme.colorScheme.primary,
                                    )
                                }
                                Button(onClick = onNavigateToChallenge) {
                                    Text("Details")
                                }
                            }
                        }
                        data is ChallengeResponse -> {
                            val totalStars = data.groups.sumOf { it.star }
                            val maxStars = data.groups.sumOf { it.maxStar }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "\uD83C\uDF19",
                                    style = MiuixTheme.textStyles.title1,
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (data.hasData) "Chaos Memory" else "No data",
                                        style = MiuixTheme.textStyles.body1,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    if (data.hasData) {
                                        Text(
                                            text = "Stars: $totalStars/$maxStars",
                                            style = MiuixTheme.textStyles.main,
                                            color = MiuixTheme.colorScheme.primary,
                                        )
                                    }
                                }
                                if (data.hasData) {
                                    Button(onClick = onNavigateToChallenge) {
                                        Text("Details")
                                    }
                                }
                            }
                        }
                        else -> {
                            Text(
                                text = "Data loaded",
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

// ========== Ledger Section ==========

@Composable
private fun LedgerSection(
    status: AsyncStatus<*>,
    game: SupportedGame?,
    onNavigateToLedger: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        pressFeedbackType = PressFeedbackType.Sink,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            val title = when (game) {
                SupportedGame.GENSHIN_IMPACT -> "Traveler's Diary"
                SupportedGame.STAR_RAIL -> "Trailblaze Monthly Calendar"
                else -> "Ledger"
            }

            Text(
                text = title,
                style = MiuixTheme.textStyles.title2,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))

            when (status) {
                is AsyncStatus.Standby -> {
                    Text(
                        text = "No data. Tap to view full ledger records.",
                        style = MiuixTheme.textStyles.main,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    )
                }
                is AsyncStatus.Loading -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                }
                is AsyncStatus.Error -> {
                    Text(
                        text = status.message,
                        style = MiuixTheme.textStyles.main,
                        color = MiuixTheme.colorScheme.error,
                    )
                }
                is AsyncStatus.Success -> {
                    val data = status.data
                    when {
                        data is LedgerData4GI -> {
                            val primogems = data.monthData.currentPrimogems
                            val mora = data.monthData.currentMora
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "\uD83D\uDCB0",
                                    style = MiuixTheme.textStyles.title1,
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Month: ${data.dataMonth}",
                                        style = MiuixTheme.textStyles.body1,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    Text(
                                        text = "Primogems: $primogems",
                                        style = MiuixTheme.textStyles.main,
                                        color = MiuixTheme.colorScheme.primary,
                                    )
                                    Text(
                                        text = "Mora: $mora",
                                        style = MiuixTheme.textStyles.main,
                                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                    )
                                }
                                Button(onClick = onNavigateToLedger) {
                                    Text("Details")
                                }
                            }
                        }
                        data is LedgerData4HSR -> {
                            val stellarJades = data.monthData.currentStellarJades
                            val passes = data.monthData.currentPasses
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "\uD83D\uDCB0",
                                    style = MiuixTheme.textStyles.title1,
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Month: ${data.dataMonth}",
                                        style = MiuixTheme.textStyles.body1,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    Text(
                                        text = "Stellar Jade: $stellarJades",
                                        style = MiuixTheme.textStyles.main,
                                        color = MiuixTheme.colorScheme.primary,
                                    )
                                    Text(
                                        text = "Passes: $passes",
                                        style = MiuixTheme.textStyles.main,
                                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                    )
                                }
                                Button(onClick = onNavigateToLedger) {
                                    Text("Details")
                                }
                            }
                        }
                        else -> {
                            Text(
                                text = "Data loaded",
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

// ========== Character Inventory Nav ==========

@Composable
private fun CharacterInventoryNav(
    onNavigate: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onNavigate() },
        pressFeedbackType = PressFeedbackType.Sink,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "\uD83D\uDC65",
                style = MiuixTheme.textStyles.headline2,
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Character Inventory",
                    style = MiuixTheme.textStyles.title2,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "View all characters on this account",
                    style = MiuixTheme.textStyles.body2,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
            Text(
                text = "\u203A",
                style = MiuixTheme.textStyles.headline2,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
        }
    }
}

// ========== Shared Components ==========

@Composable
private fun StatChip(text: String) {
    Card(
        modifier = Modifier.width(56.dp),
        pressFeedbackType = PressFeedbackType.Sink,
    ) {
        Text(
            text = text,
            style = MiuixTheme.textStyles.main,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}