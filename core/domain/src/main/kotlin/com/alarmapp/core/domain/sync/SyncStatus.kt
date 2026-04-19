package com.alarmapp.core.domain.sync

/**
 * Coarse state of the cloud-sync subsystem. The VM can render a short status
 * line from this; the repository contract is the source of truth.
 */
sealed interface SyncStatus {
    /** User hasn't enabled sync, or no Firebase project is configured. */
    data object Disabled : SyncStatus
    data object SignedOut : SyncStatus
    data object Syncing : SyncStatus
    data class Idle(val lastSyncEpochMs: Long) : SyncStatus
    data class Failed(val message: String) : SyncStatus
}

data class AuthUser(val uid: String, val displayName: String?, val email: String?)
