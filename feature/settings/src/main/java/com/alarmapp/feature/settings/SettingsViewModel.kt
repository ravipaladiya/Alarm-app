package com.alarmapp.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alarmapp.core.data.prefs.AppSettings
import com.alarmapp.core.data.prefs.SettingsDataStore
import com.alarmapp.core.data.prefs.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(val settings: AppSettings = AppSettings())

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val store: SettingsDataStore,
) : ViewModel() {

    val state: StateFlow<SettingsUiState> = store.settings
        .map(::SettingsUiState)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun setTheme(mode: ThemeMode) = viewModelScope.launch { store.setTheme(mode) }
    fun setDynamicColor(enabled: Boolean) = viewModelScope.launch { store.setDynamicColor(enabled) }
    fun setCloudSync(enabled: Boolean) = viewModelScope.launch { store.setCloudSync(enabled) }
}
