package com.lucas.horas.domain

import com.lucas.horas.data.PunchType
import com.lucas.horas.util.TimeUtils

object MessageBuilder {

    fun buildDayMessage(day: DaySummary): String {
        val sb = StringBuilder()
        sb.append("Registo de ${TimeUtils.formatDate(day.dayStartMillis)}\n")

        if (day.punches.isEmpty()) {
            sb.append("Sem registos.")
            return sb.toString()
        }

        for (punch in day.punches) {
            val label = if (punch.type == PunchType.ENTRADA) "Entrada" else "Saída"
            sb.append("$label: ${TimeUtils.formatTime(punch.timestamp)}")
            if (!punch.note.isNullOrBlank()) sb.append(" — ${punch.note}")
            sb.append("\n")
        }

        sb.append("Total: ${TimeUtils.formatDuration(day.totalMillis)}")
        if (day.inProgress) sb.append(" (em curso)")

        return sb.toString()
    }
}
