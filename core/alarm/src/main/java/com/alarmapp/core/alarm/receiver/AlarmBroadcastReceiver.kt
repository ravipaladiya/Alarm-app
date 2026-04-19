package com.alarmapp.core.alarm.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import com.alarmapp.core.alarm.service.AlarmService
import com.alarmapp.core.common.ApplicationScope
import com.alarmapp.core.domain.repository.AlarmRepository
import com.alarmapp.core.domain.scheduler.AlarmScheduler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Fires when AlarmManager delivers a scheduled alarm.
 *
 * Sequence:
 *  1. Acquire a short partial wake lock (released after the service starts).
 *  2. Start the foreground [AlarmService] which owns the actual ringtone/vibration.
 *  3. Reschedule this alarm's next occurrence (for repeat alarms).
 */
@AndroidEntryPoint
class AlarmBroadcastReceiver : BroadcastReceiver() {

    @Inject lateinit var repository: AlarmRepository
    @Inject lateinit var scheduler: AlarmScheduler
    @Inject @ApplicationScope lateinit var appScope: CoroutineScope

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_FIRE) return
        val id = intent.getLongExtra(EXTRA_ALARM_ID, -1L)
        if (id < 0) return

        val wakeLock = acquireWakeLock(context)
        val pending = goAsync()
        appScope.launch {
            try {
                val alarm = repository.getById(id)
                if (alarm == null || !alarm.enabled) {
                    Timber.w("Alarm $id not found or disabled; skipping")
                    return@launch
                }
                AlarmService.start(context, id)
                // Bump next trigger for repeat alarms so the scheduler keeps them armed.
                repository.upsert(alarm)
                if (alarm.repeatDaysMask != 0) {
                    repository.getById(id)?.let(scheduler::schedule)
                }
            } catch (t: Throwable) {
                Timber.e(t, "Failed to handle alarm $id")
            } finally {
                if (wakeLock.isHeld) wakeLock.release()
                pending.finish()
            }
        }
    }

    private fun acquireWakeLock(context: Context): PowerManager.WakeLock {
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "alarmapp:fire").apply {
            setReferenceCounted(false)
            acquire(WAKELOCK_TIMEOUT_MS)
        }
    }

    companion object {
        const val ACTION_FIRE = "com.alarmapp.action.FIRE"
        const val EXTRA_ALARM_ID = "alarm_id"
        private const val WAKELOCK_TIMEOUT_MS = 30_000L
    }
}
