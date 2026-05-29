package com.chronie.helperunited.ui.enka

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.chronie.helperunited.model.EnkaAvatarSummarized
import com.chronie.helperunited.model.EnkaProfileSummarized
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.utils.PressFeedbackType
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * Enka character showcase screen using MIUIX components.
 * Ported from PizzaHelperUnited's EnkaKit frontend.
 */
@Composable
fun EnkaShowcaseScreen(
    profile: EnkaProfileSummarized?,
    isLoading: Boolean,
    error: String?,
    onQuery: (String) -> Unit,
) {
    var uidInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        // UID Input Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            pressFeedbackType = PressFeedbackType.Sink,
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
            ) {
                Text(
                    text = "查询角色展柜",
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
                        onValueChange = { uidInput = it },
                        label = "输入游戏 UID",
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { if (uidInput.isNotBlank()) onQuery(uidInput) },
                        enabled = !isLoading && uidInput.isNotBlank(),
                    ) {
                        Text("查询")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

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
                    pressFeedbackType = PressFeedbackType.Sink,
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                    ) {
                        Text(
                            text = "错误",
                            style = MiuixTheme.textStyles.title2,
                            fontWeight = FontWeight.Bold,
                            color = MiuixTheme.colorScheme.error,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = error,
                            style = MiuixTheme.textStyles.body2,
                            color = MiuixTheme.colorScheme.onErrorContainer,
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
                        ProfileHeader(profile)
                    }

                    // Avatar List
                    items(profile.avatarList) { avatar ->
                        AvatarCard(avatar)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileHeader(profile: EnkaProfileSummarized) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        pressFeedbackType = PressFeedbackType.Sink,
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Profile Picture
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
                        style = MiuixTheme.textStyles.headline3,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Lv.${profile.level} · 世界等级 ${profile.worldLevel}",
                        style = MiuixTheme.textStyles.body2,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    )
                    if (profile.finishAchievementNum > 0) {
                        Text(
                            text = "成就 ${profile.finishAchievementNum}",
                            style = MiuixTheme.textStyles.main,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        )
                    }
                }
            }

            // Abyss Progress
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
                            text = "🌙",
                            style = MiuixTheme.textStyles.title1,
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "深境螺旋",
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AvatarCard(avatar: EnkaAvatarSummarized) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        pressFeedbackType = PressFeedbackType.Sink,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            // Avatar Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Avatar Icon
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
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        StatChip("Lv.${avatar.level}")
                        StatChip("命座 ${avatar.constellation}")
                        StatChip("好感 ${avatar.friendship}")
                    }
                }
            }

            // Weapon
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
                                text = "Lv.${weapon.level} · 精炼 ${weapon.refinement}",
                                style = MiuixTheme.textStyles.main,
                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            )
                        }
                    }
                }
            }

            // Artifacts
            if (avatar.artifacts.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "圣遗物",
                    style = MiuixTheme.textStyles.title3,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    avatar.artifacts.forEach { artifact ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.width(56.dp),
                        ) {
                            AsyncImage(
                                model = artifact.icon,
                                contentDescription = artifact.name,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MiuixTheme.colorScheme.surfaceVariant),
                                contentScale = ContentScale.Crop,
                            )
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

            // Talent Levels
            if (avatar.talentLevels.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "天赋等级",
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
                                text = "天赋${talentId}: $level",
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
