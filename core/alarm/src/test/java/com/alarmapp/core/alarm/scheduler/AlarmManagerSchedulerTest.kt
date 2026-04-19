package com.alarmapp.core.alarm.scheduler

import android.app.AlarmManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.alarmapp.core.domain.model.Alarm
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class AlarmManagerSchedulerTest {

    private lateinit var context: Context
    private lateinit var alarmManager: AlarmManager
    private lateinit var scheduler: AlarmManagerScheduler

    @Before fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        scheduler = AlarmManagerScheduler(context)
    }

    @Test fun `schedule arms setAlarmClock with the alarm's trigger time`() {
        val alarm = Alarm(id = 7, hour = 7, minute = 30, nextTriggerEpochMs = 1_700_000_000_000L)

        scheduler.schedule(alarm)

        val scheduled = shadowOf(alarmManager).nextScheduledAlarm
        assertNotNull("Expected an alarm to be scheduled", scheduled)
        assertEquals(1_700_000_000_000L, scheduled.triggerAtTime)
    }

    @Test fun `schedule with non-positive trigger does nothing`() {
        val alarm = Alarm(id = 8, hour = 7, minute = 30, nextTriggerEpochMs = 0L)

        scheduler.schedule(alarm)

        assertNull(shadowOf(alarmManager).nextScheduledAlarm)
    }

    @Test fun `cancel removes a previously scheduled alarm`() {
        val alarm = Alarm(id = 9, hour = 7, minute = 30, nextTriggerEpochMs = 1_700_000_000_000L)
        scheduler.schedule(alarm)
        assertNotNull(shadowOf(alarmManager).nextScheduledAlarm)

        scheduler.cancel(alarm.id)

        assertNull(shadowOf(alarmManager).nextScheduledAlarm)
    }

    @Test fun `rescheduleAll arms every alarm in the list`() {
        val alarms = listOf(
            Alarm(id = 1, hour = 6, minute = 0, nextTriggerEpochMs = 1_700_000_000_000L),
            Alarm(id = 2, hour = 7, minute = 0, nextTriggerEpochMs = 1_700_000_600_000L),
            Alarm(id = 3, hour = 8, minute = 0, nextTriggerEpochMs = 1_700_001_200_000L),
        )

        scheduler.rescheduleAll(alarms)

        val scheduled = shadowOf(alarmManager).scheduledAlarms
        assertEquals(alarms.size, scheduled.size)
    }
}
