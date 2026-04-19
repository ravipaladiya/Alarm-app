package com.alarmapp.feature.permissions

import android.Manifest
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alarmapp.core.designsystem.component.AlarmTopBar
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionsScreen(
    onBack: () -> Unit,
    viewModel: PermissionsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Re-read permission state whenever the user comes back from system Settings.
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refresh()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val postNotificationsState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS) {
            viewModel.refresh()
        }
    } else {
        null
    }

    Scaffold(
        topBar = {
            AlarmTopBar(
                title = stringResource(R.string.permissions_title),
                onBack = onBack,
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (!state.allRequiredGranted) {
                Text(
                    text = stringResource(R.string.permissions_intro_warning),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            } else {
                Text(
                    text = stringResource(R.string.permissions_intro_ok),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            state.entries.forEach { entry ->
                PermissionCard(
                    entry = entry,
                    onFix = {
                        when (entry.permission) {
                            AppPermission.Notifications -> {
                                val granted = postNotificationsState?.status?.isGranted == true
                                if (postNotificationsState == null || granted) {
                                    context.startActivity(PermissionIntents.appDetails(context))
                                } else {
                                    postNotificationsState.launchPermissionRequest()
                                }
                            }
                            AppPermission.ExactAlarms ->
                                context.startActivity(PermissionIntents.exactAlarms(context))
                            AppPermission.FullScreenIntent ->
                                context.startActivity(PermissionIntents.fullScreenIntent(context))
                            AppPermission.BatteryOptimizations ->
                                context.startActivity(PermissionIntents.batteryOptimizations(context))
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun PermissionCard(entry: PermissionEntry, onFix: () -> Unit) {
    val (title, description) = labelFor(entry.permission)
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StatusIcon(entry.state)
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                )
                if (!entry.permission.required) {
                    Text(
                        text = stringResource(R.string.permissions_optional),
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
            if (entry.state == PermissionState.Denied) {
                Button(onClick = onFix) { Text(stringResource(R.string.permissions_grant)) }
            } else if (entry.state == PermissionState.Unknown) {
                OutlinedButton(onClick = onFix) { Text(stringResource(R.string.permissions_check)) }
            }
        }
    }
}

@Composable
private fun StatusIcon(state: PermissionState) {
    val description = when (state) {
        PermissionState.Granted -> stringResource(R.string.permissions_status_granted)
        PermissionState.Denied -> stringResource(R.string.permissions_status_denied)
        PermissionState.NotApplicable -> stringResource(R.string.permissions_status_not_applicable)
        PermissionState.Unknown -> stringResource(R.string.permissions_status_unknown)
    }
    val semantics = Modifier.semantics { contentDescription = description }
    when (state) {
        PermissionState.Granted, PermissionState.NotApplicable ->
            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = semantics, tint = Color(0xFF4CAF50))
        PermissionState.Denied ->
            Icon(Icons.Default.ErrorOutline, contentDescription = null, modifier = semantics, tint = MaterialTheme.colorScheme.error)
        PermissionState.Unknown ->
            Icon(Icons.Outlined.RadioButtonUnchecked, contentDescription = null, modifier = semantics)
    }
}

@Composable
private fun labelFor(permission: AppPermission): Pair<String, String> = when (permission) {
    AppPermission.Notifications -> stringResource(R.string.permissions_notifications_title) to
        stringResource(R.string.permissions_notifications_description)
    AppPermission.ExactAlarms -> stringResource(R.string.permissions_exact_alarms_title) to
        stringResource(R.string.permissions_exact_alarms_description)
    AppPermission.FullScreenIntent -> stringResource(R.string.permissions_full_screen_intent_title) to
        stringResource(R.string.permissions_full_screen_intent_description)
    AppPermission.BatteryOptimizations -> stringResource(R.string.permissions_battery_title) to
        stringResource(R.string.permissions_battery_description)
}
