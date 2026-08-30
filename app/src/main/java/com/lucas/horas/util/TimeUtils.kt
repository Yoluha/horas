package com.lucas.horas.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object TimeUtils {

    private fun timeFormat() = SimpleDateFormat("HH:mm", Locale.getDefault())
    private fun dateFormat() = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private fun dayLabelFormat() = SimpleDateFormat("EEEE, dd/MM", Locale("pt", "PT"))

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

    fun formatDuration(millis: Long): String {
        val totalMinutes = millis / 60000
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return "${hours}h ${minutes}min"
    }
}
