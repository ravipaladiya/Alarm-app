package com.alarmapp.core.data.repository

import com.alarmapp.core.common.IoDispatcher
import com.alarmapp.core.common.time.Clock
import com.alarmapp.core.common.time.nextTriggerEpochMillis
import com.alarmapp.core.data.db.AlarmDao
import com.alarmapp.core.data.mapper.toDomain
import com.alarmapp.core.data.mapper.toEntity
import com.alarmapp.core.domain.model.Alarm
import com.alarmapp.core.domain.repository.AlarmRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmRepositoryImpl @Inject constructor(
    private val dao: AlarmDao,
    private val clock: Clock,
    @IoDispatcher private val io: CoroutineDispatcher,
) : AlarmRepository {

    override fun observeAll(): Flow<List<Alarm>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }.flowOn(io)

    override suspend fun getById(id: Long): Alarm? = withContext(io) {
        dao.getById(id)?.toDomain()
    }

    override suspend fun upsert(alarm: Alarm): Long = withContext(io) {
        val trigger = nextTriggerEpochMillis(
            time = LocalTime.of(alarm.hour, alarm.minute),
            repeatDaysMask = alarm.repeatDaysMask,
            clock = clock,
        )
        val prepared = alarm.copy(
            nextTriggerEpochMs = trigger,
            updatedAtEpochMs = clock.nowMillis(),
        )
        val id = dao.insert(prepared.toEntity())
        if (alarm.id == 0L) id else alarm.id
    }

    override suspend fun setEnabled(id: Long, enabled: Boolean) = withContext(io) {
        dao.setEnabled(id, enabled, clock.nowMillis())
    }

    override suspend fun delete(id: Long) = withContext(io) {
        dao.deleteById(id)
    }

    override suspend fun allEnabled(): List<Alarm> = withContext(io) {
        dao.allEnabled().map { it.toDomain() }
    }
}
