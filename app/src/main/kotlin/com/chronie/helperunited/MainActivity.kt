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
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import dagger.hilt.android.AndroidEntryPoint
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.FloatingNavigationBar
import top.yukonga.miuix.kmp.basic.FloatingNavigationBarItem
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.utils.PressFeedbackType
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
import com.chronie.helperunited.ui.ledger.LedgerScreen
import com.chronie.helperunited.ui.utils.UtilsScreen
import com.chronie.helperunited.ui.utils.UtilsSubPage
import com.chronie.helperunited.ui.materials.TodayMaterialsScreen
import com.chronie.helperunited.ui.materials.TodayMaterialsViewModel
import com.chronie.helperunited.ui.legal.PrivacyPolicyScreen
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
                title = subScreen?.title ?: currentTab.title,
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
                                    LedgerScreen()
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
                                    UtilsScreen(
                                        onNavigate = { utilsPage ->
                                            subScreen = when (utilsPage) {
                                                UtilsSubPage.Account -> SubScreen.Account
                                                UtilsSubPage.Characters -> SubScreen.Characters
                                                UtilsSubPage.Gacha -> SubScreen.Gacha
                                                UtilsSubPage.Materials -> SubScreen.Materials
                                                UtilsSubPage.Challenge -> SubScreen.Challenge
                                                UtilsSubPage.ZZZShowcase -> SubScreen.ZZZShowcase
                                                UtilsSubPage.GachaAnalytics -> SubScreen.GachaAnalytics
                                                UtilsSubPage.EventCalendar -> SubScreen.EventCalendar
                                            }
                                        },
                                    )
                                }
                                AppTab.Settings -> {
                                    SettingsScreen(
                                        onNavigateToAccount = { subScreen = SubScreen.Account },
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
