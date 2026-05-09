package com.chronie.helperunited.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Re-schedules periodic work after device reboot.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            WorkScheduler.initialize(context)
        }
    }
}
