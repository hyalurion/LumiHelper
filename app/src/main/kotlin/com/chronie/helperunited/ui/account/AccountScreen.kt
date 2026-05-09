package com.chronie.helperunited.ui.account

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.chronie.helperunited.base.SupportedGame
import com.chronie.helperunited.model.HoYoAccount
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.MiuixCard
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * Account management screen.
 * Allows users to add, view, and delete HoYoverse accounts.
 * Ported from PizzaHelperUnited's account management UI.
 */
@Composable
fun AccountScreen(
    viewModel: AccountViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "账号管理",
                    style = MiuixTheme.textStyles.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                Button(onClick = { viewModel.setShowAddDialog(true) }) {
                    Text("添加账号")
                }
            }
        }

        // Add Account Form
        if (uiState.showAddDialog) {
            item {
                AddAccountCard(
                    onAdd = { uid, game, cookie, name, isGlobal ->
                        viewModel.addAccount(uid, game, cookie, name, isGlobal)
                    },
                    onCancel = { viewModel.setShowAddDialog(false) },
                )
            }
        }

        // Account List
        if (uiState.accounts.isEmpty() && !uiState.showAddDialog) {
            item {
                MiuixCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "还没有账号",
                            style = MiuixTheme.textStyles.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "点击「添加账号」来添加你的 HoYoLAB 或米游社账号",
                            style = MiuixTheme.textStyles.bodyMedium,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        )
                    }
                }
            }
        }

        items(uiState.accounts) { account ->
            AccountCard(
                account = account,
                onDelete = { viewModel.deleteAccount(account.uuid) },
            )
        }

        // Tips
        item {
            MiuixCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "💡 提示",
                        style = MiuixTheme.textStyles.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• UID 前缀 1-5 为国服（米游社），6-9 为国际服（HoYoLAB）",
                        style = MiuixTheme.textStyles.bodySmall,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    )
                    Text(
                        text = "• Cookie 用于访问 HoYoLAB/米游社 API 获取每日便笺等数据",
                        style = MiuixTheme.textStyles.bodySmall,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    )
                    Text(
                        text = "• 所有数据仅存储在本地，不会上传到任何服务器",
                        style = MiuixTheme.textStyles.bodySmall,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    )
                }
            }
        }
    }
}

@Composable
private fun AccountCard(
    account: HoYoAccount,
    onDelete: () -> Unit,
) {
    MiuixCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Game icon
            Text(
                text = when (account.game) {
                    SupportedGame.GENSHIN_IMPACT -> "⚡"
                    SupportedGame.STAR_RAIL -> "🚂"
                    SupportedGame.ZENLESS_ZONE -> "🎮"
                },
                style = MiuixTheme.textStyles.headlineMedium,
            )
            Spacer(modifier = Modifier.width(12.dp))

            // Account info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = account.name,
                    style = MiuixTheme.textStyles.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "UID: ${account.uid} · ${if (account.region.isGlobal) "国际服" else "国服"}",
                    style = MiuixTheme.textStyles.bodySmall,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }

            // Delete button
            Button(onClick = onDelete) {
                Text("删除")
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddAccountCard(
    onAdd: (String, SupportedGame, String, String, Boolean) -> Unit,
    onCancel: () -> Unit,
) {
    var uid by remember { mutableStateOf("") }
    var cookie by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var selectedGame by remember { mutableStateOf(SupportedGame.GENSHIN_IMPACT) }
    var isGlobal by remember { mutableStateOf(true) }

    MiuixCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "添加账号",
                style = MiuixTheme.textStyles.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Game selection
            Text(
                text = "游戏",
                style = MiuixTheme.textStyles.bodyMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(4.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SupportedGame.entries.forEach { game ->
                    Button(onClick = { selectedGame = game }) {
                        Text(
                            text = game.shortName,
                            fontWeight = if (selectedGame == game) FontWeight.Bold else FontWeight.Normal,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Region
            Text(
                text = "服务器",
                style = MiuixTheme.textStyles.bodyMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(4.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { isGlobal = true }) {
                    Text("国际服 (HoYoLAB)", fontWeight = if (isGlobal) FontWeight.Bold else FontWeight.Normal)
                }
                Button(onClick = { isGlobal = false }) {
                    Text("国服 (米游社)", fontWeight = if (!isGlobal) FontWeight.Bold else FontWeight.Normal)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // UID
            TextField(
                value = uid,
                onValueChange = { uid = it },
                label = "游戏 UID",
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Name (optional)
            TextField(
                value = name,
                onValueChange = { name = it },
                label = "备注名（可选）",
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Cookie
            TextField(
                value = cookie,
                onValueChange = { cookie = it },
                label = "Cookie",
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("取消")
                }
                Button(
                    onClick = {
                        if (uid.isNotBlank() && cookie.isNotBlank()) {
                            onAdd(uid, selectedGame, cookie, name, isGlobal)
                        }
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("添加")
                }
            }
        }
    }
}
