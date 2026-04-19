package com.alarmapp.core.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [AlarmEntity::class, SleepSessionEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class AlarmDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao
    abstract fun sleepDao(): SleepDao
}
