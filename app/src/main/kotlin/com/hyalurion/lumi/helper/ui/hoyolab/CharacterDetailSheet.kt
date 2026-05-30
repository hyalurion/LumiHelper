package com.hyalurion.lumi.helper.ui.hoyolab

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.hyalurion.lumi.helper.model.HoYoLabCharacterDisplay
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.overlay.OverlayBottomSheet
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType

@Composable
fun CharacterDetailSheet(
    show: Boolean,
    character: HoYoLabCharacterDisplay,
    onDismiss: () -> Unit,
) {
    OverlayBottomSheet(
        show = show,
        title = character.name,
        onDismissRequest = onDismiss,
    ) {
        LazyColumn(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                CharacterHeader(character)
            }

            item {
                CharacterStatsCard(character)
            }

            if (character.weaponName != null) {
                item {
                    WeaponCard(character)
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun CharacterHeader(character: HoYoLabCharacterDisplay) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        pressFeedbackType = PressFeedbackType.Sink,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AsyncImage(
                model = character.iconUrl,
                contentDescription = character.name,
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MiuixTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.Crop,
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = character.name,
                    style = MiuixTheme.textStyles.headline2,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${elementToEmoji(character.element)} ${character.element}",
                    style = MiuixTheme.textStyles.body1,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    StatChip("⭐ ${character.rarity}")
                    StatChip("Lv.${character.level}")
                    if (character.constellation > 0) {
                        StatChip("命${character.constellation}")
                    }
                }
            }
        }
    }
}

@Composable
private fun CharacterStatsCard(character: HoYoLabCharacterDisplay) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        pressFeedbackType = PressFeedbackType.Sink,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "角色信息",
                style = MiuixTheme.textStyles.title2,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(12.dp))

            DetailRow("等级", "Lv.${character.level}")
            DetailDivider()
            DetailRow("稀有度", "${"★".repeat(character.rarity)} (${character.rarity}★)")
            DetailDivider()
            DetailRow("元素", "${elementToEmoji(character.element)} ${character.element}")
            DetailDivider()
            DetailRow("命之座", if (character.constellation > 0) "命${character.constellation}" else "无")
            if (character.friendship > 0) {
                DetailDivider()
                DetailRow("好感度", "Lv.${character.friendship}")
            }
        }
    }
}

@Composable
private fun WeaponCard(character: HoYoLabCharacterDisplay) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        pressFeedbackType = PressFeedbackType.Sink,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = when (character.gameId) {
                    "HSR" -> "光锥"
                    "ZZZ" -> "音擎"
                    else -> "武器"
                },
                style = MiuixTheme.textStyles.title2,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(12.dp))

            DetailRow("名称", character.weaponName ?: "未知")
            character.weaponLevel?.let {
                DetailDivider()
                DetailRow("等级", "Lv.$it")
            }
            character.weaponRarity?.let {
                DetailDivider()
                DetailRow("稀有度", "${"★".repeat(it)} (${it}★)")
            }
            character.weaponRefinement?.let {
                DetailDivider()
                DetailRow(
                    when (character.gameId) {
                        "HSR" -> "叠影"
                        else -> "精炼"
                    },
                    "Lv.$it",
                )
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MiuixTheme.textStyles.body1,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        )
        Text(
            text = value,
            style = MiuixTheme.textStyles.body1,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun DetailDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(MiuixTheme.colorScheme.surfaceVariant),
    )
}

@Composable
private fun StatChip(text: String) {
    Card(
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
        pressFeedbackType = PressFeedbackType.Sink,
    ) {
        Text(
            text = text,
            style = MiuixTheme.textStyles.main,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
        )
    }
}

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