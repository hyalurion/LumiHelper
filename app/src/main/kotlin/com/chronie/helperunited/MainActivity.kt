package com.chronie.helperunited

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.clickable
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import dagger.hilt.android.AndroidEntryPoint
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.FloatingNavigationBar
import top.yukonga.miuix.kmp.basic.FloatingNavigationBarItem
import top.yukonga.miuix.kmp.utils.PressFeedbackType
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Alarm
import top.yukonga.miuix.kmp.icon.extended.Contacts
import top.yukonga.miuix.kmp.icon.extended.FolderFill
import top.yukonga.miuix.kmp.icon.extended.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.chronie.helperunited.ui.today.TodayScreen
import com.chronie.helperunited.ui.enka.EnkaShowcaseScreen
import com.chronie.helperunited.ui.enka.EnkaShowcaseViewModel
import com.chronie.helperunited.ui.gacha.GachaScreen
import com.chronie.helperunited.ui.gacha.GachaViewModel
import com.chronie.helperunited.ui.gacha.analytics.GachaAnalyticsScreen
import com.chronie.helperunited.ui.gacha.analytics.GachaAnalyticsViewModel
import com.chronie.helperunited.ui.hoyolab.HoYoLabCharacterScreen
import com.chronie.helperunited.ui.hoyolab.HoYoLabCharacterViewModel
import com.chronie.helperunited.ui.account.AccountNavigation
import com.chronie.helperunited.ui.settings.SettingsScreen
import com.chronie.helperunited.ui.materials.TodayMaterialsScreen
import com.chronie.helperunited.ui.materials.TodayMaterialsViewModel
import com.chronie.helperunited.ui.legal.PrivacyPolicyScreen
import com.chronie.helperunited.ui.sign.SignViewModel
import com.chronie.helperunited.ui.challenge.ChallengeScreen
import com.chronie.helperunited.ui.challenge.ChallengeViewModel
import com.chronie.helperunited.ui.event.EventCalendarScreen
import com.chronie.helperunited.ui.event.EventCalendarViewModel
import com.chronie.helperunited.ui.zzz.ZZZShowcaseScreen
import com.chronie.helperunited.ui.zzz.ZZZShowcaseViewModel

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
 * Main app composable with floating bottom navigation.
 * Tabs: Today (daily note), Showcase (character builds), Utils, AppSettings.
 */
