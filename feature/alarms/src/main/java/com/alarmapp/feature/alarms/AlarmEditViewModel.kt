package com.alarmapp.feature.alarms

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alarmapp.core.domain.model.Alarm
import com.alarmapp.core.domain.model.DismissChallenge
import com.alarmapp.core.domain.repository.AlarmRepository
import com.alarmapp.core.domain.usecase.DeleteAlarmUseCase
import com.alarmapp.core.domain.usecase.SaveAlarmUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AlarmEditUiState(val alarm: Alarm = DEFAULT, val saving: Boolean = false) {
    companion object {
        internal val DEFAULT = Alarm(hour = 7, minute = 0)
    }
}

@HiltViewModel
class AlarmEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: AlarmRepository,
    private val saveAlarm: SaveAlarmUseCase,
    private val deleteAlarm: DeleteAlarmUseCase,
) : ViewModel() {

    private val initialId: Long? = savedStateHandle.get<String>("id")?.toLongOrNull()

    private val _state = MutableStateFlow(AlarmEditUiState())
    val state: StateFlow<AlarmEditUiState> = _state.asStateFlow()

    init {
        if (initialId != null) {
            viewModelScope.launch {
                val loaded = repository.getById(initialId) ?: AlarmEditUiState.DEFAULT
                _state.update { it.copy(alarm = loaded) }
            }
        }
    }

    fun setTime(hour: Int, minute: Int) = _state.update { it.copy(alarm = it.alarm.copy(hour = hour, minute = minute)) }
    fun setLabel(label: String) = _state.update { it.copy(alarm = it.alarm.copy(label = label)) }
    fun toggleDay(dayIndex: Int) = _state.update {
        val mask = it.alarm.repeatDaysMask xor (1 shl dayIndex)
        it.copy(alarm = it.alarm.copy(repeatDaysMask = mask))
    }
    fun setChallenge(challenge: DismissChallenge) =
        _state.update { it.copy(alarm = it.alarm.copy(dismissChallenge = challenge)) }
    fun setSnoozeMinutes(minutes: Int) =
        _state.update { it.copy(alarm = it.alarm.copy(snoozeMinutes = minutes)) }
    fun setFadeIn(seconds: Int) =
        _state.update { it.copy(alarm = it.alarm.copy(fadeInSeconds = seconds)) }
    fun setVolumePercent(percent: Int) =
        _state.update { it.copy(alarm = it.alarm.copy(volumePercent = percent)) }

    fun save(onDone: () -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(saving = true) }
            saveAlarm(_state.value.alarm)
            _state.update { it.copy(saving = false) }
            onDone()
        }
    }

    fun delete(onDone: () -> Unit) {
        viewModelScope.launch {
            _state.value.alarm.takeIf { it.id != 0L }?.let { deleteAlarm(it.id) }
            onDone()
        }
    }
}
