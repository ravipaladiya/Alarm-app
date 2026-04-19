package com.alarmapp.feature.alarms

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.alarmapp.core.domain.model.Alarm
import com.alarmapp.core.domain.model.DismissChallenge
import com.alarmapp.core.domain.repository.AlarmRepository
import com.alarmapp.core.domain.usecase.SaveAlarmUseCase
import com.alarmapp.core.domain.scheduler.AlarmScheduler
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
class AlarmEditViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before fun setUp() = Dispatchers.setMain(dispatcher)

    @After fun tearDown() = Dispatchers.resetMain()

    @Test fun `new alarm starts at 07 00`() = runTest(dispatcher) {
        val repo = mockk<AlarmRepository>(relaxed = true).apply {
            every { observeAll() } returns flowOf(emptyList())
        }
        val scheduler = mockk<AlarmScheduler>(relaxed = true)
        val vm = AlarmEditViewModel(
            savedStateHandle = SavedStateHandle(),
            repository = repo,
            saveAlarm = SaveAlarmUseCase(repo, scheduler),
        )
        vm.state.test {
            val first = awaitItem()
            assertEquals(7, first.alarm.hour)
            assertEquals(0, first.alarm.minute)
        }
    }

    @Test fun `setChallenge updates state`() = runTest(dispatcher) {
        val repo = mockk<AlarmRepository>(relaxed = true)
        val scheduler = mockk<AlarmScheduler>(relaxed = true)
        val vm = AlarmEditViewModel(SavedStateHandle(), repo, SaveAlarmUseCase(repo, scheduler))
        vm.setChallenge(DismissChallenge.Shake(count = 25))
        vm.state.test {
            val latest = expectMostRecentItem()
            assertEquals(DismissChallenge.Shake(count = 25), latest.alarm.dismissChallenge)
        }
    }

    @Test fun `save invokes repository and schedules when enabled`() = runTest(dispatcher) {
        val repo = mockk<AlarmRepository>(relaxed = true).apply {
            coEvery { upsert(any()) } returns 5L
            coEvery { getById(5L) } returns Alarm(id = 5, hour = 7, minute = 30, enabled = true)
        }
        val scheduler = mockk<AlarmScheduler>(relaxed = true)
        val vm = AlarmEditViewModel(SavedStateHandle(), repo, SaveAlarmUseCase(repo, scheduler))
        vm.save(onDone = {})
        dispatcher.scheduler.advanceUntilIdle()
        coVerify { scheduler.schedule(match { it.id == 5L }) }
    }
}
