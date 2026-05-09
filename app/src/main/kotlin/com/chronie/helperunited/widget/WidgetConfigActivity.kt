package com.chronie.helperunited.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.chronie.helperunited.base.SupportedGame
import com.chronie.helperunited.data.AccountRepository
import com.chronie.helperunited.model.HoYoAccount
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Configuration activity for widget setup.
 * Allows user to select which game and account to display in the widget.
 * Launched when widget is first placed or user taps configure.
 */
@AndroidEntryPoint
class WidgetConfigActivity : ComponentActivity() {

    @Inject lateinit var accountRepository: AccountRepository

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set the result to CANCELED in case the user backs out
        setResult(Activity.RESULT_CANCELED)

        // Get the widget ID from the intent
        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID,
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        setContent {
            MaterialTheme {
                WidgetConfigScreen(
                    accountRepository = accountRepository,
                    appWidgetId = appWidgetId,
                    onConfirm = { game, account ->
                        confirmWidget(game, account)
                    },
                    onCancel = {
                        setResult(Activity.RESULT_CANCELED)
                        finish()
                    },
                )
            }
        }
    }

    private fun confirmWidget(game: SupportedGame, account: HoYoAccount) {
        val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main)

        // Determine which widget type was requested
        val widgetClassName = intent?.extras?.getString("widget_type")
        val widgetClass: Class<*> = when (widgetClassName) {
            "small" -> SmallDailyNoteWidget::class.java
            "large" -> LargeDailyNoteWidget::class.java
            else -> MediumDailyNoteWidget::class.java
        }

        scope.launch {
            try {
                val manager = GlanceAppWidgetManager(this@WidgetConfigActivity)
                val glanceIds = manager.getGlanceIds(widgetClass)
                // Find the glanceId matching this appWidgetId
                val glanceId = glanceIds.find { id ->
                    manager.getAppWidgetId(id) == appWidgetId
                } ?: return@launch

                // Save config to Glance state
                updateAppWidgetState(
                    this@WidgetConfigActivity,
                    PreferencesGlanceStateDefinition,
                    glanceId,
                ) { prefs ->
                    prefs.toMutablePreferences().apply {
                        this[WidgetPrefs.KEY_WIDGET_GAME] = game.rawValue
                        this[WidgetPrefs.KEY_WIDGET_ACCOUNT_UUID] = account.uuid
                    }
                }

                // Trigger initial data fetch
                DailyNoteWidgetWorker.enqueueFetch(this@WidgetConfigActivity, glanceId)

                // Mark as configured
                WidgetPrefs.setConfigured(this@WidgetConfigActivity, appWidgetId, true)

                // Update the widget
                val widget = when (widgetClass) {
                    SmallDailyNoteWidget::class.java -> SmallDailyNoteWidget()
                    LargeDailyNoteWidget::class.java -> LargeDailyNoteWidget()
                    else -> MediumDailyNoteWidget()
                }
                widget.update(this@WidgetConfigActivity, glanceId)

                // Return success
                val resultValue = Intent().apply {
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                }
                setResult(Activity.RESULT_OK, resultValue)
                finish()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

// ========== Config Screen Composable ==========

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WidgetConfigScreen(
    accountRepository: AccountRepository,
    appWidgetId: Int,
    onConfirm: (SupportedGame, HoYoAccount) -> Unit,
    onCancel: () -> Unit,
) {
    var selectedGame by remember { mutableStateOf(SupportedGame.GENSHIN_IMPACT) }
    var selectedAccount by remember { mutableStateOf<HoYoAccount?>(null) }
    var allAccounts by remember { mutableStateOf<List<HoYoAccount>>(emptyList()) }
    var filteredAccounts by remember { mutableStateOf<List<HoYoAccount>>(emptyList()) }
    val scope = rememberCoroutineScope()

    // Load accounts
    LaunchedEffect(Unit) {
        allAccounts = accountRepository.getAllAccounts()
        filteredAccounts = allAccounts.filter { it.game == selectedGame }
        if (selectedAccount == null && filteredAccounts.isNotEmpty()) {
            selectedAccount = filteredAccounts.first()
        }
    }

    // Filter accounts when game changes
    LaunchedEffect(selectedGame) {
        filteredAccounts = allAccounts.filter { it.game == selectedGame }
        if (selectedAccount != null && selectedAccount!!.game != selectedGame) {
            selectedAccount = filteredAccounts.firstOrNull()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("配置小组件") },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
        ) {
            // Game selection
            Text(
                text = "选择游戏",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SupportedGame.entries.forEach { game ->
                    val isSelected = game == selectedGame
                    OutlinedButton(
                        onClick = { selectedGame = game },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(
                            text = when (game) {
                                SupportedGame.GENSHIN_IMPACT -> "原神"
                                SupportedGame.STAR_RAIL -> "星铁"
                                SupportedGame.ZENLESS_ZONE -> "绝区零"
                                else -> game.displayName
                            },
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Account selection
            Text(
                text = "选择账号",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (filteredAccounts.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    ),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "没有找到该游戏的账号",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "请先在「账号管理」中添加账号",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(filteredAccounts) { account ->
                        val isSelected = account.uuid == selectedAccount?.uuid
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedAccount = account },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected)
                                    MaterialTheme.colorScheme.primaryContainer
                                else
                                    MaterialTheme.colorScheme.surface,
                            ),
                            border = if (isSelected)
                                CardDefaults.outlinedCardBorder()
                            else null,
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                            ) {
                                Text(
                                    text = account.name.ifEmpty { "UID: ${account.uid}" },
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    text = "UID: ${account.displayUid}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    text = if (account.region.isGlobal) "国际服" else "国服",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("取消")
                }
                Button(
                    onClick = {
                        selectedAccount?.let { onConfirm(selectedGame, it) }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = selectedAccount != null,
                ) {
                    Text("确认")
                }
            }
        }
    }
}
