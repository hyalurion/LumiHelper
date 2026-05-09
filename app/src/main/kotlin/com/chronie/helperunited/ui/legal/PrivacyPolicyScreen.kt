package com.chronie.helperunited.ui.legal

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.MiuixCard
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

private const val PREFS_NAME = "legal_prefs"
private const val KEY_PRIVACY_ACCEPTED = "privacy_policy_accepted"

/**
 * Privacy Policy screen.
 * Displays the privacy policy text and allows the user to accept it.
 * Acceptance state is persisted in SharedPreferences.
 */
@Composable
fun PrivacyPolicyScreen(
    onAccepted: () -> Unit = {},
) {
    val context = LocalContext.current
    var accepted by remember { mutableStateOf(isPrivacyAccepted(context)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Header
        MiuixCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "🔒 隐私政策",
                    style = MiuixTheme.textStyles.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "最后更新：2024 年 12 月",
                    style = MiuixTheme.textStyles.bodySmall,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
        }

        // Privacy Policy Content
        MiuixCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                PrivacySection(
                    title = "1. 信息收集",
                    content = "Helper United（\"本应用\"）是一款开源工具类应用，帮助玩家查看游戏数据。" +
                            "本应用不收集任何个人身份信息（PII）。所有游戏数据均通过官方公开 API 获取，" +
                            "仅存储在您的设备本地。",
                )
                Spacer(modifier = Modifier.height(16.dp))

                PrivacySection(
                    title = "2. 数据存储",
                    content = "您的 HoYoLAB 账号凭据（Cookie）仅存储在设备本地，不会上传至任何服务器。" +
                            "抽卡记录、角色数据等均保存在本地数据库中。您可以随时在设置中清除所有数据。",
                )
                Spacer(modifier = Modifier.height(16.dp))

                PrivacySection(
                    title = "3. 网络请求",
                    content = "本应用会向以下服务发送网络请求：\n" +
                            "• HoYoLAB / 米游社 API（获取游戏数据）\n" +
                            "• Enka.Network API（获取角色展柜数据）\n" +
                            "• GitHub（检查更新、获取活动日历）\n" +
                            "所有请求均为功能所需，不会用于追踪或广告目的。",
                )
                Spacer(modifier = Modifier.height(16.dp))

                PrivacySection(
                    title = "4. 第三方服务",
                    content = "本应用使用以下开源库，详见「开源许可」页面：\n" +
                            "• Jetpack Compose（UI 框架）\n" +
                            "• MIUIX（UI 组件库）\n" +
                            "• Retrofit / Moshi（网络请求）\n" +
                            "• Hilt（依赖注入）\n" +
                            "• Coil（图片加载）\n" +
                            "这些库本身不收集用户数据。",
                )
                Spacer(modifier = Modifier.height(16.dp))

                PrivacySection(
                    title = "5. 权限说明",
                    content = "• 互联网权限：用于获取游戏数据和检查更新\n" +
                            "• 通知权限：用于树脂/开拓力/电量提醒\n" +
                            "• 前台服务权限：用于后台数据刷新\n" +
                            "本应用不会请求通讯录、位置、相机等敏感权限。",
                )
                Spacer(modifier = Modifier.height(16.dp))

                PrivacySection(
                    title = "6. 开源与透明",
                    content = "本应用完全开源，源代码托管于 GitHub。任何人都可以审查代码，" +
                            "确认本隐私政策的真实性。如果您发现任何不符之处，欢迎提交 Issue。",
                )
                Spacer(modifier = Modifier.height(16.dp))

                PrivacySection(
                    title = "7. 政策更新",
                    content = "我们可能会不定期更新本隐私政策。更新后的政策将在应用内展示，" +
                            "重大变更时会要求您重新确认。",
                )
                Spacer(modifier = Modifier.height(16.dp))

                PrivacySection(
                    title = "8. 联系方式",
                    content = "如有任何隐私相关问题，请通过 GitHub Issues 联系我们：\n" +
                            "https://github.com/pizza-studio/PizzaHelperUnited/issues",
                )
            }
        }

        // Accept Button
        if (!accepted) {
            Button(
                onClick = {
                    setPrivacyAccepted(context, true)
                    accepted = true
                    onAccepted()
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "我已阅读并同意隐私政策",
                    style = MiuixTheme.textStyles.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        } else {
            MiuixCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "✅ 您已于之前同意本隐私政策",
                        style = MiuixTheme.textStyles.bodyMedium,
                        color = MiuixTheme.colorScheme.primary,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun PrivacySection(title: String, content: String) {
    Text(
        text = title,
        style = MiuixTheme.textStyles.titleSmall,
        fontWeight = FontWeight.Bold,
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        text = content,
        style = MiuixTheme.textStyles.bodySmall,
        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
    )
}

fun isPrivacyAccepted(context: Context): Boolean {
    return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getBoolean(KEY_PRIVACY_ACCEPTED, false)
}

private fun setPrivacyAccepted(context: Context, accepted: Boolean) {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putBoolean(KEY_PRIVACY_ACCEPTED, accepted)
        .apply()
}
