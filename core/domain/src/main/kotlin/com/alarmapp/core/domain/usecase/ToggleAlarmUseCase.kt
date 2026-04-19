package com.alarmapp.core.domain.usecase

import com.alarmapp.core.domain.repository.AlarmRepository
import com.alarmapp.core.domain.scheduler.AlarmScheduler
import javax.inject.Inject

class ToggleAlarmUseCase @Inject constructor(
    private val repository: AlarmRepository,
    private val scheduler: AlarmScheduler,
) {
    suspend operator fun invoke(id: Long, enabled: Boolean) {
        repository.setEnabled(id, enabled)
        val alarm = repository.getById(id) ?: return
        if (enabled) scheduler.schedule(alarm) else scheduler.cancel(id)
    }
}
