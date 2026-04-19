package com.alarmapp.core.data.di

import android.content.Context
import androidx.room.Room
import com.alarmapp.core.common.time.Clock
import com.alarmapp.core.common.time.SystemClock
import com.alarmapp.core.data.db.AlarmDao
import com.alarmapp.core.data.db.AlarmDatabase
import com.alarmapp.core.data.db.SleepDao
import com.alarmapp.core.data.repository.AlarmRepositoryImpl
import com.alarmapp.core.data.repository.SleepRepositoryImpl
import com.alarmapp.core.domain.repository.AlarmRepository
import com.alarmapp.core.domain.repository.SleepRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AlarmDatabase =
        Room.databaseBuilder(context, AlarmDatabase::class.java, "alarm.db")
            .fallbackToDestructiveMigration(/* dropAllTables = */ true)
            .build()

    @Provides fun provideAlarmDao(db: AlarmDatabase): AlarmDao = db.alarmDao()
    @Provides fun provideSleepDao(db: AlarmDatabase): SleepDao = db.sleepDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class DataBindsModule {

    @Binds @Singleton
    abstract fun bindAlarmRepository(impl: AlarmRepositoryImpl): AlarmRepository

    @Binds @Singleton
    abstract fun bindSleepRepository(impl: SleepRepositoryImpl): SleepRepository

    @Binds @Singleton
    abstract fun bindClock(impl: SystemClock): Clock
}
