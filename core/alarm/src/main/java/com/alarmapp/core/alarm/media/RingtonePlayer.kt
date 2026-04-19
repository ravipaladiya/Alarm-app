package com.alarmapp.core.alarm.media

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import com.alarmapp.core.common.ApplicationScope
import com.alarmapp.core.domain.model.Alarm
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RingtonePlayer @Inject constructor(
    @ApplicationContext private val context: Context,
    @ApplicationScope private val scope: CoroutineScope,
) {

    private var player: MediaPlayer? = null
    private var fadeJob: Job? = null

    fun start(alarm: Alarm) {
        stop()
        val uri = alarm.soundUri?.toUriOrNull()
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: return

        val targetVolume = (alarm.volumePercent.coerceIn(0, 100)) / 100f
        val fadeSeconds = alarm.fadeInSeconds.coerceAtLeast(0)

        player = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build(),
            )
            setDataSource(context, uri)
            isLooping = true
            setVolume(if (fadeSeconds == 0) targetVolume else 0f, if (fadeSeconds == 0) targetVolume else 0f)
            prepare()
            start()
        }

        if (fadeSeconds > 0) {
            fadeJob = scope.launch {
                val steps = 20
                val stepDelay = (fadeSeconds * 1000L) / steps
                for (i in 1..steps) {
                    val v = targetVolume * i / steps
                    player?.setVolume(v, v)
                    delay(stepDelay)
                }
            }
        }
    }

    fun stop() {
        fadeJob?.cancel()
        fadeJob = null
        runCatching {
            player?.apply {
                if (isPlaying) stop()
                reset()
                release()
            }
        }.onFailure { Timber.w(it, "Error stopping ringtone player") }
        player = null
    }

    private fun String.toUriOrNull(): Uri? = runCatching { Uri.parse(this) }.getOrNull()
}
