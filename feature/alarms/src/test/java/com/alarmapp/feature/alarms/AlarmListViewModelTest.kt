package com.alarmapp.feature.alarms

import app.cash.turbine.test
import com.alarmapp.core.domain.model.Alarm
import com.alarmapp.core.domain.repository.AlarmRepository
import com.alarmapp.core.domain.scheduler.AlarmScheduler
import com.alarmapp.core.domain.usecase.ToggleAlarmUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AlarmListViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    @Test fun `emits alarms observed from repository`() = runTest(dispatcher) {
        val alarms = listOf(
            Alarm(id = 1, hour = 6, minute = 0),
            Alarm(id = 2, hour = 7, minute = 30),
        )
        val repo = mockk<AlarmRepository>(relaxed = true).apply {
            every { observeAll() } returns flowOf(alarms)
        }
        val scheduler = mockk<AlarmScheduler>(relaxed = true)
        val vm = AlarmListViewModel(repo, ToggleAlarmUseCase(repo, scheduler))

        vm.uiState.test {
            // Initial empty state followed by the mocked flow payload.
            skipItems(1)
            val emitted = awaitItem()
            assertEquals(2, emitted.alarms.size)
        }
    }

    @Test fun `setEnabled delegates to toggle use case and reschedules`() = runTest(dispatcher) {
        val repo = mockk<AlarmRepository>(relaxed = true).apply {
            every { observeAll() } returns flowOf(emptyList())
            coEvery { getById(42) } returns Alarm(id = 42, hour = 7, minute = 0, enabled = true)
            coEvery { upsert(any()) } returns 42L
        }
        val scheduler = mockk<AlarmScheduler>(relaxed = true)
        val vm = AlarmListViewModel(repo, ToggleAlarmUseCase(repo, scheduler))

        vm.setEnabled(id = 42, enabled = true)
        dispatcher.scheduler.advanceUntilIdle()

        coVerify { scheduler.schedule(match { it.id == 42L }) }
    }

    @Test fun `setEnabled false cancels rather than scheduling`() = runTest(dispatcher) {
        val repo = mockk<AlarmRepository>(relaxed = true).apply {
            every { observeAll() } returns flowOf(emptyList())
            coEvery { getById(42) } returns Alarm(id = 42, hour = 7, minute = 0, enabled = false)
        }
        val scheduler = mockk<AlarmScheduler>(relaxed = true)
        val vm = AlarmListViewModel(repo, ToggleAlarmUseCase(repo, scheduler))

        vm.setEnabled(id = 42, enabled = false)
        dispatcher.scheduler.advanceUntilIdle()

        coVerify { scheduler.cancel(42) }
    }
}
