package com.lucas.horas.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class PunchType { ENTRADA, SAIDA }

@Entity(tableName = "punches")
data class PunchEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val type: PunchType,
    val note: String? = null
)
