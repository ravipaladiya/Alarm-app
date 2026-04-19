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
 * Re-arm every enabled alarm after device reboot or package replacement.
 * AlarmManager state is not persisted across reboots, so this receiver is
 * required for correctness.
 */
@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject lateinit var repository: AlarmRepository
    @Inject lateinit var scheduler: AlarmScheduler
    @Inject @ApplicationScope lateinit var appScope: CoroutineScope

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_LOCKED_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_PACKAGE_REPLACED,
            -> Unit
            else -> return
        }
        val pending = goAsync()
        appScope.launch {
            try {
                val enabled = repository.allEnabled()
                Timber.d("Rescheduling ${enabled.size} alarms after ${intent.action}")
                scheduler.rescheduleAll(enabled)
            } catch (t: Throwable) {
                Timber.e(t, "Failed to reschedule alarms")
            } finally {
                pending.finish()
            }
        }
    }
}
