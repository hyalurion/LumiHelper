package com.chronie.helperunited.ui.account

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.chronie.helperunited.base.RegionType
import com.chronie.helperunited.base.SupportedGame
import com.chronie.helperunited.model.HoYoAccount
import com.chronie.helperunited.network.FetchedAccount
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SnackbarHost
import top.yukonga.miuix.kmp.basic.SnackbarHostState
import top.yukonga.miuix.kmp.basic.Switch
import top.yukonga.miuix.kmp.basic.TabRow
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Delete
import top.yukonga.miuix.kmp.icon.extended.Edit
import top.yukonga.miuix.kmp.icon.extended.Forward
import top.yukonga.miuix.kmp.overlay.OverlayBottomSheet
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType

// ============================================================
// Main AccountScreen (matches Swift ProfileManagerPageContent)
// ============================================================

@Composable
fun AccountScreen(
    onNavigateToWebView: (() -> Unit)? = null,
    onNavigateToQrCode: (() -> Unit)? = null,
    viewModel: AccountViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showDeleteConfirm by remember { mutableStateOf<HoYoAccount?>(null) }

    LaunchedSnackbarEffect(uiState, snackbarHostState, scope, viewModel)

    Scaffold(
        topBar = {
            TopAppBar(title = "账号管理")
        },
        snackbarHost = { SnackbarHost(state = snackbarHostState) },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            if (uiState.accounts.isEmpty()) {
                EmptyAccountView(onCreateClick = { viewModel.showCreateSheet() })
            } else {
                AccountListContent(
                    accounts = uiState.accounts,
                    isBusy = uiState.isBusy,
                    onCreateClick = { viewModel.showCreateSheet() },
                    onEditClick = { viewModel.showEditSheet(it) },
                    onDeleteClick = { showDeleteConfirm = it },
                )
            }

            if (uiState.isBusy) {
                BusyOverlay()
            }

            CreateProfileSheet(
                show = uiState.showCreateSheet,
                state = uiState,
                onDismiss = { viewModel.dismissCreateSheet() },
                onGameSelected = { viewModel.selectGame(it) },
                onRegionTypeSelected = { viewModel.selectRegionType(it) },
                onImportAllUIDsToggled = { viewModel.toggleImportAllUIDs() },
                onLoginClick = {
                    when (uiState.selectedRegionType) {
                        RegionType.HOYOLAB -> onNavigateToWebView?.invoke()
                        RegionType.MIYOUSHE -> onNavigateToQrCode?.invoke()
                    }
                },
                onAccountSelected = { viewModel.selectFetchedAccount(it) },
                onNameChanged = { viewModel.updateProfileName(it) },
                onSave = { viewModel.saveProfile() },
                onCancel = { viewModel.dismissCreateSheet() },
            )

            EditProfileSheet(
                show = uiState.showEditSheet,
                state = uiState,
                onDismiss = { viewModel.dismissEditSheet() },
                onNameChanged = { viewModel.updateProfileName(it) },
                onSave = { viewModel.saveEditedProfile() },
            )

            if (showDeleteConfirm != null) {
                OverlayDialog(
                    show = true,
                    onDismissRequest = { showDeleteConfirm = null },
                ) {
                    DeleteConfirmContent(
                        account = showDeleteConfirm!!,
                        onConfirm = {
                            viewModel.deleteAccount(showDeleteConfirm!!.uuid)
                            showDeleteConfirm = null
                        },
                        onDismiss = { showDeleteConfirm = null },
                    )
                }
            }
        }
    }
}

@Composable
private fun LaunchedSnackbarEffect(
    uiState: AccountUiState,
    snackbarHostState: SnackbarHostState,
    scope: kotlinx.coroutines.CoroutineScope,
    viewModel: AccountViewModel,
) {
    if (uiState.showSuccessToast) {
        androidx.compose.runtime.LaunchedEffect(uiState.toastMessage) {
            scope.launch {
                snackbarHostState.showSnackbar(uiState.toastMessage)
                viewModel.dismissToast()
            }
        }
    }
    if (uiState.showErrorToast) {
        androidx.compose.runtime.LaunchedEffect(uiState.toastMessage) {
            scope.launch {
                snackbarHostState.showSnackbar(uiState.toastMessage)
                viewModel.dismissToast()
            }
        }
    }
}

// ============================================================
// Empty State (when no accounts exist)
// ============================================================

