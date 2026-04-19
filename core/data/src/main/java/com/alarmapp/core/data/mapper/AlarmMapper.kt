package com.alarmapp.core.data.mapper

import com.alarmapp.core.data.db.AlarmEntity
import com.alarmapp.core.domain.model.Alarm
import com.alarmapp.core.domain.model.DismissChallenge
import com.alarmapp.core.domain.model.VibrationPattern
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

internal fun AlarmEntity.toDomain(): Alarm = Alarm(
    id = id,
    label = label,
    hour = hour,
    minute = minute,
    repeatDaysMask = repeatDaysMask,
    enabled = enabled,
    soundUri = soundUri,
    vibrationPattern = VibrationPattern.valueOf(vibrationPattern),
    fadeInSeconds = fadeInSeconds,
    snoozeMinutes = snoozeMinutes,
    snoozeLimit = snoozeLimit,
    autoSilenceMinutes = autoSilenceMinutes,
    volumePercent = volumePercent,
    dismissChallenge = json.decodeFromString(DismissChallenge.serializer(), dismissChallengeJson),
    colorTag = colorTag,
    iconKey = iconKey,
    nextTriggerEpochMs = nextTriggerEpochMs,
    remoteId = remoteId,
    updatedAtEpochMs = updatedAtEpochMs,
)

internal fun Alarm.toEntity(): AlarmEntity = AlarmEntity(
    id = id,
    label = label,
    hour = hour,
    minute = minute,
    repeatDaysMask = repeatDaysMask,
    enabled = enabled,
    soundUri = soundUri,
    vibrationPattern = vibrationPattern.name,
    fadeInSeconds = fadeInSeconds,
    snoozeMinutes = snoozeMinutes,
    snoozeLimit = snoozeLimit,
    autoSilenceMinutes = autoSilenceMinutes,
    volumePercent = volumePercent,
    dismissChallengeJson = json.encodeToString(DismissChallenge.serializer(), dismissChallenge),
    colorTag = colorTag,
    iconKey = iconKey,
    nextTriggerEpochMs = nextTriggerEpochMs,
    remoteId = remoteId,
    updatedAtEpochMs = updatedAtEpochMs,
)
