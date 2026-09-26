package com.zolarm.app.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.zolarm.app.BuildConfig
import com.zolarm.app.R
import com.zolarm.app.core.RepeatDays
import com.zolarm.app.core.UpdateChecker
import com.zolarm.app.core.ZolarmPermissions
import com.zolarm.app.data.AlarmEntity
import com.zolarm.app.data.ChallengeType
import com.zolarm.app.ui.components.ZolarmLogo
import com.zolarm.app.ui.theme.NeonCyan
import com.zolarm.app.ui.theme.NeonLime
import com.zolarm.app.ui.theme.NeonViolet
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZolarmDashboardScreen(
    alarms: List<AlarmEntity>,
    onToggleAlarm: (AlarmEntity, Boolean) -> Unit,
    onEditAlarm: (AlarmEntity) -> Unit,
    onDeleteAlarm: (AlarmEntity) -> Unit,
    onAddAlarm: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var permissionRefreshKey by remember { mutableStateOf(0) }
    val isConfigured = remember(permissionRefreshKey) { ZolarmPermissions.isFullyConfigured(context) }

    var menuExpanded by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }
    var showPermissionsCard by remember { mutableStateOf(false) }
    var updateChecking by remember { mutableStateOf(false) }
    var updateResult by remember { mutableStateOf<UpdateChecker.Result?>(null) }

    val runtimeLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionRefreshKey++ }

    val settingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { permissionRefreshKey++ }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ZolarmLogo(modifier = Modifier.size(28.dp))
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = stringResource(R.string.dashboard_title),
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = null)
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.menu_about)) },
                            onClick = {
                                menuExpanded = false
                                showAbout = true
                            },
                            leadingIcon = { Icon(Icons.Filled.Info, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.menu_check_update)) },
                            onClick = {
                                menuExpanded = false
                                updateChecking = true
                                updateResult = null
                                scope.launch {
                                    val result = UpdateChecker.check()
                                    updateChecking = false
                                    updateResult = result
                                }
                            },
                            leadingIcon = { Icon(Icons.Filled.SystemUpdate, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.menu_permissions)) },
                            onClick = {
                                menuExpanded = false
                                showPermissionsCard = true
                                permissionRefreshKey++
                            },
                            leadingIcon = { Icon(Icons.Filled.Security, contentDescription = null) }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddAlarm,
                containerColor = NeonCyan,
                contentColor = MaterialTheme.colorScheme.background,
                shape = CircleShape
            ) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_reminder))
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (!isConfigured || showPermissionsCard) {
                item {
                    PermissionSetupCard(
                        onGrantRuntime = { runtimeLauncher.launch(ZolarmPermissions.runtimePermissions) },
                        onOpenOverlaySettings = { settingsLauncher.launch(ZolarmPermissions.overlaySettingsIntent(context)) },
                        onOpenExactAlarmSettings = { settingsLauncher.launch(ZolarmPermissions.exactAlarmSettingsIntent(context)) },
                        onOpenBatterySettings = {
                            runCatching { settingsLauncher.launch(ZolarmPermissions.batteryOptimizationIntent(context)) }
                                .onFailure { settingsLauncher.launch(ZolarmPermissions.appDetailsIntent(context)) }
                        },
                        onOpenDndSettings = { settingsLauncher.launch(ZolarmPermissions.notificationPolicyIntent()) }
                    )
                }
            }

            item {
                Text(
                    text = stringResource(R.string.active_reminders),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            }

            if (alarms.isEmpty()) {
                item { EmptyState(onAddAlarm = onAddAlarm) }
            } else {
                items(items = alarms, key = { it.id }) { alarm ->
                    AlarmCard(
                        alarm = alarm,
                        onToggle = { enabled -> onToggleAlarm(alarm, enabled) },
                        onEdit = { onEditAlarm(alarm) },
                        onDelete = { onDeleteAlarm(alarm) }
                    )
                }
            }

            item { Spacer(Modifier.height(84.dp)) }
        }
    }

    if (showAbout) {
        AlertDialog(
            onDismissRequest = { showAbout = false },
            title = { Text(stringResource(R.string.about_title)) },
            text = {
                Text(stringResource(R.string.about_body, BuildConfig.VERSION_NAME))
            },
            confirmButton = {
                TextButton(onClick = { showAbout = false }) {
                    Text(stringResource(R.string.about_ok))
                }
            }
        )
    }

    if (updateChecking) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text(stringResource(R.string.menu_check_update)) },
            text = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 3.dp)
                    Spacer(Modifier.width(16.dp))
                    Text(stringResource(R.string.update_checking))
                }
            },
            confirmButton = {}
        )
    }

    updateResult?.let { result ->
        val body = when {
            result.error != null && result.latestVersion == null ->
                stringResource(R.string.update_failed)
            result.isLatest ->
                stringResource(R.string.update_latest, result.currentVersion)
            else ->
                stringResource(
                    R.string.update_available,
                    result.latestVersion ?: "?",
                    result.currentVersion
                )
        }
        AlertDialog(
            onDismissRequest = { updateResult = null },
            title = { Text(stringResource(R.string.menu_check_update)) },
            text = { Text(body) },
            confirmButton = {
                if (!result.isLatest) {
                    TextButton(onClick = {
                        UpdateChecker.openReleases(context, result.releaseUrl)
                        updateResult = null
                    }) {
                        Text(stringResource(R.string.update_open))
                    }
                } else {
                    TextButton(onClick = { updateResult = null }) {
                        Text(stringResource(R.string.about_ok))
                    }
                }
            },
            dismissButton = {
                if (!result.isLatest) {
                    TextButton(onClick = { updateResult = null }) {
                        Text(stringResource(R.string.update_cancel))
                    }
                }
            }
        )
    }
}

