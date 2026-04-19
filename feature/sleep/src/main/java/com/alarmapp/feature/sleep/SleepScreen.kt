package com.alarmapp.feature.sleep

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.alarmapp.core.designsystem.component.AlarmTopBar

@Composable
fun SleepScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            AlarmTopBar(title = stringResource(R.string.sleep_title), onBack = onBack)
        },
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(stringResource(R.string.sleep_bedtime), style = MaterialTheme.typography.titleMedium)
            Text(
                text = stringResource(R.string.sleep_placeholder),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