@Composable
fun HelperUnitedApp() {
    var currentTab by remember { mutableStateOf<AppTab>(AppTab.Today) }
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
        // Determine if we're switching tabs (fade) or navigating to sub-screen (slide)
        val isSubScreen = subScreen != null

        AnimatedContent(
            targetState = subScreen,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            transitionSpec = {
                if (targetState != null) {
                    // Sub-screen: slide in from right
                    (slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(300)
                    ) + fadeIn(animationSpec = tween(300)))
                        .togetherWith(
                            slideOutHorizontally(
                                targetOffsetX = { -it / 3 },
                                animationSpec = tween(300)
                            ) + fadeOut(animationSpec = tween(300))
                        )
                } else if (initialState != null) {
                    // Returning from sub-screen: slide in from left
                    (slideInHorizontally(
                        initialOffsetX = { -it / 3 },
                        animationSpec = tween(300)
                    ) + fadeIn(animationSpec = tween(300)))
                        .togetherWith(
                            slideOutHorizontally(
                                targetOffsetX = { it },
                                animationSpec = tween(300)
                            ) + fadeOut(animationSpec = tween(300))
                        )
                } else {
                    // Tab switch: fade
                    fadeIn(animationSpec = tween(200))
                        .togetherWith(fadeOut(animationSpec = tween(200)))
                }
            },
            label = "NavContent",
        ) { currentSubScreen ->
            Column(modifier = Modifier.fillMaxSize()) {
                when {
                    currentSubScreen != null -> {
                        when (currentSubScreen) {
                            SubScreen.Account -> {
                                AccountNavigation()
                            }
                            SubScreen.Characters -> {
                                val vm: HoYoLabCharacterViewModel = hiltViewModel()
                                HoYoLabCharacterScreen(viewModel = vm)
                            }
                            SubScreen.Gacha -> {
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
                                    onNavigateToAnalytics = {
                                        subScreen = SubScreen.GachaAnalytics
                                    },
                                )
                            }
                            SubScreen.Materials -> {
                                val vm: TodayMaterialsViewModel = hiltViewModel()
                                TodayMaterialsScreen(viewModel = vm)
                            }
                            SubScreen.ZZZShowcase -> {
                                val vm: ZZZShowcaseViewModel = hiltViewModel()
                                ZZZShowcaseScreen(
                                    profile = vm.uiState.value.profile,
                                    isLoading = vm.uiState.value.isLoading,
                                    error = vm.uiState.value.error,
                                )
                            }
                            SubScreen.GachaAnalytics -> {
                                val vm: GachaAnalyticsViewModel = hiltViewModel()
                                GachaAnalyticsScreen(viewModel = vm)
                            }
                            SubScreen.Challenge -> {
                                val vm: ChallengeViewModel = hiltViewModel()
                                ChallengeScreen(viewModel = vm)
                            }
                            SubScreen.EventCalendar -> {
                                val vm: EventCalendarViewModel = hiltViewModel()
                                EventCalendarScreen(viewModel = vm)
                            }
                            SubScreen.PrivacyPolicy -> {
                                PrivacyPolicyScreen()
                            }
                            SubScreen.Ledger -> {
                                subScreen = null
                            }
                        }
                    }
                    else -> {
                        // Tab content also uses AnimatedContent for tab switches
                        AnimatedContent(
                            targetState = currentTab,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(200))
                                    .togetherWith(fadeOut(animationSpec = tween(200)))
                            },
                            label = "TabContent",
                        ) { tab ->
                            when (tab) {
                                AppTab.Today -> {
                                    TodayScreen(
                                        onNavigateToMaterials = { subScreen = SubScreen.Materials },
                                        onNavigateToEvents = { subScreen = SubScreen.EventCalendar },
                                    )
                                }
                                AppTab.Showcase -> {
                                    val vm: EnkaShowcaseViewModel = hiltViewModel()
                                    EnkaShowcaseScreen(
                                        onNavigateToCharacters = { subScreen = SubScreen.Characters },
                                        onNavigateToChallenge = { subScreen = SubScreen.Challenge },
                                        onNavigateToLedger = { subScreen = SubScreen.Ledger },
                                        viewModel = vm,
                                    )
                                }
                                AppTab.Utils -> {
                                    HomeScreen(
                                        onNavigate = { subScreen = it },
                                    )
                                }
                                AppTab.Settings -> {
                                    SettingsScreen(
                                        onNavigateToPrivacy = { subScreen = SubScreen.PrivacyPolicy },
                                    )
                                }
                            }
                        }
                    }
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
    val signVm: SignViewModel = hiltViewModel()
    val signState by signVm.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Welcome
        Card(modifier = Modifier.fillMaxWidth(), pressFeedbackType = PressFeedbackType.Sink) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "🍕 Helper United",
                    style = MiuixTheme.textStyles.headline1,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Kotlin/Android Edition · 原神 · 星穹铁道 · 绝区零",
                    style = MiuixTheme.textStyles.body2,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
        }

        // HoYoLAB Auto Sign-in Card
        SignInCard(
            signState = signState,
            onSignInAll = { signVm.signInAll() },
            onRefresh = { signVm.loadSignStatus() },
        )

        // Quick Actions
        Text(
            text = "快捷功能",
            style = MiuixTheme.textStyles.title1,
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
            icon = "👥",
            title = "角色列表",
            subtitle = "查看 HoYoLAB 角色信息、天赋、命之座",
            onClick = { onNavigate(SubScreen.Characters) },
        )

        QuickActionCard(
            icon = "🎰",
            title = "抽卡记录",
            subtitle = "导入 UIGF 格式抽卡记录、查看统计",
            onClick = { onNavigate(SubScreen.Gacha) },
        )

        QuickActionCard(
            icon = "🎮",
            title = "绝区零展柜",
            subtitle = "查看 ZZZ 角色、W-Engine、驱动盘",
            onClick = { onNavigate(SubScreen.ZZZShowcase) },
        )

        QuickActionCard(
            icon = "📊",
            title = "抽卡分析",
            subtitle = "查看抽卡记录图表、保底统计、稀有度分布",
            onClick = { onNavigate(SubScreen.GachaAnalytics) },
        )

        QuickActionCard(
            icon = "🌙",
            title = "挑战记录",
            subtitle = "深境螺旋 · 混沌回忆 · 虚构叙事",
            onClick = { onNavigate(SubScreen.Challenge) },
        )

        QuickActionCard(
            icon = "📅",
            title = "活动日历",
            subtitle = "查看当前和即将开始的游戏活动、版本倒计时",
            onClick = { onNavigate(SubScreen.EventCalendar) },
        )

        // Supported Games
        Text(
            text = "支持的游戏",
            style = MiuixTheme.textStyles.title1,
            fontWeight = FontWeight.Bold,
        )

        Card(modifier = Modifier.fillMaxWidth(), pressFeedbackType = PressFeedbackType.Sink) {
            Column(modifier = Modifier.padding(16.dp)) {
                GameItem("⚡", "原神", "Genshin Impact", "每日便笺 · 角色展柜 · 抽卡记录")
                Spacer(modifier = Modifier.height(12.dp))
                GameItem("🚂", "崩坏：星穹铁道", "Honkai: Star Rail", "每日便笺 · 角色展柜 · 抽卡记录")
                Spacer(modifier = Modifier.height(12.dp))
                GameItem("🎮", "绝区零", "Zenless Zone Zero", "每日便笺 · 角色展柜 · 抽卡记录")
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        pressFeedbackType = PressFeedbackType.Sink,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = icon, style = MiuixTheme.textStyles.headline2)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MiuixTheme.textStyles.title2,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = subtitle,
                    style = MiuixTheme.textStyles.main,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
            Text(text = "›", style = MiuixTheme.textStyles.headline2)
        }
    }
}

