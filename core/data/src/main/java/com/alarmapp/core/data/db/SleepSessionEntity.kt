package com.alarmapp.core.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sleep_sessions")
data class SleepSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bedtimeEpochMs: Long,
    val wakeEpochMs: Long?,
    val durationMs: Long?,
    val quality: String,
    val remoteId: String?,
)
