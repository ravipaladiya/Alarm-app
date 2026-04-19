package com.alarmapp.core.alarm.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.alarmapp.core.common.ApplicationScope
import com.alarmapp.core.domain.repository.AlarmRepository
import com.alarmapp.core.domain.scheduler.AlarmScheduler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Recompute and reschedule enabled alarms when the device time, time zone, or
 * locale changes. Without this, an alarm set for 07:00 local could fire at the
 * wrong absolute time after travel or DST.
 */
@AndroidEntryPoint
class TimeChangedReceiver : BroadcastReceiver() {

    @Inject lateinit var repository: AlarmRepository
    @Inject lateinit var scheduler: AlarmScheduler
    @Inject @ApplicationScope lateinit var appScope: CoroutineScope

    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        appScope.launch {
            try {
                Timber.d("Rescheduling after ${intent.action}")
                val enabled = repository.allEnabled()
                // Upsert each alarm so the repository recomputes nextTriggerEpochMs
                // with the new local clock/zone before rescheduling.
                enabled.forEach { repository.upsert(it) }
                scheduler.rescheduleAll(repository.allEnabled())
            } catch (t: Throwable) {
                Timber.e(t, "Failed to reschedule on time change")
            } finally {
                pending.finish()
            }
        }
    }
}
