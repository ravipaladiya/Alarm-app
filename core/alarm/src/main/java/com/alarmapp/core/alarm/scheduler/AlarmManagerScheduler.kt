package com.alarmapp.core.alarm.scheduler

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.alarmapp.core.alarm.receiver.AlarmBroadcastReceiver
import com.alarmapp.core.domain.model.Alarm
import com.alarmapp.core.domain.scheduler.AlarmScheduler
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AlarmManager-backed scheduler.
 *
 * Uses `setAlarmClock` — the only scheduling API that is exempt from Doze and App
 * Standby on API 26+. Never replace this with `setExact` or WorkManager for alarm
 * triggers (they are both subject to Doze delays).
 */
@Singleton
class AlarmManagerScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) : AlarmScheduler {

    private val manager: AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    @SuppressLint("MissingPermission", "ScheduleExactAlarm")
    override fun schedule(alarm: Alarm) {
        if (alarm.nextTriggerEpochMs <= 0) {
            Timber.w("Refusing to schedule alarm ${alarm.id}: no trigger time")
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !manager.canScheduleExactAlarms()) {
            Timber.w("Exact alarms not permitted by user; alarm ${alarm.id} will not fire")
            return
        }
        val pi = buildPendingIntent(alarm.id, create = true)
        val showIntent = PendingIntent.getActivity(
            context,
            alarm.id.toInt(),
            Intent().setClassName(context, "com.alarmapp.MainActivity"),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        manager.setAlarmClock(
            AlarmManager.AlarmClockInfo(alarm.nextTriggerEpochMs, showIntent),
            pi,
        )
        Timber.d("Scheduled alarm ${alarm.id} for ${alarm.nextTriggerEpochMs}")
    }

    override fun cancel(alarmId: Long) {
        val pi = buildPendingIntent(alarmId, create = false) ?: return
        manager.cancel(pi)
        pi.cancel()
        Timber.d("Cancelled alarm $alarmId")
    }

    override fun rescheduleAll(alarms: List<Alarm>) {
        alarms.forEach { schedule(it) }
    }

    private fun buildPendingIntent(alarmId: Long, create: Boolean): PendingIntent? {
        val intent = Intent(context, AlarmBroadcastReceiver::class.java).apply {
            action = AlarmBroadcastReceiver.ACTION_FIRE
            putExtra(AlarmBroadcastReceiver.EXTRA_ALARM_ID, alarmId)
        }
        val flags = if (create) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        }
        return PendingIntent.getBroadcast(context, alarmId.toInt(), intent, flags)
    }
}
