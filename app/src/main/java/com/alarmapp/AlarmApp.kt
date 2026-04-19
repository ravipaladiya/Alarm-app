package com.alarmapp

import android.app.Application
import android.content.pm.ApplicationInfo
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.alarmapp.core.alarm.NotificationChannels
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class AlarmApp : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        if (isDebuggable()) {
            Timber.plant(Timber.DebugTree())
        }
        NotificationChannels.ensureCreated(this)
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    private fun isDebuggable(): Boolean =
        applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
}
