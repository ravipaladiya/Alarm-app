package com.alarmapp.core.domain.scheduler

import com.alarmapp.core.domain.model.Alarm

/**
 * Platform-agnostic contract for scheduling alarms.
 *
 * Implementations on Android MUST use `AlarmManager.setAlarmClock(...)` — the only
 * API exempt from Doze/App Standby on API 26+. Never use `setExact` or WorkManager
 * for alarm triggers.
 */
interface AlarmScheduler {
    /** Schedule (or reschedule) the next occurrence of [alarm]. */
    fun schedule(alarm: Alarm)

    /** Cancel any pending trigger for [alarmId]. Safe to call even when nothing is pending. */
    fun cancel(alarmId: Long)

    /** Reschedule every enabled alarm; called from BootReceiver and TimeChangedReceiver. */
    fun rescheduleAll(alarms: List<Alarm>)
}
