package com.alarmapp.core.alarm.media

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.alarmapp.core.domain.model.Alarm
import com.alarmapp.core.domain.model.VibrationPattern
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VibrationPlayer @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val mgr = context.getSystemService(VibratorManager::class.java)
            mgr?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    fun start(alarm: Alarm) {
        val pattern = alarm.vibrationPattern.toPattern() ?: return
        val v = vibrator ?: return
        val effect = VibrationEffect.createWaveform(pattern, /* repeat = */ 0)
        v.vibrate(effect)
    }

    fun stop() {
        vibrator?.cancel()
    }

    private fun VibrationPattern.toPattern(): LongArray? = when (this) {
        VibrationPattern.None -> null
        VibrationPattern.Gentle -> longArrayOf(0, 300, 1200)
        VibrationPattern.Default -> longArrayOf(0, 500, 500)
        VibrationPattern.Strong -> longArrayOf(0, 800, 200, 800, 200)
    }
}