@Composable
private fun EmptyAccountView(onCreateClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = MiuixIcons.Forward,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "还没有账号",
            style = MiuixTheme.textStyles.title2,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "点击下方按钮添加你的 HoYoLAB 或米游社账号",
            style = MiuixTheme.textStyles.body2,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onCreateClick) {
            Text("添加账号")
        }
    }
}

// ============================================================
// Account List Content (matches Swift ForEach + drawRow)
// ============================================================

@Composable
private fun AccountListContent(
    accounts: List<HoYoAccount>,
    isBusy: Boolean,
    onCreateClick: () -> Unit,
    onEditClick: (HoYoAccount) -> Unit,
    onDeleteClick: (HoYoAccount) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            CreateAccountCard(onClick = onCreateClick, enabled = !isBusy)
        }

        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "账号列表",
                style = MiuixTheme.textStyles.title3,
                fontWeight = FontWeight.Bold,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
        }

        items(accounts, key = { it.uuid }) { account ->
            AccountRow(
                account = account,
                onEdit = { onEditClick(account) },
                onDelete = { onDeleteClick(account) },
                enabled = !isBusy,
            )
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

@Composable
private fun CreateAccountCard(onClick: () -> Unit, enabled: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onClick() },
        pressFeedbackType = PressFeedbackType.Sink,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = MiuixIcons.Forward,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = MiuixTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "创建新账号",
                style = MiuixTheme.textStyles.title2,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = MiuixIcons.Forward,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
        }
    }
}

@Composable
private fun AccountRow(
    account: HoYoAccount,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    enabled: Boolean,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        pressFeedbackType = PressFeedbackType.Sink,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            GameIcon(game = account.game)

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = account.name,
                    style = MiuixTheme.textStyles.title2,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = account.displayUid,
                    style = MiuixTheme.textStyles.main,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
                Text(
                    text = "${account.game.displayName} · ${account.region.displayName}",
                    style = MiuixTheme.textStyles.body2,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }

            IconButton(onClick = onEdit, enabled = enabled) {
                Icon(
                    imageVector = MiuixIcons.Edit,
                    contentDescription = "编辑",
                    modifier = Modifier.size(20.dp),
                    tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
            IconButton(onClick = onDelete, enabled = enabled) {
                Icon(
                    imageVector = MiuixIcons.Delete,
                    contentDescription = "删除",
                    modifier = Modifier.size(20.dp),
                    tint = MiuixTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun GameIcon(game: SupportedGame) {
    val emoji = when (game) {
        SupportedGame.GENSHIN_IMPACT -> "\u26A1"
        SupportedGame.STAR_RAIL -> "\uD83D\uDE82"
        SupportedGame.ZENLESS_ZONE -> "\uD83C\uDFAE"
        else -> "\uD83C\uDFAE"
    }
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MiuixTheme.colorScheme.secondaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = emoji, style = MiuixTheme.textStyles.headline2)
    }
}

@Composable
private fun BusyOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(enabled = false) { },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MiuixTheme.colorScheme.surface.copy(alpha = 0.9f)),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(modifier = Modifier.size(48.dp))
        }
    }
}

// ============================================================
// CreateProfileSheet (matches Swift CreateProfileSheetView)
// ============================================================

@Composable
private fun CreateProfileSheet(
    show: Boolean,
    state: AccountUiState,
    onDismiss: () -> Unit,
    onGameSelected: (SupportedGame) -> Unit,
    onRegionTypeSelected: (RegionType) -> Unit,
    onImportAllUIDsToggled: () -> Unit,
    onLoginClick: () -> Unit,
    onAccountSelected: (FetchedAccount) -> Unit,
    onNameChanged: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
) {
    OverlayBottomSheet(
        show = show,
        title = "创建新账号",
        onDismissRequest = onDismiss,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            when (state.createStep) {
                AddProfileStatus.Pending -> PendingStepContent(
                    selectedGame = state.selectedGame,
                    selectedRegionType = state.selectedRegionType,
                    importAllUIDs = state.importAllUIDs,
                    onGameSelected = onGameSelected,
                    onRegionTypeSelected = onRegionTypeSelected,
                    onImportAllUIDsToggled = onImportAllUIDsToggled,
                    onLoginClick = onLoginClick,
                )
                AddProfileStatus.GotCookie -> GotCookieStepContent(
                    isFetching = state.isFetchingAccounts,
                )
                AddProfileStatus.GotProfile -> GotProfileStepContent(
                    fetchedAccounts = state.fetchedAccounts,
                    selectedAccount = state.selectedFetchedAccount,
                    profileName = state.profileName,
                    selectedRegionType = state.selectedRegionType,
                    onAccountSelected = onAccountSelected,
                    onNameChanged = onNameChanged,
                    onSave = onSave,
                    onCancel = onCancel,
                )
            }

            if (state.error != null) {
                Text(
                    text = state.error,
                    color = MiuixTheme.colorScheme.error,
                    style = MiuixTheme.textStyles.body2,
                )
            }
        }
    }
}

