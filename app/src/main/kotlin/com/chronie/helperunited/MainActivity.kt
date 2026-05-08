package com.chronie.helperunited

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.AndroidEntryPoint
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
                            is Screen.Home -> "Helper United"
                            is Screen.EnkaShowcase -> "角色展柜"
                            is Screen.Gacha -> "抽卡记录"
                        }
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

@Composable
private fun HomeScreen(
    onEnkaShowcaseClick: () -> Unit,
    onGachaClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Text("🍕 Pizza Helper United - Kotlin/Android Edition")
        Text("miuix v0.9.0 + Kotlin Multi-module")

        // Navigation buttons
        androidx.compose.material3.TextButton(
            onClick = onEnkaShowcaseClick,
        ) {
            Text("角色展柜 (Enka)")
        }
        androidx.compose.material3.TextButton(
            onClick = onGachaClick,
        ) {
            Text("抽卡记录 (Gacha)")
        }
    }
}

sealed class Screen {
    data object Home : Screen()
    data object EnkaShowcase : Screen()
    data object Gacha : Screen()
}
