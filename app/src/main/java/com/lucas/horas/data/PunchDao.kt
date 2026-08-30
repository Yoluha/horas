package com.lucas.horas.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PunchDao {

    @Insert
    suspend fun insert(punch: PunchEntity): Long

    @Query("SELECT * FROM punches ORDER BY timestamp ASC")
    fun observeAll(): Flow<List<PunchEntity>>

    @Query("SELECT * FROM punches WHERE timestamp >= :startMillis AND timestamp < :endMillis ORDER BY timestamp ASC")
    suspend fun getBetween(startMillis: Long, endMillis: Long): List<PunchEntity>

    @Query("SELECT * FROM punches ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLast(): PunchEntity?
}
