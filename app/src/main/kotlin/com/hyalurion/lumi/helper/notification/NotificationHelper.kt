package com.hyalurion.lumi.helper.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.hyalurion.lumi.helper.MainActivity
import com.hyalurion.lumi.helper.R

/**
 * Helper for creating and sending daily note notifications.
 * Supports per-game notifications for GI resin, HSR stamina, ZZZ energy.
 */
object NotificationHelper {

    const val CHANNEL_ID = "daily_note_alerts"
    private const val CHANNEL_NAME = "每日便笺提醒"
    private const val CHANNEL_DESC = "树脂/开拓力/电量即将满溢时的通知"

    // Notification IDs per game to avoid collisions
    const val NOTIFICATION_ID_GI = 1001
    const val NOTIFICATION_ID_HSR = 1002
    const val NOTIFICATION_ID_ZZZ = 1003

    /**
     * Creates the notification channel. Must be called before sending notifications.
     */
    fun createNotificationChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = CHANNEL_DESC
            enableVibration(true)
            setShowBadge(true)
        }
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    /**
     * Returns a PendingIntent that opens the app.
     */
    private fun createOpenAppIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    /**
     * Send a Genshin Impact resin notification.
     */
    fun sendResinNotification(context: Context, currentResin: Int, maxResin: Int) {
        if (currentResin < maxResin) return
        sendNotification(
            context = context,
            notificationId = NOTIFICATION_ID_GI,
            title = "⚠️ 树脂即将满溢！",
            body = "当前树脂: $currentResin / $maxResin\n请及时消耗，避免溢出浪费！",
        )
    }

    /**
     * Send a Star Rail stamina notification.
     */
    fun sendStaminaNotification(context: Context, currentStamina: Int, maxStamina: Int) {
        if (currentStamina < maxStamina) return
        sendNotification(
            context = context,
            notificationId = NOTIFICATION_ID_HSR,
            title = "⚠️ 开拓力即将满溢！",
            body = "当前开拓力: $currentStamina / $maxStamina\n请及时消耗，避免溢出浪费！",
        )
    }

    /**
     * Send a Zenless Zone Zero energy notification.
     */
    fun sendEnergyNotification(context: Context, currentEnergy: Int, maxEnergy: Int) {
        if (currentEnergy < maxEnergy) return
        sendNotification(
            context = context,
            notificationId = NOTIFICATION_ID_ZZZ,
            title = "⚠️ 电量即将满溢！",
            body = "当前电量: $currentEnergy / $maxEnergy\n请及时消耗，避免溢出浪费！",
        )
    }

    /**
     * Generic notification sender.
     */
    private fun sendNotification(
        context: Context,
        notificationId: Int,
        title: String,
        body: String,
    ) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(createOpenAppIntent(context))
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify(notificationId, notification)
    }
}
