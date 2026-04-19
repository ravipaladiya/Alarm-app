package com.alarmapp.feature.alarms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alarmapp.core.designsystem.component.AlarmTopBar
import com.alarmapp.core.domain.model.DismissChallenge

@Composable
fun AlarmEditScreen(
    onDone: () -> Unit,
    viewModel: AlarmEditViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val alarm = state.alarm
    val timeState = rememberTimePickerState(
        initialHour = alarm.hour,
        initialMinute = alarm.minute,
        is24Hour = true,
    )
    LaunchedEffect(timeState.hour, timeState.minute) {
        viewModel.setTime(timeState.hour, timeState.minute)
    }

    Scaffold(
        topBar = {
            AlarmTopBar(
                title = if (alarm.id == 0L) "New alarm" else "Edit alarm",
                onBack = onDone,
                actions = {
                    if (alarm.id != 0L) {
                        IconButton(onClick = { viewModel.delete(onDone) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                    IconButton(onClick = { viewModel.save(onDone) }) {
                        Icon(Icons.Default.Check, contentDescription = "Save")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            TimePicker(state = timeState)

            OutlinedTextField(
                value = alarm.label,
                onValueChange = viewModel::setLabel,
                label = { Text("Label") },
                modifier = Modifier.fillMaxWidth(),
            )

            Text("Repeat")
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("M", "T", "W", "T", "F", "S", "S").forEachIndexed { i, label ->
                    FilterChip(
                        selected = (alarm.repeatDaysMask shr i) and 1 == 1,
                        onClick = { viewModel.toggleDay(i) },
                        label = { Text(label) },
                    )
                }
            }

            Text("Dismiss challenge")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    "None" to DismissChallenge.None,
                    "Math" to DismissChallenge.Math(difficulty = 1),
                    "Shake" to DismissChallenge.Shake(count = 20),
                    "QR" to DismissChallenge.Qr(payload = ""),
                    "Typing" to DismissChallenge.Typing(text = "wake up"),
                ).forEach { (label, challenge) ->
                    FilterChip(
                        selected = alarm.dismissChallenge::class == challenge::class,
                        onClick = { viewModel.setChallenge(challenge) },
                        label = { Text(label) },
                    )
                }
            }

            Text("Volume: ${alarm.volumePercent}%")
            Slider(
                value = alarm.volumePercent.toFloat(),
                onValueChange = { viewModel.setVolumePercent(it.toInt()) },
                valueRange = 0f..100f,
            )

            Text("Fade-in: ${alarm.fadeInSeconds}s")
            Slider(
                value = alarm.fadeInSeconds.toFloat(),
                onValueChange = { viewModel.setFadeIn(it.toInt()) },
                valueRange = 0f..60f,
            )

            Text("Snooze: ${alarm.snoozeMinutes} min")
            Slider(
                value = alarm.snoozeMinutes.toFloat(),
                onValueChange = { viewModel.setSnoozeMinutes(it.toInt()) },
                valueRange = 1f..30f,
            )

            Button(onClick = { viewModel.save(onDone) }, enabled = !state.saving) {
                Text(if (state.saving) "Saving\u2026" else "Save")
            }
        }
    }
}
