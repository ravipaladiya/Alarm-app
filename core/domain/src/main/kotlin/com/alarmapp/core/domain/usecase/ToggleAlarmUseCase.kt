package com.alarmapp.core.domain.usecase

import com.alarmapp.core.domain.repository.AlarmRepository
import com.alarmapp.core.domain.scheduler.AlarmScheduler
import javax.inject.Inject

class ToggleAlarmUseCase @Inject constructor(
    private val repository: AlarmRepository,
    private val scheduler: AlarmScheduler,
) {
    suspend operator fun invoke(id: Long, enabled: Boolean) {
        val existing = repository.getById(id) ?: return
        if (enabled) {
            // Upsert recomputes nextTriggerEpochMs against the current clock so
            // re-enabling a fired one-shot alarm doesn't schedule a past time.
            repository.upsert(existing.copy(enabled = true))
            repository.getById(id)?.let(scheduler::schedule)
        } else {
            repository.setEnabled(id, false)
            scheduler.cancel(id)
        }
    }
}
