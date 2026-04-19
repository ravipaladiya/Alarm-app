package com.alarmapp.feature.ringing

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alarmapp.core.alarm.service.AlarmService
import com.alarmapp.core.designsystem.theme.AlarmAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RingingActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val alarmId = intent.getLongExtra(AlarmService.EXTRA_ALARM_ID, -1L)
        setContent {
            AlarmAppTheme {
                val vm: RingingViewModel = hiltViewModel()
                val state by vm.state.collectAsStateWithLifecycle()
                RingingScreen(
                    state = state,
                    onChallengeProgress = vm::onChallengeProgress,
                    onSnooze = {
                        sendCommand(AlarmService.ACTION_SNOOZE, alarmId)
                        finish()
                    },
                    onDismiss = {
                        sendCommand(AlarmService.ACTION_DISMISS, alarmId)
                        finish()
                    },
                )
                vm.bindAlarm(alarmId)
            }
        }
    }

    private fun sendCommand(action: String, alarmId: Long) {
        startService(
            Intent(this, AlarmService::class.java)
                .setAction(action)
                .putExtra(AlarmService.EXTRA_ALARM_ID, alarmId),
        )
    }
}
