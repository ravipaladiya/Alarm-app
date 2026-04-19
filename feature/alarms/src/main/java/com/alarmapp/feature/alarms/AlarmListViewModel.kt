package com.alarmapp.feature.alarms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alarmapp.core.domain.model.Alarm
import com.alarmapp.core.domain.repository.AlarmRepository
import com.alarmapp.core.domain.usecase.ToggleAlarmUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AlarmListUiState(val alarms: List<Alarm> = emptyList())

@HiltViewModel
class AlarmListViewModel @Inject constructor(
    repository: AlarmRepository,
    private val toggle: ToggleAlarmUseCase,
) : ViewModel() {

    val uiState: StateFlow<AlarmListUiState> =
        repository.observeAll()
            .map(::AlarmListUiState)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AlarmListUiState())

    fun setEnabled(id: Long, enabled: Boolean) {
        viewModelScope.launch { toggle(id, enabled) }
    }
}
