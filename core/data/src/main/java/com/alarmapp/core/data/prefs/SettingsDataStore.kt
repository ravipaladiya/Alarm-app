package com.alarmapp.core.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "alarm_settings")

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.System,
    val dynamicColor: Boolean = true,
    val defaultSnoozeMinutes: Int = 9,
    val cloudSyncEnabled: Boolean = false,
)

enum class ThemeMode { System, Light, Dark }

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private object Keys {
        val theme = stringPreferencesKey("theme_mode")
        val dynamic = booleanPreferencesKey("dynamic_color")
        val snooze = intPreferencesKey("default_snooze_minutes")
        val sync = booleanPreferencesKey("cloud_sync_enabled")
    }

    val settings: Flow<AppSettings> = context.dataStore.data.map { it.toSettings() }

    suspend fun setTheme(mode: ThemeMode) = context.dataStore.edit { it[Keys.theme] = mode.name }
    suspend fun setDynamicColor(enabled: Boolean) = context.dataStore.edit { it[Keys.dynamic] = enabled }
    suspend fun setDefaultSnooze(minutes: Int) = context.dataStore.edit { it[Keys.snooze] = minutes }
    suspend fun setCloudSync(enabled: Boolean) = context.dataStore.edit { it[Keys.sync] = enabled }

    private fun Preferences.toSettings() = AppSettings(
        themeMode = runCatching { ThemeMode.valueOf(this[Keys.theme] ?: "System") }
            .getOrDefault(ThemeMode.System),
        dynamicColor = this[Keys.dynamic] ?: true,
        defaultSnoozeMinutes = this[Keys.snooze] ?: 9,
        cloudSyncEnabled = this[Keys.sync] ?: false,
    )
}
