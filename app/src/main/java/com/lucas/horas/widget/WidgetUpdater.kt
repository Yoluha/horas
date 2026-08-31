package com.lucas.horas.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.lucas.horas.R
import com.lucas.horas.data.AppDatabase
import com.lucas.horas.domain.HoursCalculator
import com.lucas.horas.util.TimeUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object WidgetUpdater {

    /** Chamado sempre que um registo é criado/editado/apagado, para o(s) widget(s) refletirem o total de hoje. */
    fun updateAll(context: Context) {
        CoroutineScope(Dispatchers.IO).launch { refresh(context) }
    }

    /** Igual a updateAll, mas suspende até terminar — usar quando já se está numa coroutine
     * de curta duração (ex: goAsync do widget) que não deve terminar antes de acabar. */
    suspend fun refresh(context: Context) {
        val manager = AppWidgetManager.getInstance(context)
        val ids = manager.getAppWidgetIds(ComponentName(context, HorasWidgetProvider::class.java))
        if (ids.isEmpty()) return

        val dao = AppDatabase.getInstance(context).punchDao()
        val inicio = TimeUtils.startOfToday()
        val fim = TimeUtils.endOfDay(inicio)
        val punches = dao.getBetween(inicio, fim)
        val resumo = HoursCalculator.summarizeDay(inicio, punches)

        val views = RemoteViews(context.packageName, R.layout.widget_horas)
        val sufixo = if (resumo.inProgress) " ${context.getString(R.string.em_curso)}" else ""
        views.setTextViewText(R.id.widgetTotal, "${context.getString(R.string.titulo_hoje)}: ${TimeUtils.formatDuration(resumo.totalMillis)}$sufixo")

        views.setOnClickPendingIntent(R.id.widgetBtnEntrada, actionPendingIntent(context, HorasWidgetProvider.ACTION_ENTRADA, 1))
        views.setOnClickPendingIntent(R.id.widgetBtnSaida, actionPendingIntent(context, HorasWidgetProvider.ACTION_SAIDA, 2))

        for (id in ids) {
            manager.updateAppWidget(id, views)
        }
    }

    private fun actionPendingIntent(context: Context, action: String, requestCode: Int): PendingIntent {
        val intent = Intent(context, HorasWidgetProvider::class.java).apply { this.action = action }
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
