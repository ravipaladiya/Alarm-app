package com.alarmapp.feature.challenges

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlin.math.sqrt

@Composable
fun ShakeChallenge(
    count: Int,
    onComplete: () -> Unit,
    onProgress: () -> Unit,
) {
    val context = LocalContext.current
    var shakes by remember { mutableIntStateOf(0) }

    DisposableEffect(Unit) {
        val manager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accel = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        var lastEventMs = 0L
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val gX = event.values[0] / SensorManager.GRAVITY_EARTH
                val gY = event.values[1] / SensorManager.GRAVITY_EARTH
                val gZ = event.values[2] / SensorManager.GRAVITY_EARTH
                val gForce = sqrt(gX * gX + gY * gY + gZ * gZ)
                val now = System.currentTimeMillis()
                if (gForce > SHAKE_THRESHOLD_G && now - lastEventMs > DEBOUNCE_MS) {
                    lastEventMs = now
                    shakes += 1
                    if (shakes >= count) onComplete() else onProgress()
                }
            }
            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) = Unit
        }
        manager.registerListener(listener, accel, SensorManager.SENSOR_DELAY_GAME)
        onDispose { manager.unregisterListener(listener) }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.challenge_shake_title, count),
            style = MaterialTheme.typography.titleMedium,
        )
        LinearProgressIndicator(
            progress = { (shakes.toFloat() / count).coerceIn(0f, 1f) },
        )
        Text(text = stringResource(R.string.challenge_shake_progress, shakes, count))
    }
}

private const val SHAKE_THRESHOLD_G = 2.3f
private const val DEBOUNCE_MS = 250L
