package com.lucas.horas.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.lucas.horas.data.WifiPrefs

/** O Android (sobretudo Xiaomi/POCO a poupar bateria) pode matar o WifiPresenceService
 * em segundo plano sem avisar, e a deteção automática fica parada até reabrires a app.
 * Este alarme periódico (a cada 30 min) volta a arrancar o serviço sozinho, mesmo com a
 * app fechada — chamar startForegroundService num serviço já em curso não faz nada. */
object WifiWatchdog {

    private const val REQUEST_CODE = 4321
    private const val INTERVALO_MILLIS = AlarmManager.INTERVAL_HALF_HOUR

    private fun pendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, WifiWatchdogReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun agendar(context: Context) {
        if (!WifiPrefs.isEnabled(context)) {
            cancelar(context)
            return
        }
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setInexactRepeating(
            AlarmManager.RTC,
            System.currentTimeMillis() + INTERVALO_MILLIS,
            INTERVALO_MILLIS,
            pendingIntent(context)
        )
    }

    fun cancelar(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent(context))
    }

    fun garantirServicoAtivo(context: Context) {
        if (WifiPrefs.isEnabled(context)) {
            ContextCompat.startForegroundService(context, Intent(context, WifiPresenceService::class.java))
        }
    }
}
