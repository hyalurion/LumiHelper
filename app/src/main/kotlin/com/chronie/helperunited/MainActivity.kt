package com.chronie.helperunited

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.AndroidEntryPoint
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.MiuixCard
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.theme.MiuixTheme
import com.chronie.helperunited.ui.enka.EnkaShowcaseScreen
import com.chronie.helperunited.ui.enka.EnkaShowcaseViewModel
import com.chronie.helperunited.ui.gacha.GachaScreen
import com.chronie.helperunited.ui.gacha.GachaViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MiuixTheme {
                HelperUnitedApp()
            }
        }
    }
}

@Composable
fun HelperUnitedApp() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (currentScreen) {
                            is Screen.Home -> "🍕 Helper United"
                            is Screen.EnkaShowcase -> "角色展柜"
                            is Screen.Gacha -> "抽卡记录"
                        },
                        fontWeight = FontWeight.Bold,
                    )
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            when (val screen = currentScreen) {
                is Screen.Home -> {
                    HomeScreen(
                        onEnkaShowcaseClick = { currentScreen = Screen.EnkaShowcase },
                        onGachaClick = { currentScreen = Screen.Gacha },
                    )
                }
                is Screen.EnkaShowcase -> {
                    val viewModel: EnkaShowcaseViewModel = hiltViewModel()
                    EnkaShowcaseScreen(
                        profile = viewModel.uiState.value.profile,
                        isLoading = viewModel.uiState.value.isLoading,
                        error = viewModel.uiState.value.error,
                        onQuery = { viewModel.queryShowcase(it) },
                    )
                }
                is Screen.Gacha -> {
                    val viewModel: GachaViewModel = hiltViewModel()
                    val uiState by viewModel.uiState.collectAsState()
                    GachaScreen(
                        profiles = uiState.profiles,
                        entries = uiState.entries,
                        isLoading = uiState.isLoading,
                        error = uiState.error,
                        selectedProfile = uiState.selectedProfile,
                        onProfileSelect = { viewModel.selectProfile(it) },
                        onFetch = { uid, game -> viewModel.fetchGachaRecords(uid, game) },
                        onImport = { viewModel.importUIGF(it) },
                        onExport = { _, _ -> },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HomeScreen(
    onEnkaShowcaseClick: () -> Unit,
    onGachaClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Welcome Card
        MiuixCard(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
            ) {
                Text(
                    text = "🍕 Pizza Helper United",
                    style = top.yukonga.miuix.kmp.theme.MiuixTheme.textStyles.headlineLarge,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Kotlin/Android Edition · miuix v0.9.0",
                    style = top.yukonga.miuix.kmp.theme.MiuixTheme.textStyles.bodyMedium,
                    color = top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "支持原神、星穹铁道、绝区零",
                    style = top.yukonga.miuix.kmp.theme.MiuixTheme.textStyles.bodySmall,
                    color = top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
        }

        // Features Section
        Text(
            text = "功能",
            style = top.yukonga.miuix.kmp.theme.MiuixTheme.textStyles.titleLarge,
            fontWeight = FontWeight.Bold,
        )

        // Feature Cards
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            FeatureCard(
                title = "角色展柜",
                subtitle = "Enka.Network",
                description = "查看角色详情、武器、圣遗物",
                icon = "🎭",
                onClick = onEnkaShowcaseClick,
                modifier = Modifier.weight(1f),
            )
            FeatureCard(
                title = "抽卡记录",
                subtitle = "GachaKit",
                description = "记录分析、UIGF 导入导出",
                icon = "🎰",
                onClick = onGachaClick,
                modifier = Modifier.weight(1f),
            )
        }

        // Supported Games
        Text(
            text = "支持的游戏",
            style = top.yukonga.miuix.kmp.theme.MiuixTheme.textStyles.titleLarge,
            fontWeight = FontWeight.Bold,
        )

        MiuixCard(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                GameItem(
                    name = "原神",
                    subtitle = "Genshin Impact",
                    emoji = "⚡",
                )
                GameItem(
                    name = "星穹铁道",
                    subtitle = "Honkai: Star Rail",
                    emoji = "🚂",
                )
                GameItem(
                    name = "绝区零",
                    subtitle = "Zenless Zone Zero",
                    emoji = "🎮",
                )
            }
        }

        // Version Info
        MiuixCard(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
            ) {
                Text(
                    text = "关于",
                    style = top.yukonga.miuix.kmp.theme.MiuixTheme.textStyles.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "版本 1.0.0",
                    style = top.yukonga.miuix.kmp.theme.MiuixTheme.textStyles.bodyMedium,
                )
                Text(
                    text = "从 PizzaHelperUnited (Swift/iOS) 移植",
                    style = top.yukonga.miuix.kmp.theme.MiuixTheme.textStyles.bodySmall,
                    color = top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
        }
    }
}

@Composable
private fun FeatureCard(
    title: String,
    subtitle: String,
    description: String,
    icon: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    MiuixCard(
        modifier = modifier,
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = icon,
                style = top.yukonga.miuix.kmp.theme.MiuixTheme.textStyles.headlineMedium,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = top.yukonga.miuix.kmp.theme.MiuixTheme.textStyles.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = subtitle,
                style = top.yukonga.miuix.kmp.theme.MiuixTheme.textStyles.bodySmall,
                color = top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = top.yukonga.miuix.kmp.theme.MiuixTheme.textStyles.bodySmall,
                color = top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
        }
    }
}

@Composable
private fun GameItem(
    name: String,
    subtitle: String,
    emoji: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = emoji,
            style = top.yukonga.miuix.kmp.theme.MiuixTheme.textStyles.headlineSmall,
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = name,
                style = top.yukonga.miuix.kmp.theme.MiuixTheme.textStyles.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = subtitle,
                style = top.yukonga.miuix.kmp.theme.MiuixTheme.textStyles.bodySmall,
                color = top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
        }
    }
}

sealed class Screen {
    data object Home : Screen()
    data object EnkaShowcase : Screen()
    data object Gacha : Screen()
}
