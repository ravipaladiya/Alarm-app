package com.alarmapp.core.common.di

import com.alarmapp.core.common.crash.CrashReporter
import com.alarmapp.core.common.crash.LoggingCrashReporter
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Default binding: log-only. Swap the @Binds target (or supply a higher-priority
 * binding in an :app-level module) once Firebase Crashlytics or an alternative
 * is wired up.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class CrashReportingModule {
    @Binds @Singleton
    abstract fun bindCrashReporter(impl: LoggingCrashReporter): CrashReporter
}