@Composable
private fun AlarmCard(
    alarm: AlarmEntity,
    onToggle: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = alarm.timeText,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Black,
                    color = if (alarm.enabled) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = alarm.label.ifBlank { stringResource(R.string.reminder_default) },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ChallengeChip(alarm.challengeType, alarm.stepGoal)
                    RepeatChip(RepeatDays.toDisplayString(context, alarm.repeatDays))
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Switch(
                    checked = alarm.enabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.background,
                        checkedTrackColor = NeonCyan,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    )
                )
                Spacer(Modifier.height(4.dp))
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(
                            Icons.Filled.Edit,
                            contentDescription = stringResource(R.string.edit),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = stringResource(R.string.delete),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChallengeChip(type: ChallengeType, stepGoal: Int) {
    val icon = if (type == ChallengeType.STEPS) Icons.Filled.DirectionsWalk else Icons.Filled.CameraAlt
    val text = if (type == ChallengeType.STEPS) stringResource(R.string.steps_label, stepGoal)
    else stringResource(R.string.photo_label)
    val tint = if (type == ChallengeType.STEPS) NeonLime else NeonViolet
    Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.surfaceContainerHigh) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(5.dp))
            Text(text = text, style = MaterialTheme.typography.labelSmall, color = tint)
        }
    }
}

@Composable
private fun RepeatChip(text: String) {
    Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.surfaceContainerHigh) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

@Composable
private fun EmptyState(onAddAlarm: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(84.dp)
                .background(MaterialTheme.colorScheme.surfaceContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.Alarm, contentDescription = null, tint = NeonCyan,
                modifier = Modifier.size(38.dp)
            )
        }
        Spacer(Modifier.height(18.dp))
        Text(
            stringResource(R.string.no_reminders),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.no_reminders_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(18.dp))
        Button(onClick = onAddAlarm) { Text(stringResource(R.string.create_reminder)) }
    }
}

@Composable
private fun PermissionSetupCard(
    onGrantRuntime: () -> Unit,
    onOpenOverlaySettings: () -> Unit,
    onOpenExactAlarmSettings: () -> Unit,
    onOpenBatterySettings: () -> Unit,
    onOpenDndSettings: () -> Unit
) {
    val context = LocalContext.current
    val missing = buildList {
        if (!ZolarmPermissions.canDrawOverlays(context)) add("فوق التطبيقات")
        if (!ZolarmPermissions.canScheduleExactAlarms(context)) add("منبّه دقيق")
        if (!ZolarmPermissions.isIgnoringBatteryOptimizations(context)) add("تحسين البطارية")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !ZolarmPermissions.hasRuntime(context, Manifest.permission.POST_NOTIFICATIONS)
        ) add("الإشعارات")
        if (!ZolarmPermissions.hasRuntime(context, Manifest.permission.CAMERA)) add("الكاميرا")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
            !ZolarmPermissions.hasRuntime(context, Manifest.permission.ACTIVITY_RECOGNITION)
        ) add("النشاط البدني")
        if (!ZolarmPermissions.canBypassDnd(context)) add("عدم الإزعاج")
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Warning, contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    stringResource(R.string.permission_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.permission_missing, missing.joinToString("، ")),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onGrantRuntime, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.permission_runtime))
                }
                Button(onClick = onOpenOverlaySettings, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.permission_overlay))
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onOpenExactAlarmSettings, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.permission_exact))
                }
                Button(onClick = onOpenBatterySettings, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.permission_battery))
                }
            }
            Spacer(Modifier.height(8.dp))
            Button(onClick = onOpenDndSettings, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.permission_dnd))
            }
        }
    }
}