@Composable
private fun GameItem(emoji: String, name: String, subtitle: String, features: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = emoji, style = MiuixTheme.textStyles.headline2)
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = name,
                style = MiuixTheme.textStyles.title2,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = subtitle,
                style = MiuixTheme.textStyles.main,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
            Text(
                text = features,
                style = MiuixTheme.textStyles.main,
                color = MiuixTheme.colorScheme.primary,
            )
        }
    }
}

/**
 * HoYoLAB Auto Sign-in card for the Home screen.
 */
@Composable
private fun SignInCard(
    signState: com.chronie.helperunited.ui.sign.SignUiState,
    onSignInAll: () -> Unit,
    onRefresh: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth(), pressFeedbackType = PressFeedbackType.Sink) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = "📝", style = MiuixTheme.textStyles.headline2)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "HoYoLAB 签到",
                        style = MiuixTheme.textStyles.title2,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "每日签到获取奖励",
                        style = MiuixTheme.textStyles.main,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    )
                }
                Button(
                    onClick = onRefresh,
                    enabled = !signState.isLoading && !signState.isSigning,
                ) {
                    Text("刷新")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onSignInAll,
                    enabled = !signState.isLoading && !signState.isSigning,
                ) {
                    Text(if (signState.isSigning) "签到中..." else "一键签到")
                }
            }

            if (signState.error != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = signState.error,
                    style = MiuixTheme.textStyles.main,
                    color = MiuixTheme.colorScheme.error,
                )
            }

            if (signState.signStatuses.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                signState.signStatuses.forEach { status ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = if (status.isSigned) "✅" else "❌",
                            style = MiuixTheme.textStyles.body2,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = status.game.displayName,
                            style = MiuixTheme.textStyles.body2,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        if (status.error != null) {
                            Text(
                                text = status.error!!,
                                style = MiuixTheme.textStyles.main,
                                color = MiuixTheme.colorScheme.error,
                            )
                        } else {
                            Text(
                                text = if (status.isSigned) "已签到 (${status.totalSignDays}天)"
                                else "未签到",
                                style = MiuixTheme.textStyles.main,
                                color = if (status.isSigned) MiuixTheme.colorScheme.primary
                                else MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            )
                        }
                    }
                }
            }

            if (signState.isLoading) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

/**
 * Floating bottom navigation bar with MIUIX style.
 */
@Composable
private fun BottomNavBar(
    currentTab: AppTab,
    onTabSelected: (AppTab) -> Unit,
) {
    FloatingNavigationBar(
        cornerRadius = 28.dp,
        color = MiuixTheme.colorScheme.surfaceContainer,
    ) {
        AppTab.entries.forEach { tab ->
            FloatingNavigationBarItem(
                selected = tab == currentTab,
                onClick = { onTabSelected(tab) },
                icon = tab.icon,
                label = tab.shortTitle,
            )
        }
    }
}

enum class AppTab(
    val icon: ImageVector,
    val title: String,
    val shortTitle: String,
) {
    Today(MiuixIcons.Alarm, "实时便笺", "便笺"),
    Showcase(MiuixIcons.Contacts, "角色展柜", "展柜"),
    Utils(MiuixIcons.FolderFill, "工具", "工具"),
    Settings(MiuixIcons.Settings, "设置", "设置"),
}

enum class SubScreen(val title: String) {
    Account("账号管理"),
    Characters("角色列表"),
    Gacha("抽卡记录"),
    Materials("今日素材"),
    Challenge("挑战记录"),
    ZZZShowcase("绝区零展柜"),
    GachaAnalytics("抽卡分析"),
    EventCalendar("活动日历"),
    PrivacyPolicy("隐私政策"),
    Ledger("月度账本"),
}
