package com.alarmapp.feature.challenges

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

/**
 * Placeholder QR challenge.
 *
 * Production implementation should:
 *  - Request CAMERA permission at runtime (Compose `accompanist-permissions`).
 *  - Host a `PreviewView` via `AndroidView` bound to `ProcessCameraProvider`.
 *  - Run ML Kit `BarcodeScanning` against each `ImageProxy`.
 *  - Call [onComplete] when a scan matches [expected], otherwise surface guidance.
 *
 * This placeholder is wired in so the `:feature:ringing` flow compiles; the user
 * can dismiss by scanning in the production build once CameraX is added.
 */
@Composable
fun QrChallenge(
    expected: String,
    @Suppress("UNUSED_PARAMETER") onComplete: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = "Scan the configured QR to dismiss.", style = MaterialTheme.typography.titleMedium)
        Text(text = "Expected: $expected", style = MaterialTheme.typography.bodyMedium)
        Text(
            text = "(Camera preview not yet implemented.)",
            style = MaterialTheme.typography.labelMedium,
        )
    }
}
