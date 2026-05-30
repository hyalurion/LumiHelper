package com.chronie.helperunited.ui.account.login

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.Refresh
import top.yukonga.miuix.kmp.theme.MiuixTheme
import com.chronie.helperunited.base.RegionType
import com.chronie.helperunited.base.SupportedGame

private fun getAccountPageLoginURL(regionType: RegionType, game: SupportedGame): String {
    return when (regionType) {
        RegionType.MIYOUSHE -> "https://user.mihoyo.com/#/login/captcha"
        RegionType.HOYOLAB -> when (game) {
            SupportedGame.GENSHIN_IMPACT -> "https://act.hoyolab.com/app/community-game-records-sea/m.html#/ys"
            SupportedGame.STAR_RAIL -> "https://act.hoyolab.com/app/community-game-records-sea/rpg/m.html#/hsr"
            SupportedGame.ZENLESS_ZONE -> "https://act.hoyolab.com/app/zzz-game-record/index.html#/zzz"
            else -> "https://account.hoyolab.com/#/login"
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun GetCookieWebViewScreen(
    onBack: () -> Unit,
    onCookieExtracted: (cookies: HoYoLabCookieData) -> Unit,
    regionType: RegionType = RegionType.HOYOLAB,
    game: SupportedGame = SupportedGame.GENSHIN_IMPACT,
) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }
    var pageTitle by remember { mutableStateOf("") }
    var currentUrl by remember { mutableStateOf("") }

    val loginUrl = remember(regionType, game) {
        getAccountPageLoginURL(regionType, game)
    }

    val titleText = when (regionType) {
        RegionType.MIYOUSHE -> "米游社登录"
        RegionType.HOYOLAB -> "HoYoLab 登录"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = pageTitle.ifBlank { titleText },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = MiuixIcons.Back,
                            contentDescription = "返回",
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { isLoading = true }) {
                        Icon(
                            imageVector = MiuixIcons.Refresh,
                            contentDescription = "刷新",
                        )
                    }
                    Button(
                        onClick = {
                            val cookie = extractCookie()
                            if (cookie != null) {
                                onCookieExtracted(cookie)
                            } else {
                                Toast.makeText(
                                    context,
                                    "未检测到登录Cookie，请先登录",
                                    Toast.LENGTH_SHORT,
                                ).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColorsPrimary(),
                    ) {
                        Text("完成")
                    }
                },
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            ) {
                Button(
                    onClick = {
                        val cookie = extractCookie()
                        if (cookie != null) {
                            onCookieExtracted(cookie)
                        } else {
                            Toast.makeText(
                                context,
                                "未检测到登录Cookie，请先登录",
                                Toast.LENGTH_SHORT,
                            ).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColorsPrimary(),
                ) {
                    Text("完成登录并获取 Cookie")
                }
            }
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.userAgentString =
                            "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 " +
                                "(KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"

                        val cookieManager = CookieManager.getInstance()
                        cookieManager.setAcceptCookie(true)
                        cookieManager.setAcceptThirdPartyCookies(this, true)

                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                super.onPageStarted(view, url, favicon)
                                url?.let { currentUrl = it }
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                isLoading = false
                                pageTitle = view?.title ?: ""
                                url?.let { currentUrl = it }
                            }

                            override fun shouldOverrideUrlLoading(
                                view: WebView?,
                                url: String?,
                            ): Boolean {
                                if (url != null) currentUrl = url
                                return super.shouldOverrideUrlLoading(view, url)
                            }
                        }
                        loadUrl(loginUrl)
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            )

            AnimatedVisibility(
                visible = isLoading,
                modifier = Modifier.align(Alignment.Center),
            ) {
                CircularProgressIndicator()
            }

            if (regionType == RegionType.HOYOLAB) {
                Column(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(
                            top = padding.calculateTopPadding() + 8.dp,
                            start = 16.dp,
                            end = 16.dp,
                        ),
                ) {
                    Card {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = "请在浏览器中登录你的 HoYoLab 账号",
                                style = MiuixTheme.textStyles.body2,
                                textAlign = TextAlign.Center,
                            )
                            Text(
                                text = "登录成功后点击下方按钮获取 Cookie",
                                style = MiuixTheme.textStyles.body2,
                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }
        }
    }
}

data class HoYoLabCookieData(
    val ltoken: String,
    val ltuid: String,
    val cookieToken: String,
    val accountId: String,
    val rawCookies: String,
)

private fun extractCookie(): HoYoLabCookieData? {
    val cookieManager = CookieManager.getInstance()
    val cookies = cookieManager.getCookie("https://account.hoyolab.com") ?: ""
    val hoyolabCookies = cookieManager.getCookie(".hoyolab.com") ?: ""

    val allCookies = "$cookies;$hoyolabCookies"
    if (allCookies.isBlank()) return null

    val cookieMap = allCookies
        .split(";")
        .mapNotNull { part ->
            val trimmed = part.trim()
            val eqIndex = trimmed.indexOf('=')
            if (eqIndex > 0) {
                trimmed.substring(0, eqIndex) to trimmed.substring(eqIndex + 1)
            } else null
        }
        .toMap()

    val ltoken = cookieMap["ltoken_v2"] ?: cookieMap["ltoken"] ?: return null
    val ltuid = cookieMap["ltuid_v2"] ?: cookieMap["ltuid"] ?: return null
    val cookieToken = cookieMap["cookie_token_v2"]
        ?: cookieMap["cookie_token"] ?: return null
    val accountId = cookieMap["account_id_v2"]
        ?: cookieMap["account_id"] ?: ltuid

    return HoYoLabCookieData(
        ltoken = ltoken,
        ltuid = ltuid,
        cookieToken = cookieToken,
        accountId = accountId,
        rawCookies = groupCookies(allCookies),
    )
}

private fun groupCookies(rawCookie: String): String {
    val targetCookies = listOf(
        "ltoken_v2", "ltuid_v2", "ltoken", "ltuid",
        "cookie_token_v2", "cookie_token", "account_id_v2", "account_id",
    )
    return rawCookie.split(";")
        .map { it.trim() }
        .filter { part ->
            targetCookies.any { part.startsWith("$it=") }
        }
        .joinToString("; ")
}