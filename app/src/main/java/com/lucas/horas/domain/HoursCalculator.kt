package com.lucas.horas.domain

import com.lucas.horas.data.PunchEntity
import com.lucas.horas.data.PunchType
import com.lucas.horas.util.TimeUtils

object HoursCalculator {

    /** Agrupa registos por dia (hora local) e calcula o total trabalhado em cada dia. */
    fun groupByDay(allPunches: List<PunchEntity>): List<DaySummary> {
        val today = TimeUtils.startOfToday()
        return allPunches
            .sortedBy { it.timestamp }
            .groupBy { TimeUtils.startOfDay(it.timestamp) }
            .map { (dayStart, punches) -> summarize(dayStart, punches, isToday = dayStart == today) }
            .sortedByDescending { it.dayStartMillis }
    }

    /** Soma o total trabalhado nos dias entre startMillis (incl.) e endMillisExclusive (excl.). */
    fun totalBetween(dias: List<DaySummary>, startMillis: Long, endMillisExclusive: Long): Long =
        dias.filter { it.dayStartMillis in startMillis until endMillisExclusive }
            .sumOf { it.totalMillis }

    fun summarizeDay(dayStartMillis: Long, punches: List<PunchEntity>): DaySummary =
        summarize(dayStartMillis, punches, isToday = dayStartMillis == TimeUtils.startOfToday())

    private fun summarize(dayStartMillis: Long, punches: List<PunchEntity>, isToday: Boolean): DaySummary {
        val sorted = punches.sortedBy { it.timestamp }
        var total = 0L
        var openEntry: Long? = null

        for (punch in sorted) {
            when (punch.type) {
                PunchType.ENTRADA -> openEntry = punch.timestamp
                PunchType.SAIDA -> {
                    val open = openEntry
                    if (open != null) {
                        total += punch.timestamp - open
                        openEntry = null
                    }
                }
            }
        }

        val inProgress = openEntry != null
        if (inProgress && isToday) {
            total += System.currentTimeMillis() - openEntry!!
        }

        return DaySummary(dayStartMillis, sorted, total, inProgress)
    }
}
