package com.lucas.horas.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.lucas.horas.data.WifiPrefs

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        if (!WifiPrefs.isEnabled(context)) return

        ContextCompat.startForegroundService(context, Intent(context, WifiPresenceService::class.java))
    }
}
