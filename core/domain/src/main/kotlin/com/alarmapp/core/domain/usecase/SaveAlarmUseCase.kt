package com.alarmapp.core.domain.usecase

import com.alarmapp.core.domain.model.Alarm
import com.alarmapp.core.domain.repository.AlarmRepository
import com.alarmapp.core.domain.scheduler.AlarmScheduler
import javax.inject.Inject

class SaveAlarmUseCase @Inject constructor(
    private val repository: AlarmRepository,
    private val scheduler: AlarmScheduler,
) {
    suspend operator fun invoke(alarm: Alarm): Long {
        val id = repository.upsert(alarm)
        val saved = repository.getById(id) ?: return id
        if (saved.enabled) scheduler.schedule(saved) else scheduler.cancel(id)
        return id
    }
}
