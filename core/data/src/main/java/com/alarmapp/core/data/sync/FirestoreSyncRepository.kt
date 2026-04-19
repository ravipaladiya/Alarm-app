package com.alarmapp.core.data.sync

import android.content.Context
import com.alarmapp.core.common.IoDispatcher
import com.alarmapp.core.common.time.Clock
import com.alarmapp.core.data.db.AlarmDao
import com.alarmapp.core.data.mapper.toDomain
import com.alarmapp.core.data.mapper.toEntity
import com.alarmapp.core.data.prefs.SettingsDataStore
import com.alarmapp.core.domain.repository.AlarmRepository
import com.alarmapp.core.domain.sync.AuthUser
import com.alarmapp.core.domain.sync.SyncRepository
import com.alarmapp.core.domain.sync.SyncStatus
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firestore-backed cloud sync with last-write-wins conflict resolution on
 * `updatedAtEpochMs`. No-ops when Firebase isn't initialized (e.g. debug
 * builds without `google-services.json`).
 *
 * Storage layout: `/users/{uid}/alarms/{remoteIdOrLocalId}` — one document per
 * alarm. The device's nextTriggerEpochMs, ringtone URI, and exact-alarm state
 * are never round-tripped; they are always recomputed locally after a pull.
 */
@Singleton
class FirestoreSyncRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val alarmRepository: AlarmRepository,
    private val alarmDao: AlarmDao,
    private val clock: Clock,
    private val settings: SettingsDataStore,
    @IoDispatcher private val io: CoroutineDispatcher,
) : SyncRepository {

    private val _status = MutableStateFlow<SyncStatus>(SyncStatus.Disabled)
    private val _user = MutableStateFlow<AuthUser?>(null)

    override val status: Flow<SyncStatus> = _status.asStateFlow()
    override val currentUser: Flow<AuthUser?> = _user.asStateFlow()

    private val firebaseReady: Boolean
        get() = FirebaseApp.getApps(context).isNotEmpty()

    override suspend fun enable() {
        settings.setCloudSync(true)
        if (!firebaseReady) {
            _status.value = SyncStatus.Disabled
            return
        }
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            _status.value = SyncStatus.SignedOut
        } else {
            _status.value = SyncStatus.Idle(clock.nowMillis())
        }
    }

    override suspend fun disable() {
        settings.setCloudSync(false)
        _status.value = SyncStatus.Disabled
    }

    override suspend fun signInAnonymously(): Result<AuthUser> = runCatching {
        if (!firebaseReady) error("Firebase not configured")
        withContext(io) {
            val result = FirebaseAuth.getInstance().signInAnonymously().await()
            val fu = result.user ?: error("Anonymous sign-in returned no user")
            AuthUser(fu.uid, fu.displayName, fu.email).also { _user.value = it }
        }
    }

    override suspend fun signOut() {
        if (firebaseReady) FirebaseAuth.getInstance().signOut()
        _user.value = null
        _status.value = SyncStatus.SignedOut
    }

    override suspend fun syncNow(): Result<Unit> = runCatching {
        if (!firebaseReady) return Result.success(Unit)
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            _status.value = SyncStatus.SignedOut
            return@runCatching
        }
        _status.update { SyncStatus.Syncing }
        withContext(io) {
            val firestore = FirebaseFirestore.getInstance()
            val remote = firestore.collection("users").document(uid)
                .collection("alarms").get().await()

            // Pull: upsert anything remote that's newer than local.
            for (doc in remote.documents) {
                val remoteAlarm = doc.toObject(RemoteAlarm::class.java) ?: continue
                val local = alarmDao.getById(remoteAlarm.id)
                if (local == null || remoteAlarm.updatedAtEpochMs > local.updatedAtEpochMs) {
                    alarmRepository.upsert(remoteAlarm.toDomainModel())
                }
            }

            // Push: upload anything local that's newer than the remote copy.
            val locals = alarmDao.all()
            for (entity in locals) {
                val ref = firestore.collection("users").document(uid)
                    .collection("alarms").document(entity.id.toString())
                val snapshot = ref.get().await()
                val remoteUpdated = snapshot.getLong("updatedAtEpochMs") ?: 0L
                if (entity.updatedAtEpochMs > remoteUpdated) {
                    ref.set(RemoteAlarm.from(entity.toDomain())).await()
                }
            }
        }
        _status.value = SyncStatus.Idle(clock.nowMillis())
    }.onFailure { t ->
        Timber.w(t, "Sync failed")
        _status.value = SyncStatus.Failed(t.message ?: "unknown")
    }
}
