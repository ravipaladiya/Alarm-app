package com.alarmapp.core.common.di

import com.alarmapp.core.common.ApplicationScope
import com.alarmapp.core.common.DefaultDispatcher
import com.alarmapp.core.common.IoDispatcher
import com.alarmapp.core.common.MainDispatcher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CoroutineModule {

    @Provides @IoDispatcher
    fun provideIo(): CoroutineDispatcher = Dispatchers.IO

    @Provides @DefaultDispatcher
    fun provideDefault(): CoroutineDispatcher = Dispatchers.Default

    @Provides @MainDispatcher
    fun provideMain(): CoroutineDispatcher = Dispatchers.Main

    @Provides @Singleton @ApplicationScope
    fun provideApplicationScope(
        @DefaultDispatcher dispatcher: CoroutineDispatcher,
    ): CoroutineScope = CoroutineScope(SupervisorJob() + dispatcher)
}
