package com.alarmapp.feature.sleep

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alarmapp.core.common.time.Clock
import com.alarmapp.core.domain.model.SleepQuality
import com.alarmapp.core.domain.model.SleepSession
import com.alarmapp.core.domain.repository.SleepRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SleepUiState(
    val sessions: List<SleepSession> = emptyList(),
)

@HiltViewModel
class SleepViewModel @Inject constructor(
    private val repository: SleepRepository,
    private val clock: Clock,
) : ViewModel() {

    val state: StateFlow<SleepUiState> = repository.observeRecent(limit = 30)
        .map(::SleepUiState)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SleepUiState())

    /**
     * Record a "bedtime started now" row; can be ended later via [endCurrent].
     * A real product would wire this to the alarm-dismiss flow so waking up
     * ends the session automatically.
     */
    fun startBedtimeNow() {
        viewModelScope.launch { repository.startSession(clock.nowMillis()) }
    }

    fun endCurrent() {
        viewModelScope.launch {
            val current = state.value.sessions.firstOrNull { it.wakeEpochMs == null }
            if (current != null) {
                repository.endSession(current.id, clock.nowMillis())
            }
        }
    }

    fun rate(id: Long, quality: SleepQuality) {
        viewModelScope.launch { repository.rateSession(id, quality) }
    }
}
