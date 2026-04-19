package com.alarmapp.core.domain.model

import kotlinx.serialization.Serializable

/**
 * A user-configured alarm.
 *
 * Time is stored as a local [hour]/[minute] pair rather than an absolute instant so
 * the alarm reschedules correctly across time-zone and DST changes. The actual next
 * trigger is computed by `nextTriggerEpochMillis` and cached in [nextTriggerEpochMs]
 * for list sorting and scheduler comparisons.
 */
data class Alarm(
    val id: Long = 0,
    val label: String = "",
    val hour: Int,
    val minute: Int,
    val repeatDaysMask: Int = 0,
    val enabled: Boolean = true,
    val soundUri: String? = null,
    val vibrationPattern: VibrationPattern = VibrationPattern.Default,
    val fadeInSeconds: Int = 15,
    val snoozeMinutes: Int = 9,
    val snoozeLimit: Int = 3,
    val autoSilenceMinutes: Int = 10,
    val volumePercent: Int = 70,
    val dismissChallenge: DismissChallenge = DismissChallenge.None,
    val colorTag: Int = 0,
    val iconKey: String = "alarm",
    val nextTriggerEpochMs: Long = 0,
    val remoteId: String? = null,
    val updatedAtEpochMs: Long = 0,
)

enum class VibrationPattern { None, Default, Gentle, Strong }

@Serializable
sealed interface DismissChallenge {
    @Serializable
    data object None : DismissChallenge

    @Serializable
    data class Math(val difficulty: Int = 1) : DismissChallenge

    @Serializable
    data class Shake(val count: Int = 20) : DismissChallenge

    @Serializable
    data class Qr(val payload: String) : DismissChallenge

    @Serializable
    data class Typing(val text: String) : DismissChallenge
}
