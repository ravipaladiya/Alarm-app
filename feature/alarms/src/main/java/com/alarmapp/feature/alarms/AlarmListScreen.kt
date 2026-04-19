package com.alarmapp.feature.alarms

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AlarmOff
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alarmapp.core.designsystem.component.AlarmTopBar
import com.alarmapp.core.domain.model.Alarm
import com.alarmapp.feature.permissions.PermissionBanner

@Composable
fun AlarmListScreen(
    onAddAlarm: () -> Unit,
    onEditAlarm: (Long) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenSleep: () -> Unit,
    onOpenPermissions: () -> Unit,
    viewModel: AlarmListViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            AlarmTopBar(
                title = stringResource(R.string.alarm_list_title),
                actions = {
                    IconButton(onClick = onOpenSleep) {
                        Icon(
                            Icons.Default.Bedtime,
                            contentDescription = stringResource(R.string.alarm_list_open_sleep),
                        )
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = stringResource(R.string.alarm_list_open_settings),
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddAlarm,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.alarm_list_add)) },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PermissionBanner(onOpenPermissions = onOpenPermissions)

            if (state.alarms.isEmpty()) {
                EmptyState(onAddAlarm = onAddAlarm)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(state.alarms, key = { it.id }) { alarm ->
                        AlarmRow(
                            alarm = alarm,
                            is24Hour = android.text.format.DateFormat.is24HourFormat(context),
                            onToggle = { viewModel.setEnabled(alarm.id, it) },
                            onClick = { onEditAlarm(alarm.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun EmptyState(onAddAlarm: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.AlarmOff,
                contentDescription = null,
                modifier = Modifier.padding(bottom = 16.dp),
            )
            Text(
                stringResource(R.string.alarm_list_empty_title),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                stringResource(R.string.alarm_list_empty_body),
                style = MaterialTheme.typography.bodyMedium,
            )
            ExtendedFloatingActionButton(
                onClick = onAddAlarm,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.alarm_list_add)) },
                modifier = Modifier.padding(top = 16.dp),
            )
        }
    }
}

@Composable
private fun AlarmRow(
    alarm: Alarm,
    is24Hour: Boolean,
    onToggle: (Boolean) -> Unit,
    onClick: () -> Unit,
) {
    val timeText = formatTime(alarm.hour, alarm.minute, is24Hour)
    val repeatText = repeatLabel(alarm.repeatDaysMask)
    val label = alarm.label.ifBlank { repeatText }
    val rowDescription = stringResource(
        if (alarm.enabled) R.string.alarm_row_description_enabled
        else R.string.alarm_row_description_disabled,
        timeText,
        label,
    )
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = rowDescription }
            .pointerInput(alarm.id) { detectTapGestures(onTap = { onClick() }) },
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = timeText,
                    style = MaterialTheme.typography.displayLarge,
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Switch(checked = alarm.enabled, onCheckedChange = onToggle)
        }
    }
}

@Composable
private fun repeatLabel(mask: Int): String {
    if (mask == 0) return stringResource(R.string.alarm_list_repeat_once)
    val days = listOf(
        R.string.day_short_monday,
        R.string.day_short_tuesday,
        R.string.day_short_wednesday,
        R.string.day_short_thursday,
        R.string.day_short_friday,
        R.string.day_short_saturday,
        R.string.day_short_sunday,
    )
    return days
        .mapIndexedNotNull { i, res -> if ((mask shr i) and 1 == 1) stringResource(res) else null }
        .joinToString(" ")
}

internal fun formatTime(hour: Int, minute: Int, is24Hour: Boolean): String =
    if (is24Hour) {
        "%02d:%02d".format(hour, minute)
    } else {
        val h12 = ((hour + 11) % 12) + 1
        val suffix = if (hour < 12) "AM" else "PM"
        "%d:%02d %s".format(h12, minute, suffix)
    }
