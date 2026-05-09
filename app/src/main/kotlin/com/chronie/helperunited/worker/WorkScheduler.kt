package com.chronie.helperunited.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * Schedules and manages the periodic background refresh for daily notes.
 * Supports enabling/disabling from settings and configurable intervals.
 */
object WorkScheduler {

    private const val PREFS_NAME = "work_scheduler_prefs"
    private const val KEY_ENABLED = "background_refresh_enabled"
    private const val KEY_INTERVAL_MINUTES = "refresh_interval_minutes"
    private const val KEY_THRESHOLD_PERCENT = "notification_threshold_percent"

    // Defaults
    private const val DEFAULT_INTERVAL_MINUTES = 30L
    private const val DEFAULT_THRESHOLD_PERCENT = 80

    /**
     * Schedule periodic background work for daily note refresh.
     */
    fun schedule(context: Context, intervalMinutes: Long = getIntervalMinutes(context)) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<DailyNoteWorker>(
            intervalMinutes,
            TimeUnit.MINUTES,
        )
            .setConstraints(constraints)
            .addTag(DailyNoteWorker.WORK_NAME)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            DailyNoteWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest,
        )
    }

    /**
     * Cancel the periodic background work.
     */
    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(DailyNoteWorker.WORK_NAME)
    }

    /**
     * Enable or disable background refresh.
     */
    fun setEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_ENABLED, enabled)
            .apply()

        if (enabled) {
            schedule(context)
        } else {
            cancel(context)
        }
    }

    /**
     * Check if background refresh is enabled.
     */
    fun isEnabled(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_ENABLED, false)
    }

    /**
     * Set the refresh interval in minutes. Reschedules if currently enabled.
     */
    fun setIntervalMinutes(context: Context, minutes: Long) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putLong(KEY_INTERVAL_MINUTES, minutes)
            .apply()

        if (isEnabled(context)) {
            schedule(context, minutes)
        }
    }

    /**
     * Get the current refresh interval in minutes.
     */
    fun getIntervalMinutes(context: Context): Long {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getLong(KEY_INTERVAL_MINUTES, DEFAULT_INTERVAL_MINUTES)
    }

    /**
     * Set the notification threshold percentage (0-100).
     */
    fun setThresholdPercent(context: Context, percent: Int) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_THRESHOLD_PERCENT, percent.coerceIn(50, 100))
            .apply()
    }

    /**
     * Get the notification threshold percentage.
     */
    fun getThresholdPercent(context: Context): Int {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_THRESHOLD_PERCENT, DEFAULT_THRESHOLD_PERCENT)
    }

    /**
     * Initialize on app start - re-schedule if previously enabled.
     */
    fun initialize(context: Context) {
        if (isEnabled(context)) {
            schedule(context)
        }
    }
}
