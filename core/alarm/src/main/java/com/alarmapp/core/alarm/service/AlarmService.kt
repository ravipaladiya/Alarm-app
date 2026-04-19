package com.alarmapp.core.alarm.service

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.alarmapp.core.alarm.NotificationChannels
import com.alarmapp.core.alarm.media.RingtonePlayer
import com.alarmapp.core.alarm.media.VibrationPlayer
import com.alarmapp.core.domain.model.Alarm
import com.alarmapp.core.domain.repository.AlarmRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.time.Duration.Companion.minutes

/**
 * Foreground service that owns the ringing alarm.
 *
 * Responsibilities:
 *  - Promote itself to the foreground with a full-screen-intent notification so the
 *    OS cannot kill it, and so the lock-screen launches RingingActivity automatically.
 *  - Drive [RingtonePlayer] and [VibrationPlayer] for the audio/haptic experience.
 *  - Auto-silence after [Alarm.autoSilenceMinutes] if the user never dismisses.
 */
@AndroidEntryPoint
class AlarmService : LifecycleService() {

    @Inject lateinit var repository: AlarmRepository
    @Inject lateinit var ringtonePlayer: RingtonePlayer
    @Inject lateinit var vibrationPlayer: VibrationPlayer

    private var autoSilenceJob: Job? = null
    private var activeAlarmId: Long = -1

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val id = intent?.getLongExtra(EXTRA_ALARM_ID, -1L) ?: -1L
        when (intent?.action) {
            ACTION_START -> startRinging(id)
            ACTION_SNOOZE -> snooze(id)
            ACTION_DISMISS -> dismiss()
            else -> dismiss()
        }
        return START_NOT_STICKY
    }

    private fun startRinging(id: Long) {
        if (id < 0) return stopSelf()
        activeAlarmId = id
        lifecycleScope.launch {
            val alarm = repository.getById(id) ?: run { stopSelf(); return@launch }
            startForegroundCompat(alarm)
            ringtonePlayer.start(alarm)
            vibrationPlayer.start(alarm)
            autoSilenceJob = launch {
                delay(alarm.autoSilenceMinutes.minutes)
                Timber.i("Auto-silencing alarm ${alarm.id}")
                dismiss()
            }
        }
    }

    private fun snooze(id: Long) {
        lifecycleScope.launch {
            val alarm = repository.getById(id) ?: return@launch
            // Schedule a one-off trigger [snoozeMinutes] from now by upserting a
            // transient copy with an adjusted nextTriggerEpochMs — kept simple for
            // the scaffold; production logic should track snooze count per fire.
            val reArmed = alarm.copy(
                nextTriggerEpochMs = System.currentTimeMillis() + alarm.snoozeMinutes * 60_000L,
            )
            repository.upsert(reArmed)
            dismiss()
        }
    }

    private fun dismiss() {
        autoSilenceJob?.cancel()
        ringtonePlayer.stop()
        vibrationPlayer.stop()
        stopForegroundCompat()
        stopSelf()
    }

    override fun onDestroy() {
        ringtonePlayer.stop()
        vibrationPlayer.stop()
        super.onDestroy()
    }

    private fun startForegroundCompat(alarm: Alarm) {
        val notification = buildNotification(alarm)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK or
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE,
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun stopForegroundCompat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
    }

    private fun buildNotification(alarm: Alarm): Notification {
        val fullScreen = ringingActivityIntent(this, alarm.id)
        val fullScreenPi = PendingIntent.getActivity(
            this,
            alarm.id.toInt(),
            fullScreen,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        return NotificationCompat.Builder(this, NotificationChannels.RINGING_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(alarm.label.ifBlank { "Alarm" })
            .setContentText("Tap to dismiss")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setFullScreenIntent(fullScreenPi, /* highPriority = */ true)
            .setContentIntent(fullScreenPi)
            .build()
    }

    companion object {
        private const val NOTIFICATION_ID = 42
        const val ACTION_START = "com.alarmapp.action.RING_START"
        const val ACTION_SNOOZE = "com.alarmapp.action.RING_SNOOZE"
        const val ACTION_DISMISS = "com.alarmapp.action.RING_DISMISS"
        const val EXTRA_ALARM_ID = "alarm_id"

        fun start(context: Context, alarmId: Long) {
            val intent = Intent(context, AlarmService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_ALARM_ID, alarmId)
            }
            context.startForegroundService(intent)
        }

        private fun ringingActivityIntent(context: Context, alarmId: Long): Intent =
            Intent().setClassName(context, "com.alarmapp.feature.ringing.RingingActivity")
                .putExtra(EXTRA_ALARM_ID, alarmId)
                .addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_NO_USER_ACTION,
                )
    }
}
