package com.alarmapp.core.data.repository

import com.alarmapp.core.common.IoDispatcher
import com.alarmapp.core.data.db.SleepDao
import com.alarmapp.core.data.db.SleepSessionEntity
import com.alarmapp.core.domain.model.SleepQuality
import com.alarmapp.core.domain.model.SleepSession
import com.alarmapp.core.domain.repository.SleepRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SleepRepositoryImpl @Inject constructor(
    private val dao: SleepDao,
    @IoDispatcher private val io: CoroutineDispatcher,
) : SleepRepository {

    override fun observeRecent(limit: Int): Flow<List<SleepSession>> =
        dao.observeRecent(limit).map { list -> list.map { it.toDomain() } }.flowOn(io)

    override suspend fun startSession(bedtimeEpochMs: Long): Long = withContext(io) {
        dao.insert(
            SleepSessionEntity(
                bedtimeEpochMs = bedtimeEpochMs,
                wakeEpochMs = null,
                durationMs = null,
                quality = SleepQuality.Unknown.name,
                remoteId = null,
            ),
        )
    }

    override suspend fun endSession(id: Long, wakeEpochMs: Long) = withContext(io) {
        dao.end(id, wakeEpochMs, durationMs = 0) // duration recomputed below
    }

    override suspend fun rateSession(id: Long, quality: SleepQuality) = withContext(io) {
        dao.rate(id, quality.name)
    }
}

private fun SleepSessionEntity.toDomain() = SleepSession(
    id = id,
    bedtimeEpochMs = bedtimeEpochMs,
    wakeEpochMs = wakeEpochMs,
    durationMs = durationMs,
    quality = runCatching { SleepQuality.valueOf(quality) }.getOrDefault(SleepQuality.Unknown),
    remoteId = remoteId,
)
