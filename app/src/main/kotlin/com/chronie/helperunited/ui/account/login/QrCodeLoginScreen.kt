package com.chronie.helperunited.ui.account.login

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Error
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.chronie.helperunited.network.FetchedAccount
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.LazyColumn
import top.yukonga.miuix.kmp.basic.ProgressIndicator
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme
import java.io.File
import java.io.FileOutputStream

private const val MIYOUSHE_SCHEME = "mihoyobbs://"
private const val MIYOUSHE_APP_STORE = "https://apps.apple.com/cn/app/id1470182559"

@Composable
fun QrCodeLoginScreen(
    onBack: () -> Unit,
    onLoginComplete: (accounts: List<FetchedAccount>, stoken: String?, cookieToken: String?, loginUid: String?, deviceFp: String?) -> Unit,
    viewModel: QrCodeLoginViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showNotScannedAlert by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.startQRLogin()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("扫码登录")
                },
                navigationIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "返回",
                        modifier = Modifier,
                    )
                },
                onNavigationClick = {
                    viewModel.reset()
                    onBack()
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
            }

            if (uiState.error != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Error,
                                contentDescription = null,
                                tint = MiuixTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp),
                            )
                            Text(
                                text = uiState.error!!,
                                color = MiuixTheme.colorScheme.error,
                                style = MiuixTheme.textStyles.body2,
                            )
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        when (uiState.status) {
                            QrCodeStatus.Loading -> {
                                Box(
                                    modifier = Modifier.size(220.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    ProgressIndicator(
                                        modifier = Modifier.size(48.dp),
                                    )
                                }
                            }

                            QrCodeStatus.Error -> {
                                Box(
                                    modifier = Modifier.size(220.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Error,
                                            contentDescription = null,
                                            tint = MiuixTheme.colorScheme.textSecondary,
                                            modifier = Modifier.size(48.dp),
                                        )
                                        Text(
                                            text = "二维码生成失败",
                                            style = MiuixTheme.textStyles.body2,
                                            color = MiuixTheme.colorScheme.textSecondary,
                                        )
                                    }
                                }
                            }

                            else -> {
                                val qrBitmap = remember(uiState.qrCodeUrl) {
                                    uiState.qrCodeUrl?.let { generateQRCodeBitmap(it, 512) }
                                }

                                if (qrBitmap != null) {
                                    Box(
                                        modifier = Modifier
                                            .size(220.dp)
                                            .clickable {
                                                shareQRCode(context, qrBitmap)
                                            },
                                    ) {
                                        Image(
                                            bitmap = qrBitmap.asImageBitmap(),
                                            contentDescription = "登录二维码，点击分享",
                                            modifier = Modifier.fillMaxSize(),
                                        )
                                    }

                                    Text(
                                        text = "点击二维码分享",
                                        style = MiuixTheme.textStyles.caption,
                                        color = MiuixTheme.colorScheme.textSecondary,
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier.size(220.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        ProgressIndicator(modifier = Modifier.size(48.dp))
                                    }
                                }

                                Text(
                                    text = when (uiState.status) {
                                        QrCodeStatus.WaitingForScan -> "请打开米游社App扫码"
                                        QrCodeStatus.Scanned -> "已扫码，请确认登录"
                                        QrCodeStatus.Confirmed -> "登录成功，正在获取账号信息..."
                                        else -> ""
                                    },
                                    style = MiuixTheme.textStyles.body1,
                                    color = MiuixTheme.colorScheme.textSecondary,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }
                }
            }

            if (uiState.status == QrCodeStatus.Scanned) {
                item {
                    Button(
                        onClick = {
                            viewModel.checkScannedManually(
                                onNotScanned = { showNotScannedAlert = true },
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("我已扫码，检查登录状态")
                    }
                }
            }

            if (uiState.qrCodeUrl != null || uiState.error != null) {
                item {
                    Button(
                        onClick = { viewModel.reCreateQRCode() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(),
                    ) {
                        Text("重新生成二维码")
                    }
                }
            }

            item {
                val miyousheInstalled = isMiyousheInstalled(context)
                val linkText = if (miyousheInstalled) {
                    "打开米游社"
                } else {
                    "前往 App Store 下载米游社"
                }
                val linkUri = if (miyousheInstalled) {
                    "${MIYOUSHE_SCHEME}me"
                } else {
                    MIYOUSHE_APP_STORE
                }

                Button(
                    onClick = {
                        openLink(context, linkUri)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(),
                ) {
                    Text(linkText)
                }
            }

            item {
                Text(
                    text = "请使用米游社App扫描二维码登录。\n扫描成功后，请在手机上点击确认登录。\n确认后本页面将自动获取您的游戏账号信息。",
                    style = MiuixTheme.textStyles.caption,
                    color = MiuixTheme.colorScheme.textSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                )
            }

            if (uiState.fetchedAccounts.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "检测到以下游戏账号",
                        style = MiuixTheme.textStyles.title3,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                items(uiState.fetchedAccounts.size) { index ->
                    val account = uiState.fetchedAccounts[index]
                    FetchedAccountCard(account)
                }

                item {
                    Button(
                        onClick = {
                            onLoginComplete(
                                uiState.fetchedAccounts,
                                uiState.stoken,
                                uiState.cookieToken,
                                uiState.loginUid,
                                uiState.deviceFp,
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("完成登录")
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }

    if (showNotScannedAlert) {
        OverlayDialog(
            show = true,
            onDismissRequest = { showNotScannedAlert = false },
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "未检测到扫码",
                    style = MiuixTheme.textStyles.title2,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "请确保已打开米游社App并扫描二维码，然后在手机上确认登录。",
                    style = MiuixTheme.textStyles.body2,
                    textAlign = TextAlign.Center,
                )
                Button(
                    onClick = { showNotScannedAlert = false },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("知道了")
                }
            }
        }
    }
}

@Composable
private fun FetchedAccountCard(account: FetchedAccount) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = account.nickname.ifBlank { "旅行者" },
                style = MiuixTheme.textStyles.title3,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = "UID: ${account.gameUid} | Lv.${account.level}",
                style = MiuixTheme.textStyles.body2,
                color = MiuixTheme.colorScheme.textSecondary,
            )
        }
    }
}

private fun generateQRCodeBitmap(text: String, size: Int): Bitmap {
    val writer = QRCodeWriter()
    val bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, size, size)
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    for (x in 0 until size) {
        for (y in 0 until size) {
            bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
        }
    }
    return bitmap
}

private fun shareQRCode(context: Context, bitmap: Bitmap) {
    try {
        val cacheDir = File(context.cacheDir, "shared_images")
        cacheDir.mkdirs()
        val file = File(cacheDir, "qr_code.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file,
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "分享二维码"))
    } catch (e: Exception) {
        // share failed silently
    }
}

private fun isMiyousheInstalled(context: Context): Boolean {
    val intent = context.packageManager.getLaunchIntentForPackage("com.mihoyo.hyperion")
    return intent != null
}

private fun openLink(context: Context, uri: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(uri))
        context.startActivity(intent)
    } catch (e: Exception) {
        // open failed silently
    }
}