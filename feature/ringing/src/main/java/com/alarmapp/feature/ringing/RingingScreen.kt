package com.alarmapp.feature.ringing

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alarmapp.feature.challenges.ChallengeHost

@Composable
fun RingingScreen(
    state: RingingUiState,
    onChallengeProgress: (Boolean) -> Unit,
    onSnooze: () -> Unit,
    onDismiss: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = state.alarm?.let { "%02d:%02d".format(it.hour, it.minute) } ?: "--:--",
                    style = MaterialTheme.typography.displayLarge,
                )
                Text(
                    text = state.alarm?.label?.ifBlank { "Alarm" } ?: "Alarm",
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
                    Text("Snooze")
                }
                Button(onClick = onDismiss, enabled = state.challengeComplete) {
                    Text("Dismiss")
                }
            }
        }
    }
}
