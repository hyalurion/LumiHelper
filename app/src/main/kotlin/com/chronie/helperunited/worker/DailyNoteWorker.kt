package com.chronie.helperunited.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.chronie.helperunited.base.SupportedGame
import com.chronie.helperunited.data.AccountRepository
import com.chronie.helperunited.data.DailyNoteRepository
import com.chronie.helperunited.model.HoYoAccount
import com.chronie.helperunited.notification.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Background worker that periodically fetches daily notes for all saved accounts
 * and sends notifications when resin/stamina/energy is about to overflow.
 *
 * Threshold logic: notification fires when current value >= 80% of max,
 * or when remaining capacity <= 20 (whichever triggers first).
 */
@HiltWorker
class DailyNoteWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val accountRepository: AccountRepository,
    private val dailyNoteRepository: DailyNoteRepository,
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME = "daily_note_periodic_refresh"
    }

    override suspend fun doWork(): Result {
        // Ensure notification channel exists
        NotificationHelper.createNotificationChannel(applicationContext)

        val accounts = accountRepository.getAllAccounts()
        if (accounts.isEmpty()) return Result.success()

        // Filter accounts that allow notifications
        val notifyAccounts = accounts.filter { it.allowNotification }

        // Get configurable threshold
        val thresholdPercent = WorkScheduler.getThresholdPercent(applicationContext)

        for (account in notifyAccounts) {
            try {
                checkAccount(account, thresholdPercent)
            } catch (_: Exception) {
                // Continue with other accounts even if one fails
            }
        }

        return Result.success()
    }

    private suspend fun checkAccount(account: HoYoAccount, thresholdPercent: Int) {
        when (account.game) {
            SupportedGame.GENSHIN_IMPACT -> checkGI(account, thresholdPercent)
            SupportedGame.STAR_RAIL -> checkHSR(account, thresholdPercent)
            SupportedGame.ZENLESS_ZONE -> checkZZZ(account, thresholdPercent)
        }
    }

    private suspend fun checkGI(account: HoYoAccount, thresholdPercent: Int) {
        val result = dailyNoteRepository.fetchDailyNoteGI(account)
        result.onSuccess { note ->
            val threshold = (note.maxResin * thresholdPercent / 100).coerceAtMost(note.maxResin - 1)
            if (note.currentResin >= threshold) {
                NotificationHelper.sendResinNotification(
                    applicationContext,
                    note.currentResin,
                    note.maxResin,
                )
            }
        }
    }

    private suspend fun checkHSR(account: HoYoAccount, thresholdPercent: Int) {
        val result = dailyNoteRepository.fetchDailyNoteHSR(account)
        result.onSuccess { note ->
            val threshold = (note.maxStamina * thresholdPercent / 100).coerceAtMost(note.maxStamina - 1)
            if (note.currentStamina >= threshold) {
                NotificationHelper.sendStaminaNotification(
                    applicationContext,
                    note.currentStamina,
                    note.maxStamina,
                )
            }
        }
    }

    private suspend fun checkZZZ(account: HoYoAccount, thresholdPercent: Int) {
        val result = dailyNoteRepository.fetchDailyNoteZZZ(account)
        result.onSuccess { note ->
            val maxEnergy = note.energy.maxProgress
            val currentEnergy = note.energy.progress
            val threshold = (maxEnergy * thresholdPercent / 100).coerceAtMost(maxEnergy - 1)
            if (currentEnergy >= threshold) {
                NotificationHelper.sendEnergyNotification(
                    applicationContext,
                    currentEnergy,
                    maxEnergy,
                )
            }
        }
    }
}
