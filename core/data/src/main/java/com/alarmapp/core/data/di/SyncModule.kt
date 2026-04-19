package com.alarmapp.core.data.di

import android.content.Context
import com.alarmapp.core.data.sync.FirestoreSyncRepository
import com.alarmapp.core.data.sync.NoopSyncRepository
import com.alarmapp.core.domain.sync.SyncRepository
import com.google.firebase.FirebaseApp
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Picks [FirestoreSyncRepository] when Firebase has been initialized (i.e. the
 * app module applied the google-services plugin and shipped a
 * `google-services.json`). Otherwise falls back to [NoopSyncRepository] so
 * builds without Firebase are still installable and runnable.
 */
@Module
@InstallIn(SingletonComponent::class)
object SyncModule {

    @Provides @Singleton
    fun provideSyncRepository(
        @ApplicationContext context: Context,
        firestore: dagger.Lazy<FirestoreSyncRepository>,
        noop: dagger.Lazy<NoopSyncRepository>,
    ): SyncRepository =
        if (FirebaseApp.getApps(context).isNotEmpty()) firestore.get() else noop.get()
}
