package com.alarmapp.core.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val label: String,
    val hour: Int,
    val minute: Int,
    val repeatDaysMask: Int,
    val enabled: Boolean,
    val soundUri: String?,
    val vibrationPattern: String,
    val fadeInSeconds: Int,
    val snoozeMinutes: Int,
    val snoozeLimit: Int,
    val autoSilenceMinutes: Int,
    val volumePercent: Int,
    val dismissChallengeJson: String,
    val colorTag: Int,
    val iconKey: String,
    val nextTriggerEpochMs: Long,
    val remoteId: String?,
    val updatedAtEpochMs: Long,
)
