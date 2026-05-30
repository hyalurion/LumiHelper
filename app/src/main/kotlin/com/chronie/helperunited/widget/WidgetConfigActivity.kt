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
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType

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
            MiuixTheme {
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
        val widgetClass: Class<out GlanceAppWidget> = when (widgetClassName) {
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
                title = "配置小组件",
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
        ) {
            // Game selection
            Text(
                text = "选择游戏",
                style = MiuixTheme.textStyles.title2,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SupportedGame.entries.forEach { game ->
                    val isSelected = game == selectedGame
                    Button(
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
                            color = if (isSelected) MiuixTheme.colorScheme.primary
                            else MiuixTheme.colorScheme.onSurface,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Account selection
            Text(
                text = "选择账号",
                style = MiuixTheme.textStyles.title2,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (filteredAccounts.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    pressFeedbackType = PressFeedbackType.Sink,
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "没有找到该游戏的账号",
                            style = MiuixTheme.textStyles.main,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "请先在「账号管理」中添加账号",
                            style = MiuixTheme.textStyles.main,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
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
                            pressFeedbackType = PressFeedbackType.Sink,
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                            ) {
                                Text(
                                    text = account.name.ifEmpty { "UID: ${account.uid}" },
                                    style = MiuixTheme.textStyles.main,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    text = "UID: ${account.displayUid}",
                                    style = MiuixTheme.textStyles.main,
                                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                )
                                Text(
                                    text = if (account.region.isGlobal) "国际服" else "国服",
                                    style = MiuixTheme.textStyles.main,
                                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
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
                Button(
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
