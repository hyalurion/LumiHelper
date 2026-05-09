package com.chronie.helperunited

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.AndroidEntryPoint
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.MiuixCard
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.theme.MiuixTheme
import com.chronie.helperunited.ui.daily.DailyNoteScreen
import com.chronie.helperunited.ui.daily.DailyNoteViewModel
import com.chronie.helperunited.ui.enka.EnkaShowcaseScreen
import com.chronie.helperunited.ui.enka.EnkaShowcaseViewModel
import com.chronie.helperunited.ui.gacha.GachaScreen
import com.chronie.helperunited.ui.gacha.GachaViewModel
import com.chronie.helperunited.ui.account.AccountScreen
import com.chronie.helperunited.ui.account.AccountViewModel
import com.chronie.helperunited.ui.settings.SettingsScreen
import com.chronie.helperunited.ui.materials.TodayMaterialsScreen
import com.chronie.helperunited.ui.materials.TodayMaterialsViewModel

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

/**
 * Main app composable with bottom navigation.
 * Tabs: Home, Daily Note, Enka, Gacha, More (Materials, Account, Settings).
 */
@Composable
fun HelperUnitedApp() {
    var currentTab by remember { mutableStateOf<AppTab>(AppTab.Home) }
    var subScreen by remember { mutableStateOf<SubScreen?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = subScreen?.title ?: currentTab.title,
                        fontWeight = FontWeight.Bold,
                    )
                },
            )
        },
        bottomBar = {
            BottomNavBar(
                currentTab = currentTab,
                onTabSelected = {
                    currentTab = it
                    subScreen = null
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            when {
                subScreen != null -> {
                    when (subScreen) {
                        SubScreen.Account -> {
                            val vm: AccountViewModel = hiltViewModel()
                            AccountScreen(viewModel = vm)
                        }
                        SubScreen.Settings -> {
                            SettingsScreen()
                        }
                        SubScreen.Materials -> {
                            val vm: TodayMaterialsViewModel = hiltViewModel()
                            TodayMaterialsScreen(viewModel = vm)
                        }
                        null -> {}
                    }
                }
                currentTab == AppTab.Home -> {
                    HomeScreen(
                        onNavigate = { subScreen = it },
                    )
                }
                currentTab == AppTab.DailyNote -> {
                    val vm: DailyNoteViewModel = hiltViewModel()
                    DailyNoteScreen(viewModel = vm)
                }
                currentTab == AppTab.Enka -> {
                    val vm: EnkaShowcaseViewModel = hiltViewModel()
                    EnkaShowcaseScreen(
                        profile = vm.uiState.value.profile,
                        isLoading = vm.uiState.value.isLoading,
                        error = vm.uiState.value.error,
                        onQuery = { vm.queryShowcase(it) },
                    )
                }
                currentTab == AppTab.Gacha -> {
                    val vm: GachaViewModel = hiltViewModel()
                    val state by vm.uiState.collectAsState()
                    GachaScreen(
                        profiles = state.profiles,
                        entries = state.entries,
                        isLoading = state.isLoading,
                        error = state.error,
                        selectedProfile = state.selectedProfile,
                        onProfileSelect = { vm.selectProfile(it) },
                        onFetch = { uid, game -> vm.fetchGachaRecords(uid, game) },
                        onImport = { vm.importUIGF(it) },
                        onExport = { _, _ -> },
                    )
                }
            }
        }
    }
}

/**
 * Home screen - feature overview and quick access.
 */
@Composable
private fun HomeScreen(
    onNavigate: (SubScreen) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Welcome
        MiuixCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "🍕 Helper United",
                    style = MiuixTheme.textStyles.headlineLarge,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Kotlin/Android Edition · 原神 · 星穹铁道 · 绝区零",
                    style = MiuixTheme.textStyles.bodyMedium,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
        }

        // Quick Actions
        Text(
            text = "快捷功能",
            style = MiuixTheme.textStyles.titleLarge,
            fontWeight = FontWeight.Bold,
        )

        QuickActionCard(
            icon = "📅",
            title = "今日素材",
            subtitle = "查看今天可以刷哪些天赋/武器素材",
            onClick = { onNavigate(SubScreen.Materials) },
        )

        QuickActionCard(
            icon = "👤",
            title = "账号管理",
            subtitle = "添加和管理 HoYoLAB / 米游社账号",
            onClick = { onNavigate(SubScreen.Account) },
        )

        QuickActionCard(
            icon = "⚙️",
            title = "设置",
            subtitle = "关于、版本、链接",
            onClick = { onNavigate(SubScreen.Settings) },
        )

        // Supported Games
        Text(
            text = "支持的游戏",
            style = MiuixTheme.textStyles.titleLarge,
            fontWeight = FontWeight.Bold,
        )

        MiuixCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                GameItem("⚡", "原神", "Genshin Impact", "每日便笺 · 角色展柜 · 抽卡记录")
                Spacer(modifier = Modifier.height(12.dp))
                GameItem("🚂", "崩坏：星穹铁道", "Honkai: Star Rail", "每日便笺 · 角色展柜 · 抽卡记录")
                Spacer(modifier = Modifier.height(12.dp))
                GameItem("🎮", "绝区零", "Zenless Zone Zero", "每日便笺")
            }
        }
    }
}

@Composable
private fun QuickActionCard(
    icon: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    MiuixCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = icon, style = MiuixTheme.textStyles.headlineMedium)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MiuixTheme.textStyles.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = subtitle,
                    style = MiuixTheme.textStyles.bodySmall,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
            Text(text = "›", style = MiuixTheme.textStyles.headlineSmall)
        }
    }
}

@Composable
private fun GameItem(emoji: String, name: String, subtitle: String, features: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = emoji, style = MiuixTheme.textStyles.headlineMedium)
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = name,
                style = MiuixTheme.textStyles.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = subtitle,
                style = MiuixTheme.textStyles.bodySmall,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
            Text(
                text = features,
                style = MiuixTheme.textStyles.bodySmall,
                color = MiuixTheme.colorScheme.primary,
            )
        }
    }
}

/**
 * Bottom navigation bar.
 */
@Composable
private fun BottomNavBar(
    currentTab: AppTab,
    onTabSelected: (AppTab) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        AppTab.entries.forEach { tab ->
            val isSelected = tab == currentTab
            Button(
                onClick = { onTabSelected(tab) },
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = tab.icon,
                        style = MiuixTheme.textStyles.titleMedium,
                    )
                    Text(
                        text = tab.shortTitle,
                        style = MiuixTheme.textStyles.bodySmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) MiuixTheme.colorScheme.primary
                        else MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    )
                }
            }
        }
    }
}

enum class AppTab(
    val icon: String,
    val title: String,
    val shortTitle: String,
) {
    Home("🏠", "Helper United", "首页"),
    DailyNote("⚡", "实时便笺", "便笺"),
    Enka("🎭", "角色展柜", "展柜"),
    Gacha("🎰", "抽卡记录", "抽卡"),
}

enum class SubScreen(val title: String) {
    Account("账号管理"),
    Settings("设置"),
    Materials("今日素材"),
}
