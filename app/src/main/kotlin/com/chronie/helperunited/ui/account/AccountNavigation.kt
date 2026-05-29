package com.chronie.helperunited.ui.account

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.chronie.helperunited.ui.account.login.GetCookieWebViewScreen
import com.chronie.helperunited.ui.account.login.QrCodeLoginScreen

@Composable
fun AccountNavigation(
    viewModel: AccountViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var showWebView by remember { mutableStateOf(false) }
    var showQrCode by remember { mutableStateOf(false) }

    when {
        showWebView -> {
            GetCookieWebViewScreen(
                onBack = { showWebView = false },
                onCookieExtracted = { cookie ->
                    viewModel.onHoYoLabCookieExtracted(cookie)
                    showWebView = false
                },
                regionType = uiState.selectedRegionType,
                game = uiState.selectedGame,
            )
        }
        showQrCode -> {
            QrCodeLoginScreen(
                onBack = { showQrCode = false },
                onLoginComplete = { accounts, stoken, cookieToken, loginUid, deviceFp ->
                    viewModel.onQrCodeLoginComplete(accounts, stoken, cookieToken, loginUid, deviceFp)
                    showQrCode = false
                },
            )
        }
        else -> {
            AccountScreen(
                onNavigateToWebView = { showWebView = true },
                onNavigateToQrCode = { showQrCode = true },
                viewModel = viewModel,
            )
        }
    }
}