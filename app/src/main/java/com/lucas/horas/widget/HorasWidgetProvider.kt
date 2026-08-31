package com.lucas.horas.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import com.lucas.horas.data.AppDatabase
import com.lucas.horas.data.PunchEntity
import com.lucas.horas.data.PunchType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HorasWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_ENTRADA = "com.lucas.horas.widget.ACTION_ENTRADA"
        const val ACTION_SAIDA = "com.lucas.horas.widget.ACTION_SAIDA"
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        WidgetUpdater.updateAll(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val tipo = when (intent.action) {
            ACTION_ENTRADA -> PunchType.ENTRADA
            ACTION_SAIDA -> PunchType.SAIDA
            else -> return
        }
        registar(context, tipo)
    }

    private fun registar(context: Context, type: PunchType) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dao = AppDatabase.getInstance(context).punchDao()
                dao.insert(PunchEntity(timestamp = System.currentTimeMillis(), type = type, note = null))
                WidgetUpdater.refresh(context)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
