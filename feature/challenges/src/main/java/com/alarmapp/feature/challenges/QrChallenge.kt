package com.alarmapp.feature.challenges

import android.Manifest
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import timber.log.Timber
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Camera-based dismiss challenge. Uses CameraX for the preview/analysis
 * pipeline and ML Kit's on-device barcode scanner to match the frame content
 * against the configured [expected] payload.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun QrChallenge(
    expected: String,
    onComplete: () -> Unit,
) {
    if (expected.isBlank()) {
        Text(
            text = stringResource(R.string.challenge_qr_unconfigured),
            style = MaterialTheme.typography.bodyMedium,
        )
        return
    }

    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = stringResource(R.string.challenge_qr_title),
            style = MaterialTheme.typography.titleMedium,
        )

        if (cameraPermission.status.isGranted) {
            var lastMismatch by remember { mutableStateOf(false) }
            QrScannerView(
                expected = expected,
                onMatch = onComplete,
                onMismatch = { lastMismatch = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
            )
            if (lastMismatch) {
                Text(
                    text = stringResource(R.string.challenge_qr_wrong_code),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        } else {
            Text(
                text = stringResource(R.string.challenge_qr_permission_denied),
                style = MaterialTheme.typography.bodyMedium,
            )
            Button(onClick = { cameraPermission.launchPermissionRequest() }) {
                Text(stringResource(R.string.challenge_qr_grant_camera))
            }
        }
    }
}

@Composable
private fun QrScannerView(
    expected: String,
    onMatch: () -> Unit,
    onMismatch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val analyzerExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }
    val scanner: BarcodeScanner = remember {
        BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE, Barcode.FORMAT_ALL_FORMATS)
                .build(),
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            analyzerExecutor.shutdown()
            scanner.close()
        }
    }

    var matched by remember { mutableStateOf(false) }
    LaunchedEffect(matched) { if (matched) onMatch() }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get() ?: return@addListener
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    val analysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                    analysis.setAnalyzer(analyzerExecutor) { imageProxy ->
                        val mediaImage = imageProxy.image
                        if (mediaImage == null || matched) {
                            imageProxy.close()
                            return@setAnalyzer
                        }
                        val inputImage = InputImage.fromMediaImage(
                            mediaImage,
                            imageProxy.imageInfo.rotationDegrees,
                        )
                        scanner.process(inputImage)
                            .addOnSuccessListener { barcodes ->
                                val payloads = barcodes.mapNotNull { it.rawValue }
                                when {
                                    payloads.isEmpty() -> Unit
                                    payloads.any { it == expected } -> matched = true
                                    else -> onMismatch()
                                }
                            }
                            .addOnFailureListener { Timber.w(it, "Barcode scan failed") }
                            .addOnCompleteListener { imageProxy.close() }
                    }
                    runCatching {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            analysis,
                        )
                    }.onFailure { Timber.e(it, "Failed to bind camera") }
                }, ContextCompat.getMainExecutor(ctx))
                previewView
            },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
