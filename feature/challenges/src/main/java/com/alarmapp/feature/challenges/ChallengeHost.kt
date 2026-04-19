package com.alarmapp.feature.challenges

import androidx.compose.runtime.Composable
import com.alarmapp.core.domain.model.DismissChallenge

/**
 * Renders the UI for a dismiss challenge.
 *
 * When the user completes the challenge, [onComplete] is called; while they are
 * partway through, the handler may emit [onProgress] updates which the parent can
 * use to keep dismiss disabled.
 *
 * To add a new challenge type:
 *  1. Add a `data class` to [DismissChallenge] in `:core:domain`.
 *  2. Add a branch below that renders the handler composable.
 *  3. Create a `*Challenge` composable in this module.
 *  4. Update the challenge picker in `feature:alarms/AlarmEditScreen`.
 */
@Composable
fun ChallengeHost(
    challenge: DismissChallenge,
    onComplete: () -> Unit,
    onProgress: () -> Unit,
) {
    when (challenge) {
        is DismissChallenge.None -> onComplete()
        is DismissChallenge.Math -> MathChallenge(difficulty = challenge.difficulty, onComplete = onComplete)
        is DismissChallenge.Shake -> ShakeChallenge(count = challenge.count, onComplete = onComplete, onProgress = onProgress)
        is DismissChallenge.Typing -> TypingChallenge(text = challenge.text, onComplete = onComplete)
        is DismissChallenge.Qr -> QrChallenge(expected = challenge.payload, onComplete = onComplete)
    }
}
