package com.lucas.horas.domain

import com.lucas.horas.data.PunchEntity

data class DaySummary(
    val dayStartMillis: Long,
    val punches: List<PunchEntity>,
    val totalMillis: Long,
    val inProgress: Boolean
)
