package com.alarmapp.feature.permissions

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Reads current permission state from the OS. All API-level gating is centralized
 * here so the UI can treat "not applicable on this device" the same as "granted".
 */
@Singleton
class PermissionChecker @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    fun snapshot(): PermissionsUiState = PermissionsUiState(
        entries = AppPermission.entries.map { PermissionEntry(it, stateOf(it)) },
    )

    fun stateOf(permission: AppPermission): PermissionState = when (permission) {
        AppPermission.Notifications -> notificationsState()
        AppPermission.ExactAlarms -> exactAlarmsState()
        AppPermission.FullScreenIntent -> fullScreenIntentState()
        AppPermission.BatteryOptimizations -> batteryOptimizationsState()
    }

    private fun notificationsState(): PermissionState {
        // Runtime request only required on API 33+.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return notificationsEnabledChannelState()
        }
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
        return if (granted) notificationsEnabledChannelState() else PermissionState.Denied
    }

    private fun notificationsEnabledChannelState(): PermissionState {
        // Even when the permission is held, the user may have disabled the
        // ringing channel from system Settings. Treat that as Denied.
        val mgr = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val enabled = mgr.areNotificationsEnabled()
        return if (enabled) PermissionState.Granted else PermissionState.Denied
    }

    private fun exactAlarmsState(): PermissionState {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return PermissionState.NotApplicable
        val mgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        return if (mgr.canScheduleExactAlarms()) PermissionState.Granted else PermissionState.Denied
    }

    private fun fullScreenIntentState(): PermissionState {
        // Runtime gating was introduced on API 34 for new installs.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            return PermissionState.NotApplicable
        }
        val mgr = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return if (mgr.canUseFullScreenIntent()) PermissionState.Granted else PermissionState.Denied
    }

    private fun batteryOptimizationsState(): PermissionState {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return PermissionState.NotApplicable
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val ignoring = pm.isIgnoringBatteryOptimizations(context.packageName)
        return if (ignoring) PermissionState.Granted else PermissionState.Denied
    }
}
