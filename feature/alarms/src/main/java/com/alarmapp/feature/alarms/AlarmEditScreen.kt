package com.alarmapp.feature.alarms

import android.app.Activity
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.text.format.DateFormat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alarmapp.core.designsystem.component.AlarmTopBar
import com.alarmapp.core.domain.model.DismissChallenge
import com.alarmapp.core.domain.model.VibrationPattern

@Composable
fun AlarmEditScreen(
    onDone: () -> Unit,
    viewModel: AlarmEditViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val alarm = state.alarm
    val context = LocalContext.current
    val is24Hour = DateFormat.is24HourFormat(context)

    val timeState = rememberTimePickerState(
        initialHour = alarm.hour,
        initialMinute = alarm.minute,
        is24Hour = is24Hour,
    )
    LaunchedEffect(timeState.hour, timeState.minute) {
        viewModel.setTime(timeState.hour, timeState.minute)
    }

    val ringtonePicker = rememberLauncherForActivityResult(StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.getParcelableExtra<Uri>(
                RingtoneManager.EXTRA_RINGTONE_PICKED_URI,
            )
            viewModel.setSoundUri(uri?.toString())
        }
    }

    Scaffold(
        topBar = {
            AlarmTopBar(
                title = stringResource(
                    if (alarm.id == 0L) R.string.alarm_edit_title_new
                    else R.string.alarm_edit_title_edit,
                ),
                onBack = onDone,
                actions = {
                    if (alarm.id != 0L) {
                        IconButton(onClick = { viewModel.delete(onDone) }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = stringResource(R.string.alarm_edit_action_delete),
                            )
                        }
                    }
                    IconButton(onClick = { viewModel.save(onDone) }) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = stringResource(R.string.alarm_edit_action_save),
                        )
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
                label = { Text(stringResource(R.string.alarm_edit_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            Text(stringResource(R.string.alarm_edit_repeat))
            RepeatChips(
                mask = alarm.repeatDaysMask,
                onToggleDay = viewModel::toggleDay,
            )

            Text(stringResource(R.string.alarm_edit_challenge))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ChallengeChips(alarm.dismissChallenge) { viewModel.setChallenge(it) }
            }

            Text(stringResource(R.string.alarm_edit_sound))
            OutlinedButton(
                onClick = {
                    val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                        putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
                        putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                        putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
                        putExtra(
                            RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI,
                            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM),
                        )
                        alarm.soundUri?.let {
                            putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, it.toUri())
                        }
                    }
                    ringtonePicker.launch(intent)
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(soundLabel(alarm.soundUri))
            }

            Text(stringResource(R.string.alarm_edit_vibration))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                VibrationChips(alarm.vibrationPattern) { viewModel.setVibrationPattern(it) }
            }

            Text(stringResource(R.string.alarm_edit_volume, alarm.volumePercent))
            Slider(
                value = alarm.volumePercent.toFloat(),
                onValueChange = { viewModel.setVolumePercent(it.toInt()) },
                valueRange = 0f..100f,
            )

            Text(stringResource(R.string.alarm_edit_fade_in, alarm.fadeInSeconds))
            Slider(
                value = alarm.fadeInSeconds.toFloat(),
                onValueChange = { viewModel.setFadeIn(it.toInt()) },
                valueRange = 0f..60f,
            )

            Text(stringResource(R.string.alarm_edit_snooze, alarm.snoozeMinutes))
            Slider(
                value = alarm.snoozeMinutes.toFloat(),
                onValueChange = { viewModel.setSnoozeMinutes(it.toInt()) },
                valueRange = 1f..30f,
            )

            Button(
                onClick = { viewModel.save(onDone) },
                enabled = !state.saving,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    stringResource(
                        if (state.saving) R.string.alarm_edit_action_save_progress
                        else R.string.alarm_edit_action_save,
                    ),
                )
            }
        }
    }
}

@Composable
private fun RepeatChips(mask: Int, onToggleDay: (Int) -> Unit) {
    val dayShortIds = listOf(
        R.string.day_short_monday,
        R.string.day_short_tuesday,
        R.string.day_short_wednesday,
        R.string.day_short_thursday,
        R.string.day_short_friday,
        R.string.day_short_saturday,
        R.string.day_short_sunday,
    )
    val dayFullIds = listOf(
        R.string.day_monday,
        R.string.day_tuesday,
        R.string.day_wednesday,
        R.string.day_thursday,
        R.string.day_friday,
        R.string.day_saturday,
        R.string.day_sunday,
    )
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        dayShortIds.forEachIndexed { i, shortId ->
            val fullName = stringResource(dayFullIds[i])
            FilterChip(
                selected = (mask shr i) and 1 == 1,
                onClick = { onToggleDay(i) },
                label = { Text(stringResource(shortId)) },
                modifier = Modifier.semantics { contentDescription = fullName },
            )
        }
    }
}

@Composable
private fun ChallengeChips(
    selected: DismissChallenge,
    onSelect: (DismissChallenge) -> Unit,
) {
    val options = listOf(
        R.string.alarm_edit_challenge_none to DismissChallenge.None,
        R.string.alarm_edit_challenge_math to DismissChallenge.Math(difficulty = 1),
        R.string.alarm_edit_challenge_shake to DismissChallenge.Shake(count = 20),
        R.string.alarm_edit_challenge_qr to DismissChallenge.Qr(payload = ""),
        R.string.alarm_edit_challenge_typing to DismissChallenge.Typing(text = "wake up"),
    )
    options.forEach { (labelRes, challenge) ->
        FilterChip(
            selected = selected::class == challenge::class,
            onClick = { onSelect(challenge) },
            label = { Text(stringResource(labelRes)) },
        )
    }
}

@Composable
private fun VibrationChips(
    selected: VibrationPattern,
    onSelect: (VibrationPattern) -> Unit,
) {
    val options = listOf(
        R.string.alarm_edit_vibration_none to VibrationPattern.None,
        R.string.alarm_edit_vibration_gentle to VibrationPattern.Gentle,
        R.string.alarm_edit_vibration_default to VibrationPattern.Default,
        R.string.alarm_edit_vibration_strong to VibrationPattern.Strong,
    )
    options.forEach { (labelRes, pattern) ->
        FilterChip(
            selected = selected == pattern,
            onClick = { onSelect(pattern) },
            label = { Text(stringResource(labelRes)) },
        )
    }
}

@Composable
private fun soundLabel(uriString: String?): String {
    val context = LocalContext.current
    val fallback = stringResource(R.string.alarm_edit_sound_default)
    return remember(uriString) {
        if (uriString == null) return@remember fallback
        runCatching {
            RingtoneManager.getRingtone(context, uriString.toUri())?.getTitle(context)
        }.getOrNull() ?: fallback
    }
}
