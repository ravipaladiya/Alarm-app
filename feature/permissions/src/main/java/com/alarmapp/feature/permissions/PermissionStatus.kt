package com.alarmapp.feature.permissions

/**
 * Identifier for a permission the alarm app needs in order to fire reliably.
 *
 * Notifications and exact-alarm rights are required on modern Android — without
 * them the full-screen intent is dropped and the alarm is effectively silent.
 * The remaining two (full-screen intent on API 34+, battery optimizations)
 * are strongly recommended but the app can technically fire without them.
 */
enum class AppPermission(val required: Boolean) {
    Notifications(required = true),
    ExactAlarms(required = true),
    FullScreenIntent(required = false),
    BatteryOptimizations(required = false),
}

enum class PermissionState { Granted, Denied, Unknown, NotApplicable }

data class PermissionEntry(val permission: AppPermission, val state: PermissionState)

data class PermissionsUiState(
    val entries: List<PermissionEntry> = emptyList(),
) {
    val allRequiredGranted: Boolean
        get() = entries
            .filter { it.permission.required }
            .all { it.state == PermissionState.Granted || it.state == PermissionState.NotApplicable }
}
