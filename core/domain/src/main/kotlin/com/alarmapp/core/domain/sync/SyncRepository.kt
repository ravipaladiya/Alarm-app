package com.alarmapp.core.domain.sync

import kotlinx.coroutines.flow.Flow

/**
 * Cross-device sync orchestration.
 *
 * Contract:
 *  - Local Room is the source of truth when offline. Remote reconciliation is
 *    last-write-wins on `updatedAtEpochMs`.
 *  - Per-device fields (ringtone URI, exact-alarm permission state,
 *    nextTriggerEpochMs) are NOT synced; they are always recomputed locally.
 *  - Implementations must be safe to invoke from a WorkManager worker and
 *    must handle offline gracefully (return a status, never throw).
 */
interface SyncRepository {
    val status: Flow<SyncStatus>
    val currentUser: Flow<AuthUser?>
    suspend fun enable()
    suspend fun disable()
    suspend fun syncNow(): Result<Unit>
    suspend fun signInAnonymously(): Result<AuthUser>
    suspend fun signOut()
}
