package com.alarmapp.core.data.sync

import com.alarmapp.core.domain.model.Alarm
import com.alarmapp.core.domain.model.DismissChallenge
import com.alarmapp.core.domain.model.VibrationPattern
import kotlinx.serialization.json.Json

/**
 * On-the-wire representation of an [Alarm] for Firestore.
 *
 * Deliberately omits device-scoped fields (`nextTriggerEpochMs`, ringtone URI
 * is stored as an opaque key) because they don't round-trip across devices.
 * The receiving device recomputes trigger time locally via
 * `nextTriggerEpochMillis` after upsert.
 */
// @JvmOverloads forces the Kotlin compiler to emit a Java no-arg constructor
// alongside the default-arg variants; Firestore's reflection-based toObject()
// instantiation depends on that no-arg constructor being present.
data class RemoteAlarm @JvmOverloads constructor(
    val id: Long = 0,
    val label: String = "",
    val hour: Int = 0,
    val minute: Int = 0,
    val repeatDaysMask: Int = 0,
    val enabled: Boolean = true,
    val soundUri: String? = null,
    val vibrationPattern: String = VibrationPattern.Default.name,
    val fadeInSeconds: Int = 15,
    val snoozeMinutes: Int = 9,
    val snoozeLimit: Int = 3,
    val autoSilenceMinutes: Int = 10,
    val volumePercent: Int = 70,
    val dismissChallengeJson: String = NO_CHALLENGE_JSON,
    val colorTag: Int = 0,
    val iconKey: String = "alarm",
    val updatedAtEpochMs: Long = 0,
) {
    fun toDomainModel(): Alarm = Alarm(
        id = id,
        label = label,
        hour = hour,
        minute = minute,
        repeatDaysMask = repeatDaysMask,
        enabled = enabled,
        soundUri = soundUri,
        vibrationPattern = runCatching { VibrationPattern.valueOf(vibrationPattern) }
            .getOrDefault(VibrationPattern.Default),
        fadeInSeconds = fadeInSeconds,
        snoozeMinutes = snoozeMinutes,
        snoozeLimit = snoozeLimit,
        autoSilenceMinutes = autoSilenceMinutes,
        volumePercent = volumePercent,
        dismissChallenge = runCatching {
            json.decodeFromString(DismissChallenge.serializer(), dismissChallengeJson)
        }.getOrDefault(DismissChallenge.None),
        colorTag = colorTag,
        iconKey = iconKey,
        nextTriggerEpochMs = 0L,
        updatedAtEpochMs = updatedAtEpochMs,
    )

    companion object {
        private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
        private val NO_CHALLENGE_JSON = json.encodeToString(
            DismissChallenge.serializer(),
            DismissChallenge.None,
        )

        fun from(alarm: Alarm): RemoteAlarm = RemoteAlarm(
            id = alarm.id,
            label = alarm.label,
            hour = alarm.hour,
            minute = alarm.minute,
            repeatDaysMask = alarm.repeatDaysMask,
            enabled = alarm.enabled,
            soundUri = alarm.soundUri,
            vibrationPattern = alarm.vibrationPattern.name,
            fadeInSeconds = alarm.fadeInSeconds,
            snoozeMinutes = alarm.snoozeMinutes,
            snoozeLimit = alarm.snoozeLimit,
            autoSilenceMinutes = alarm.autoSilenceMinutes,
            volumePercent = alarm.volumePercent,
            dismissChallengeJson = json.encodeToString(
                DismissChallenge.serializer(),
                alarm.dismissChallenge,
            ),
            colorTag = alarm.colorTag,
            iconKey = alarm.iconKey,
            updatedAtEpochMs = alarm.updatedAtEpochMs,
        )
    }
}
