package com.alarmapp.core.domain.model

data class SleepSession(
    val id: Long = 0,
    val bedtimeEpochMs: Long,
    val wakeEpochMs: Long?,
    val durationMs: Long?,
    val quality: SleepQuality = SleepQuality.Unknown,
    val remoteId: String? = null,
)

enum class SleepQuality { Unknown, Poor, Fair, Good, Great }
