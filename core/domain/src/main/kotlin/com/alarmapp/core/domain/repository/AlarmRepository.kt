package com.alarmapp.core.domain.repository

import com.alarmapp.core.domain.model.Alarm
import kotlinx.coroutines.flow.Flow

interface AlarmRepository {
    fun observeAll(): Flow<List<Alarm>>
    suspend fun getById(id: Long): Alarm?
    suspend fun upsert(alarm: Alarm): Long
    suspend fun setEnabled(id: Long, enabled: Boolean)
    suspend fun delete(id: Long)
    suspend fun allEnabled(): List<Alarm>
}