// ============================================================
// Pending Step (matches Swift RequireLoginView in CreateProfileSheetView)
// ============================================================

@Composable
private fun PendingStepContent(
    selectedGame: SupportedGame,
    selectedRegionType: RegionType,
    importAllUIDs: Boolean,
    onGameSelected: (SupportedGame) -> Unit,
    onRegionTypeSelected: (RegionType) -> Unit,
    onImportAllUIDsToggled: () -> Unit,
    onLoginClick: () -> Unit,
) {
    val gameTabs = SupportedGame.entries.map { it.shortName }
    val gameIndex = SupportedGame.entries.indexOf(selectedGame).coerceAtLeast(0)

    Text(
        text = "请选择游戏",
        style = MiuixTheme.textStyles.body2,
        fontWeight = FontWeight.Medium,
    )
    TabRow(
        tabs = gameTabs,
        selectedTabIndex = gameIndex,
        onTabSelected = { index ->
            SupportedGame.entries.getOrNull(index)?.let { onGameSelected(it) }
        },
        modifier = Modifier.fillMaxWidth(),
    )

    val availableRegions = listOf(RegionType.MIYOUSHE, RegionType.HOYOLAB)
    val regionTabs = availableRegions.map { it.displayName }
    var regionIndex by remember(selectedRegionType) {
        mutableIntStateOf(availableRegions.indexOf(selectedRegionType).coerceAtLeast(0))
    }

    Text(
        text = "请选择服务器",
        style = MiuixTheme.textStyles.body2,
        fontWeight = FontWeight.Medium,
    )
    TabRow(
        tabs = regionTabs,
        selectedTabIndex = regionIndex,
        onTabSelected = { index ->
            regionIndex = index
            availableRegions.getOrNull(index)?.let { onRegionTypeSelected(it) }
        },
        modifier = Modifier.fillMaxWidth(),
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "导入所有游戏UID",
                style = MiuixTheme.textStyles.body1,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = "登录后将自动获取该账号下所有游戏的UID",
                style = MiuixTheme.textStyles.body2,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(
            checked = importAllUIDs,
            onCheckedChange = { onImportAllUIDsToggled() },
        )
    }

    val loginLabel = buildLoginLabel(selectedRegionType, selectedGame)
    Button(
        onClick = onLoginClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = loginLabel,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "免责声明",
                style = MiuixTheme.textStyles.title3,
                fontWeight = FontWeight.Bold,
                color = MiuixTheme.colorScheme.error,
            )
            Text(
                text = "使用非官方API获取游戏数据存在一定风险，请自行评估后再使用。开发者不对账号安全承担任何责任。",
                style = MiuixTheme.textStyles.body2,
                color = MiuixTheme.colorScheme.error,
            )
            Text(
                text = "登录说明",
                style = MiuixTheme.textStyles.title3,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "你的登录凭据（Cookie / SToken）将仅存储在本设备中，不会上传至任何服务器。",
                style = MiuixTheme.textStyles.body2,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
            Text(
                text = "安全提醒",
                style = MiuixTheme.textStyles.title3,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "请勿将你的登录凭据分享给任何人。账号凭据若被泄露，可能导致账号安全问题。",
                style = MiuixTheme.textStyles.body2,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
        }
    }
}

private fun buildLoginLabel(regionType: RegionType, game: SupportedGame): String {
    val regionLabel = regionType.displayName
    val gameLabel = game.shortName
    return "点击此处登录 $regionLabel\n（$gameLabel）"
}

// ============================================================
// GotCookie Step (matches Swift WinUI3ProgressRing)
// ============================================================

