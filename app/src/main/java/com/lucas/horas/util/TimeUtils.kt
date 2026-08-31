package com.lucas.horas.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object TimeUtils {

    private fun timeFormat() = SimpleDateFormat("HH:mm", Locale.getDefault())
    private fun dateFormat() = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private fun dayLabelFormat() = SimpleDateFormat("EEEE, dd/MM", Locale.getDefault())

    fun formatTime(millis: Long): String = timeFormat().format(millis)

    fun formatDate(millis: Long): String = dateFormat().format(millis)

    fun formatDayLabel(millis: Long): String =
        dayLabelFormat().format(millis).replaceFirstChar { it.uppercase() }

    fun startOfDay(millis: Long): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = millis
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun endOfDay(millis: Long): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = startOfDay(millis)
        cal.add(Calendar.DAY_OF_MONTH, 1)
        return cal.timeInMillis
    }

    fun startOfToday(): Long = startOfDay(System.currentTimeMillis())

    /** Início da semana (segunda-feira) que contém o instante dado. */
    fun startOfWeek(millis: Long): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = startOfDay(millis)
        val diaSemana = cal.get(Calendar.DAY_OF_WEEK) // 1=Domingo ... 7=Sábado
        val diasDesdeSegunda = (diaSemana + 5) % 7 // Segunda=0, Terça=1, ..., Domingo=6
        cal.add(Calendar.DAY_OF_MONTH, -diasDesdeSegunda)
        return cal.timeInMillis
    }

    /** Início do mês que contém o instante dado. */
    fun startOfMonth(millis: Long): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = startOfDay(millis)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        return cal.timeInMillis
    }

    fun formatDuration(millis: Long): String {
        val totalMinutes = millis / 60000
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return "${hours}h ${minutes}min"
    }
}
