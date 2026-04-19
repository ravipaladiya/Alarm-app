package com.alarmapp.core.domain.repository

import com.alarmapp.core.domain.model.SleepSession
import kotlinx.coroutines.flow.Flow

interface SleepRepository {
    fun observeRecent(limit: Int = 30): Flow<List<SleepSession>>
    suspend fun startSession(bedtimeEpochMs: Long): Long
    suspend fun endSession(id: Long, wakeEpochMs: Long)
    suspend fun rateSession(id: Long, quality: com.alarmapp.core.domain.model.SleepQuality)
}
