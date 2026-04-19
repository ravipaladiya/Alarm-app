package com.alarmapp.feature.ringing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alarmapp.core.domain.model.Alarm
import com.alarmapp.core.domain.model.DismissChallenge
import com.alarmapp.core.domain.repository.AlarmRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RingingUiState(
    val alarm: Alarm? = null,
    val challenge: DismissChallenge = DismissChallenge.None,
    val challengeComplete: Boolean = true,
)

@HiltViewModel
class RingingViewModel @Inject constructor(
    private val repository: AlarmRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(RingingUiState())
    val state: StateFlow<RingingUiState> = _state.asStateFlow()

    fun bindAlarm(id: Long) {
        if (_state.value.alarm?.id == id) return
        viewModelScope.launch {
            val alarm = repository.getById(id) ?: return@launch
            _state.value = RingingUiState(
                alarm = alarm,
                challenge = alarm.dismissChallenge,
                challengeComplete = alarm.dismissChallenge is DismissChallenge.None,
            )
        }
    }

    fun onChallengeProgress(complete: Boolean) {
        _state.update { it.copy(challengeComplete = complete) }
    }
}
