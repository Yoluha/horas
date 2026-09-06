package com.lucas.horas.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class WifiWatchdogReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        WifiWatchdog.garantirServicoAtivo(context)
    }
}
