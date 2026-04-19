package com.alarmapp.core.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SleepDao {

    @Query("SELECT * FROM sleep_sessions ORDER BY bedtimeEpochMs DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<SleepSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: SleepSessionEntity): Long

    @Query(
        "UPDATE sleep_sessions SET wakeEpochMs = :wakeMs, " +
            "durationMs = :durationMs WHERE id = :id",
    )
    suspend fun end(id: Long, wakeMs: Long, durationMs: Long)

    @Query("UPDATE sleep_sessions SET quality = :quality WHERE id = :id")
    suspend fun rate(id: Long, quality: String)
}
