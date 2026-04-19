package com.alarmapp.core.data.sync

import com.alarmapp.core.domain.sync.AuthUser
import com.alarmapp.core.domain.sync.SyncRepository
import com.alarmapp.core.domain.sync.SyncStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Fallback [SyncRepository] used when Firebase is not configured. All operations
 * are reported as disabled; no network traffic is attempted. Binding this keeps
 * the app compilable and runnable without `google-services.json`.
 */
@Singleton
class NoopSyncRepository @Inject constructor() : SyncRepository {
    private val _status = MutableStateFlow<SyncStatus>(SyncStatus.Disabled)
    private val _user = MutableStateFlow<AuthUser?>(null)

    override val status: Flow<SyncStatus> = _status.asStateFlow()
    override val currentUser: Flow<AuthUser?> = _user.asStateFlow()

    override suspend fun enable() = Unit
    override suspend fun disable() = Unit
    override suspend fun syncNow(): Result<Unit> = Result.success(Unit)
    override suspend fun signInAnonymously(): Result<AuthUser> =
        Result.failure(IllegalStateException("Sync not configured"))
    override suspend fun signOut() = Unit
}
