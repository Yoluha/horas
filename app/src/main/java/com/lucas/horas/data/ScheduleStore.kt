package com.lucas.horas.data

import android.content.Context
import java.util.Calendar

/**
 * Horário normal por dia da semana, em horas (ex: 8.0 = 8h). Guardado localmente.
 * Índices seguem Calendar.DAY_OF_WEEK (1=Domingo ... 7=Sábado).
 */
object ScheduleStore {
    private const val PREFS = "horas_prefs"
    private val DEFAULT_HOURS = mapOf(
        Calendar.SUNDAY to 0.0,
        Calendar.MONDAY to 8.0,
        Calendar.TUESDAY to 8.0,
        Calendar.WEDNESDAY to 8.0,
        Calendar.THURSDAY to 8.0,
        Calendar.FRIDAY to 8.0,
        Calendar.SATURDAY to 0.0
    )

    private fun key(dayOfWeek: Int) = "horario_dia_$dayOfWeek"

    fun getHoursFor(context: Context, dayOfWeek: Int): Double {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val default = DEFAULT_HOURS[dayOfWeek] ?: 8.0
        return prefs.getFloat(key(dayOfWeek), default.toFloat()).toDouble()
    }

    fun setHoursFor(context: Context, dayOfWeek: Int, hours: Double) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putFloat(key(dayOfWeek), hours.toFloat())
            .apply()
    }

    fun expectedMillisFor(context: Context, dayStartMillis: Long): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = dayStartMillis
        val hours = getHoursFor(context, cal.get(Calendar.DAY_OF_WEEK))
        return (hours * 60 * 60 * 1000).toLong()
    }
}
