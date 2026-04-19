package com.alarmapp.core.alarm.di

import com.alarmapp.core.alarm.scheduler.AlarmManagerScheduler
import com.alarmapp.core.domain.scheduler.AlarmScheduler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AlarmModule {
    @Binds @Singleton
    abstract fun bindScheduler(impl: AlarmManagerScheduler): AlarmScheduler
}
