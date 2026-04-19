package com.alarmapp.feature.ringing

import android.text.format.DateFormat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.alarmapp.feature.challenges.ChallengeHost
import kotlinx.coroutines.delay
import java.util.Date

@Composable
fun RingingScreen(
    state: RingingUiState,
    onChallengeProgress: (Boolean) -> Unit,
    onSnooze: () -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val is24Hour = DateFormat.is24HourFormat(context)
    val pattern = if (is24Hour) "HH:mm" else "h:mm a"
    var nowMs by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            nowMs = System.currentTimeMillis()
            delay(1_000)
        }
    }
    val currentTimeText = DateFormat.format(pattern, Date(nowMs)).toString()

    Box(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = currentTimeText,
                    style = MaterialTheme.typography.displayLarge,
                )
                Text(
                    text = state.alarm?.label?.ifBlank { stringResource(R.string.ringing_default_label) }
                        ?: stringResource(R.string.ringing_default_label),
                    style = MaterialTheme.typography.titleMedium,
                )
            }

            ChallengeHost(
                challenge = state.challenge,
                onComplete = { onChallengeProgress(true) },
                onProgress = { onChallengeProgress(false) },
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onSnooze, enabled = state.challengeComplete) {
                    Text(stringResource(R.string.ringing_snooze))
                }
                Button(onClick = onDismiss, enabled = state.challengeComplete) {
                    Text(stringResource(R.string.ringing_dismiss))
                }
            }
        }
    }
}
