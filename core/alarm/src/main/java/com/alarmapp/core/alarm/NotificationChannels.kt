package com.alarmapp.core.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes

object NotificationChannels {
    const val RINGING_ID = "alarm_ringing"
    const val REMINDERS_ID = "alarm_reminders"
    const val BEDTIME_ID = "bedtime"

    fun ensureCreated(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val ringing = NotificationChannel(
            RINGING_ID,
            "Alarm ringing",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Active alarm that is currently ringing"
            setBypassDnd(true)
            // Sound is handled by RingtonePlayer (not the channel) so we can fade and
            // honor per-alarm selection; silence the channel itself.
            setSound(null, null)
            enableVibration(false)
            setShowBadge(false)
            lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
        }
        val reminders = NotificationChannel(
            REMINDERS_ID,
            "Reminders",
            NotificationManager.IMPORTANCE_DEFAULT,
        )
        val bedtime = NotificationChannel(
            BEDTIME_ID,
            "Bedtime",
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            setSound(
                null,
                AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION).build(),
            )
        }
        manager.createNotificationChannels(listOf(ringing, reminders, bedtime))
    }
}