@Composable
private fun GotCookieStepContent(isFetching: Boolean) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        CircularProgressIndicator(modifier = Modifier.size(56.dp))
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (isFetching) "正在获取游戏账号列表..."
            else "正在等待登录完成...",
            style = MiuixTheme.textStyles.body1,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        )
    }
}

// ============================================================
// GotProfile Step (matches Swift ProfileConfigViewContents)
// ============================================================

@Composable
private fun GotProfileStepContent(
    fetchedAccounts: List<FetchedAccount>,
    selectedAccount: FetchedAccount?,
    profileName: String,
    selectedRegionType: RegionType,
    onAccountSelected: (FetchedAccount) -> Unit,
    onNameChanged: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
) {
    if (fetchedAccounts.size > 1) {
        Text(
            text = "选择账号",
            style = MiuixTheme.textStyles.body2,
            fontWeight = FontWeight.Bold,
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            fetchedAccounts.forEach { account ->
                val isSelected = account == selectedAccount
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onAccountSelected(account) },
                    pressFeedbackType = PressFeedbackType.Sink,
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) MiuixTheme.colorScheme.primary
                                    else MiuixTheme.colorScheme.secondaryContainer,
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = gameBizPrefix(account.gameBiz),
                                style = MiuixTheme.textStyles.body2,
                                color = if (isSelected) MiuixTheme.colorScheme.onPrimary
                                else MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = account.nickname.ifBlank { "旅行者" },
                                style = MiuixTheme.textStyles.body1,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            )
                            Text(
                                text = "UID: ${account.gameUid} · Lv.${account.level}",
                                style = MiuixTheme.textStyles.body2,
                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            )
                        }
                    }
                }
            }
        }
    }

    if (selectedAccount != null) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "UID: ${selectedAccount.gameUid}",
                        style = MiuixTheme.textStyles.body1,
                        fontWeight = FontWeight.Bold,
                    )
                }

                Text(
                    text = "备注名",
                    style = MiuixTheme.textStyles.body2,
                    fontWeight = FontWeight.Bold,
                )
                TextField(
                    value = profileName,
                    onValueChange = onNameChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = "输入备注名（留空则使用昵称）",
                )
            }
        }
    }

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
            onClick = onSave,
            modifier = Modifier.weight(1f),
        ) {
            Text("完成")
        }
    }
}

// ============================================================
// EditProfileSheet (matches Swift EditProfileSheetView + ProfileConfigViewContents)
// ============================================================

@Composable
private fun EditProfileSheet(
    show: Boolean,
    state: AccountUiState,
    onDismiss: () -> Unit,
    onNameChanged: (String) -> Unit,
    onSave: () -> Unit,
) {
    val account = state.editingAccount ?: return

    OverlayBottomSheet(
        show = show,
        title = "编辑账号",
        onDismissRequest = onDismiss,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "UID: ${account.displayUid}",
                            style = MiuixTheme.textStyles.body1,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Text(
                        text = "${account.game.displayName} · ${account.region.displayName}",
                        style = MiuixTheme.textStyles.body2,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    )

                    Text(
                        text = "备注名",
                        style = MiuixTheme.textStyles.body2,
                        fontWeight = FontWeight.Bold,
                    )
                    TextField(
                        value = state.profileName,
                        onValueChange = onNameChanged,
                        modifier = Modifier.fillMaxWidth(),
                        label = "输入备注名",
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("取消")
                }
                Button(
                    onClick = onSave,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("完成")
                }
            }
        }
    }
}

// ============================================================
// Delete Confirm Dialog
// ============================================================

@Composable
private fun DeleteConfirmContent(
    account: HoYoAccount,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "删除账号",
            style = MiuixTheme.textStyles.headline2,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "确定要删除账号「${account.name}」(UID: ${account.displayUid}) 吗？此操作不可撤销。",
            style = MiuixTheme.textStyles.body1,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Button(
                onClick = onDismiss,
                modifier = Modifier.weight(1f),
            ) {
                Text("取消")
            }
            Button(
                onClick = onConfirm,
                modifier = Modifier.weight(1f),
            ) {
                Text("删除")
            }
        }
    }
}

private fun gameBizPrefix(gameBiz: String): String = when {
    gameBiz.contains("hk4e") -> "原"
    gameBiz.contains("hkrpg") -> "铁"
    gameBiz.contains("nap") -> "绝"
    else -> "?"
}