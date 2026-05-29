package com.chronie.helperunited.ui.hoyolab

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.chronie.helperunited.data.SortOption
import com.chronie.helperunited.model.HoYoAccount
import com.chronie.helperunited.model.HoYoLabCharacterDisplay
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.utils.PressFeedbackType
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * HoYoLAB character list screen.
 * Shows all characters fetched from the HoYoLAB game_record API.
 */
@Composable
fun HoYoLabCharacterScreen(
    viewModel: HoYoLabCharacterViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        // Account selector
        if (uiState.accounts.isNotEmpty()) {
            HoYoLabAccountSelector(
                accounts = uiState.accounts,
                selectedAccount = uiState.selectedAccount,
                onSelect = { viewModel.selectAccount(it) },
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Sort selector
        SortSelector(
            currentSort = uiState.sortOption,
            onSortChange = { viewModel.setSortOption(it) },
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Filter chips
        if (uiState.availableElements.isNotEmpty() || uiState.availableRarities.isNotEmpty()) {
            FilterChips(
                elements = uiState.availableElements,
                rarities = uiState.availableRarities,
                selectedElement = uiState.selectedElement,
                selectedRarity = uiState.selectedRarity,
                onElementClick = { viewModel.filterByElement(it) },
                onRarityClick = { viewModel.filterByRarity(it) },
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Refresh button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "角色列表 (${uiState.filteredCharacters.size})",
                style = MiuixTheme.textStyles.headline3,
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

        Spacer(modifier = Modifier.height(12.dp))

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
                        Text(
                            text = uiState.error ?: "",
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
            uiState.filteredCharacters.isEmpty() && uiState.allCharacters.isNotEmpty() -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    pressFeedbackType = PressFeedbackType.Sink,
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "没有匹配的角色",
                            style = MiuixTheme.textStyles.title2,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "尝试调整筛选条件",
                            style = MiuixTheme.textStyles.body2,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        )
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(uiState.filteredCharacters, key = { it.id }) { character ->
                        HoYoLabCharacterCard(character)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HoYoLabAccountSelector(
    accounts: List<HoYoAccount>,
    selectedAccount: HoYoAccount?,
    onSelect: (HoYoAccount) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        pressFeedbackType = PressFeedbackType.Sink,
    ) {
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SortSelector(
    currentSort: SortOption,
    onSortChange: (SortOption) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        pressFeedbackType = PressFeedbackType.Sink,
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "排序",
                style = MiuixTheme.textStyles.title3,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                val sortOptions = listOf(
                    SortOption.LEVEL to "等级",
                    SortOption.CONSTELLATION to "命座",
                    SortOption.FRIENDSHIP to "好感",
                    SortOption.RARITY to "稀有度",
                )
                sortOptions.forEach { (option, label) ->
                    val isSelected = currentSort == option
                    Button(onClick = { onSortChange(option) }) {
                        Text(
                            text = label,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FilterChips(
    elements: List<String>,
    rarities: List<Int>,
    selectedElement: String?,
    selectedRarity: Int?,
    onElementClick: (String?) -> Unit,
    onRarityClick: (Int?) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        pressFeedbackType = PressFeedbackType.Sink,
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Element filters
            if (elements.isNotEmpty()) {
                Text(
                    text = "元素",
                    style = MiuixTheme.textStyles.title3,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    elements.forEach { element ->
                        val isSelected = selectedElement == element
                        Button(onClick = { onElementClick(element) }) {
                            Text(
                                text = elementToEmoji(element) + element,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            )
                        }
                    }
                }
            }

            // Rarity filters
            if (rarities.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "稀有度",
                    style = MiuixTheme.textStyles.title3,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    rarities.forEach { rarity ->
                        val isSelected = selectedRarity == rarity
                        Button(onClick = { onRarityClick(rarity) }) {
                            Text(
                                text = "⭐${rarity}",
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HoYoLabCharacterCard(character: HoYoLabCharacterDisplay) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        pressFeedbackType = PressFeedbackType.Sink,
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Character icon
            AsyncImage(
                model = character.iconUrl,
                contentDescription = character.name,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MiuixTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.Crop,
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Character info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = character.name,
                    style = MiuixTheme.textStyles.title2,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    StatChip("${elementToEmoji(character.element)}Lv.${character.level}")
                    StatChip("⭐${character.rarity}")
                    if (character.constellation > 0) {
                        StatChip("命${character.constellation}")
                    }
                    if (character.friendship > 0) {
                        StatChip("❤${character.friendship}")
                    }
                }
                // Weapon info
                character.weaponName?.let { weapon ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = buildString {
                            append(weapon)
                            character.weaponLevel?.let { append(" Lv.$it") }
                            character.weaponRefinement?.let { append(" 精$it") }
                        },
                        style = MiuixTheme.textStyles.main,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun StatChip(text: String) {
    Card(
        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
        pressFeedbackType = PressFeedbackType.Sink,
    ) {
        Text(
            text = text,
            style = MiuixTheme.textStyles.main,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
        )
    }
}

/**
 * Map element names to emoji for display.
 */
private fun elementToEmoji(element: String): String = when (element.lowercase()) {
    "pyro", "火" -> "🔥"
    "hydro", "水" -> "💧"
    "anemo", "风" -> "🍃"
    "electro", "雷" -> "⚡"
    "dendro", "草" -> "🌿"
    "cryo", "冰" -> "❄️"
    "geo", "岩" -> "🪨"
    "physical" -> "⚔️"
    "fire" -> "🔥"
    "ice" -> "❄️"
    "wind" -> "🍃"
    "thunder" -> "⚡"
    "quantum" -> "🔮"
    "imaginary" -> "✨"
    else -> "🌀"
}
