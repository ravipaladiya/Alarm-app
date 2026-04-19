package com.alarmapp.feature.sleep

import android.text.format.DateFormat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alarmapp.core.designsystem.component.AlarmTopBar
import com.alarmapp.core.domain.model.SleepSession
import java.util.Date
import java.util.concurrent.TimeUnit

@Composable
fun SleepScreen(
    onBack: () -> Unit,
    viewModel: SleepViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val is24Hour = DateFormat.is24HourFormat(context)

    Scaffold(
        topBar = { AlarmTopBar(title = stringResource(R.string.sleep_title), onBack = onBack) },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(stringResource(R.string.sleep_bedtime), style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = viewModel::startBedtimeNow) {
                    Text(stringResource(R.string.sleep_start))
                }
                OutlinedButton(onClick = viewModel::endCurrent) {
                    Text(stringResource(R.string.sleep_end))
                }
            }

            Text(stringResource(R.string.sleep_history), style = MaterialTheme.typography.titleMedium)

            if (state.sessions.isEmpty()) {
                Text(
                    stringResource(R.string.sleep_history_empty),
                    style = MaterialTheme.typography.bodyMedium,
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(state.sessions, key = { it.id }) { session ->
                        SleepRow(session = session, is24Hour = is24Hour)
                    }
                }
            }
        }
    }
}

@Composable
private fun SleepRow(session: SleepSession, is24Hour: Boolean) {
    val pattern = if (is24Hour) "EEE d MMM · HH:mm" else "EEE d MMM · h:mm a"
    val start = DateFormat.format(pattern, Date(session.bedtimeEpochMs)).toString()
    val duration = session.durationMs?.let(::durationLabel)
    val subtitle = when {
        session.wakeEpochMs == null -> stringResource(R.string.sleep_in_progress)
        duration != null -> duration
        else -> ""
    }
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(start, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

private fun durationLabel(ms: Long): String {
    if (ms <= 0) return ""
    val hours = TimeUnit.MILLISECONDS.toHours(ms)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(ms) % 60
    return "%dh %02dm".format(hours, minutes)
}
