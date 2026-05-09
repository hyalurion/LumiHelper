package com.chronie.helperunited.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.hilt.work.HiltWorker
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.CoroutineWorker
import com.chronie.helperunited.base.SupportedGame
import com.chronie.helperunited.data.AccountRepository
import com.chronie.helperunited.data.DailyNoteRepository
import com.chronie.helperunited.model.DailyNoteGI
import com.chronie.helperunited.model.DailyNoteHSR
import com.chronie.helperunited.model.DailyNoteZZZ
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Worker that fetches daily note data for a specific widget and updates its Glance state.
 * Triggered on widget tap-to-refresh or periodic updates.
 */
@HiltWorker
class DailyNoteWidgetWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val accountRepository: AccountRepository,
    private val dailyNoteRepository: DailyNoteRepository,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val glanceIdStr = inputData.getString(KEY_WORKER_WIDGET_ID) ?: return Result.failure()

        // Get all widget IDs across all widget types and find matching one
        val manager = GlanceAppWidgetManager(applicationContext)
        val allGlanceIds = buildList {
            addAll(manager.getGlanceIds(DailyNoteWidget::class.java))
            addAll(manager.getGlanceIds(SmallDailyNoteWidget::class.java))
            addAll(manager.getGlanceIds(MediumDailyNoteWidget::class.java))
            addAll(manager.getGlanceIds(LargeDailyNoteWidget::class.java))
        }

        val glanceId = allGlanceIds.find { it.toString() == glanceIdStr } ?: return Result.failure()

        // Read current widget config from Glance state
        val state = androidx.glance.appwidget.state.getAppWidgetState(
            applicationContext,
            PreferencesGlanceStateDefinition,
            glanceId,
        )

        val gameRawValue = state[WidgetPrefs.KEY_WIDGET_GAME] ?: return Result.failure()
        val accountUuid = state[WidgetPrefs.KEY_WIDGET_ACCOUNT_UUID] ?: return Result.failure()

        val game = SupportedGame.fromRawValue(gameRawValue) ?: return Result.failure()
        val account = accountRepository.getAccount(accountUuid) ?: return Result.failure()

        return try {
            when (game) {
                SupportedGame.GENSHIN_IMPACT -> fetchAndUpdateGI(account)
                SupportedGame.STAR_RAIL -> fetchAndUpdateHSR(account)
                SupportedGame.ZENLESS_ZONE -> fetchAndUpdateZZZ(account)
                else -> throw IllegalArgumentException("Unsupported game: ${game.rawValue}")
            }
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }

    private suspend fun fetchAndUpdateGI(
        account: com.chronie.helperunited.model.HoYoAccount,
    ) {
        val result = dailyNoteRepository.fetchDailyNoteGI(account)
        result.onSuccess { note ->
            val manager = GlanceAppWidgetManager(applicationContext)
            val glanceIds = buildList {
                addAll(manager.getGlanceIds(DailyNoteWidget::class.java))
                addAll(manager.getGlanceIds(SmallDailyNoteWidget::class.java))
                addAll(manager.getGlanceIds(MediumDailyNoteWidget::class.java))
                addAll(manager.getGlanceIds(LargeDailyNoteWidget::class.java))
            }
            for (glanceId in glanceIds) {
                updateStateGI(glanceId, note, account.uid)
                refreshWidget(glanceId)
            }
        }
    }

    private suspend fun fetchAndUpdateHSR(
        account: com.chronie.helperunited.model.HoYoAccount,
    ) {
        val result = dailyNoteRepository.fetchDailyNoteHSR(account)
        result.onSuccess { note ->
            val manager = GlanceAppWidgetManager(applicationContext)
            val glanceIds = buildList {
                addAll(manager.getGlanceIds(DailyNoteWidget::class.java))
                addAll(manager.getGlanceIds(SmallDailyNoteWidget::class.java))
                addAll(manager.getGlanceIds(MediumDailyNoteWidget::class.java))
                addAll(manager.getGlanceIds(LargeDailyNoteWidget::class.java))
            }
            for (glanceId in glanceIds) {
                updateStateHSR(glanceId, note, account.uid)
                refreshWidget(glanceId)
            }
        }
    }

    private suspend fun fetchAndUpdateZZZ(
        account: com.chronie.helperunited.model.HoYoAccount,
    ) {
        val result = dailyNoteRepository.fetchDailyNoteZZZ(account)
        result.onSuccess { note ->
            val manager = GlanceAppWidgetManager(applicationContext)
            val glanceIds = buildList {
                addAll(manager.getGlanceIds(DailyNoteWidget::class.java))
                addAll(manager.getGlanceIds(SmallDailyNoteWidget::class.java))
                addAll(manager.getGlanceIds(MediumDailyNoteWidget::class.java))
                addAll(manager.getGlanceIds(LargeDailyNoteWidget::class.java))
            }
            for (glanceId in glanceIds) {
                updateStateZZZ(glanceId, note, account.uid)
                refreshWidget(glanceId)
            }
        }
    }

    private suspend fun refreshWidget(glanceId: GlanceId) {
        // Trigger re-render for whichever widget type this glanceId belongs to
        // We try all types; the wrong ones will just no-op
        DailyNoteWidget().update(applicationContext, glanceId)
        SmallDailyNoteWidget().update(applicationContext, glanceId)
        MediumDailyNoteWidget().update(applicationContext, glanceId)
        LargeDailyNoteWidget().update(applicationContext, glanceId)
    }

    private suspend fun updateStateGI(
        glanceId: GlanceId,
        note: DailyNoteGI,
        uid: String,
    ) {
        updateAppWidgetState(applicationContext, PreferencesGlanceStateDefinition, glanceId) { prefs ->
            prefs.toMutablePreferences().apply {
                this[DailyNoteWidget.KEY_UID] = uid
                this[DailyNoteWidget.KEY_CURRENT] = note.currentResin
                this[DailyNoteWidget.KEY_MAX] = note.maxResin
                this[DailyNoteWidget.KEY_LABEL] = "原粹树脂"
                this[DailyNoteWidget.KEY_UPDATED_AT] = System.currentTimeMillis()
                this[KEY_COMMISSIONS_DONE] = note.finishedTaskNum
                this[KEY_COMMISSIONS_TOTAL] = note.totalTaskNum
                this[KEY_EXPEDITIONS_DONE] = note.expeditions.count { it.isFinished }
                this[KEY_EXPEDITIONS_TOTAL] = note.currentExpeditionNum
                this[KEY_REALM_CURRENCY_CURRENT] = note.currentHomeCoin
                this[KEY_REALM_CURRENCY_MAX] = note.maxHomeCoin
                this[KEY_WEEKLY_BOSS_REMAINING] = note.remainResinDiscountNum
                this[KEY_WEEKLY_BOSS_LIMIT] = note.resinDiscountNumLimit
                this[KEY_ERROR_MSG] = ""
            }
        }
    }

    private suspend fun updateStateHSR(
        glanceId: GlanceId,
        note: DailyNoteHSR,
        uid: String,
    ) {
        updateAppWidgetState(applicationContext, PreferencesGlanceStateDefinition, glanceId) { prefs ->
            prefs.toMutablePreferences().apply {
                this[DailyNoteWidget.KEY_UID] = uid
                this[DailyNoteWidget.KEY_CURRENT] = note.currentStamina
                this[DailyNoteWidget.KEY_MAX] = note.maxStamina
                this[DailyNoteWidget.KEY_LABEL] = "开拓力"
                this[DailyNoteWidget.KEY_UPDATED_AT] = System.currentTimeMillis()
                this[KEY_EXPEDITIONS_DONE] = note.expeditions.count { it.isFinished }
                this[KEY_EXPEDITIONS_TOTAL] = note.acceptedExpeditionNum
                this[KEY_RESERVE_STAMINA] = note.currentReserveStamina
                this[KEY_SIM_UNIVERSE_CURRENT] = note.currentTrainScore
                this[KEY_SIM_UNIVERSE_MAX] = note.maxTrainScore
                this[KEY_ERROR_MSG] = ""
            }
        }
    }

    private suspend fun updateStateZZZ(
        glanceId: GlanceId,
        note: DailyNoteZZZ,
        uid: String,
    ) {
        updateAppWidgetState(applicationContext, PreferencesGlanceStateDefinition, glanceId) { prefs ->
            prefs.toMutablePreferences().apply {
                this[DailyNoteWidget.KEY_UID] = uid
                this[DailyNoteWidget.KEY_CURRENT] = note.energy.progress
                this[DailyNoteWidget.KEY_MAX] = note.energy.maxProgress
                this[DailyNoteWidget.KEY_LABEL] = "电量"
                this[DailyNoteWidget.KEY_UPDATED_AT] = System.currentTimeMillis()
                this[KEY_VITALITY_CURRENT] = note.vitality.current
                this[KEY_VITALITY_MAX] = note.vitality.max
                this[KEY_BOUNTY_CURRENT] = note.bountyCommission.num
                this[KEY_BOUNTY_MAX] = note.bountyCommission.total
                this[KEY_SURVEY_CURRENT] = note.surveyPoints.num
                this[KEY_SURVEY_MAX] = note.surveyPoints.total
                this[KEY_ERROR_MSG] = ""
            }
        }
    }

    companion object {
        private const val KEY_WORKER_WIDGET_ID = "widget_id"

        val KEY_COMMISSIONS_DONE = intPreferencesKey("commissions_done")
        val KEY_COMMISSIONS_TOTAL = intPreferencesKey("commissions_total")
        val KEY_EXPEDITIONS_DONE = intPreferencesKey("expeditions_done")
        val KEY_EXPEDITIONS_TOTAL = intPreferencesKey("expeditions_total")
        val KEY_REALM_CURRENCY_CURRENT = intPreferencesKey("realm_currency_current")
        val KEY_REALM_CURRENCY_MAX = intPreferencesKey("realm_currency_max")
        val KEY_WEEKLY_BOSS_REMAINING = intPreferencesKey("weekly_boss_remaining")
        val KEY_WEEKLY_BOSS_LIMIT = intPreferencesKey("weekly_boss_limit")
        val KEY_RESERVE_STAMINA = intPreferencesKey("reserve_stamina")
        val KEY_SIM_UNIVERSE_CURRENT = intPreferencesKey("sim_universe_current")
        val KEY_SIM_UNIVERSE_MAX = intPreferencesKey("sim_universe_max")
        val KEY_VITALITY_CURRENT = intPreferencesKey("vitality_current")
        val KEY_VITALITY_MAX = intPreferencesKey("vitality_max")
        val KEY_BOUNTY_CURRENT = intPreferencesKey("bounty_current")
        val KEY_BOUNTY_MAX = intPreferencesKey("bounty_max")
        val KEY_SURVEY_CURRENT = intPreferencesKey("survey_current")
        val KEY_SURVEY_MAX = intPreferencesKey("survey_max")
        val KEY_ERROR_MSG = stringPreferencesKey("error_msg")

        fun enqueueFetch(context: Context, glanceId: GlanceId) {
            val data = Data.Builder()
                .putString(KEY_WORKER_WIDGET_ID, glanceId.toString())
                .build()

            val request = OneTimeWorkRequestBuilder<DailyNoteWidgetWorker>()
                .setInputData(data)
                .addTag("widget_refresh")
                .build()

            WorkManager.getInstance(context).enqueue(request)
        }
    }
}
