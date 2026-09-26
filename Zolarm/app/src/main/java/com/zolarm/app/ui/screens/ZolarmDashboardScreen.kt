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
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.zolarm.app.core.RepeatDays
import com.zolarm.app.core.ZolarmPermissions
import com.zolarm.app.data.AlarmEntity
import com.zolarm.app.data.ChallengeType
import com.zolarm.app.ui.components.ZolarmLogo
import com.zolarm.app.ui.theme.NeonCyan
import com.zolarm.app.ui.theme.NeonLime
import com.zolarm.app.ui.theme.NeonViolet

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
    var permissionRefreshKey by remember { mutableStateOf(0) }
    val isConfigured = remember(permissionRefreshKey) { ZolarmPermissions.isFullyConfigured(context) }

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
                            text = "Zolarm",
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onBackground
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
            ) { Icon(Icons.Filled.Add, contentDescription = "Add reminder") }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (!isConfigured) {
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
                    text = "ACTIVE REMINDERS",
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
}

@Composable
private fun AlarmCard(
    alarm: AlarmEntity,
    onToggle: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
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
                    text = alarm.label.ifBlank { "Reminder" },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ChallengeChip(alarm.challengeType, alarm.stepGoal)
                    RepeatChip(RepeatDays.toDisplayString(alarm.repeatDays))
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
                        Icon(Icons.Filled.Edit, contentDescription = "Edit reminder",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete reminder",
                            tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
private fun ChallengeChip(type: ChallengeType, stepGoal: Int) {
    val icon = if (type == ChallengeType.STEPS) Icons.Filled.DirectionsWalk else Icons.Filled.CameraAlt
    val text = if (type == ChallengeType.STEPS) stepGoal.toString() + " steps" else "Photo"
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
            text = text.uppercase(),
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
            Icon(Icons.Filled.Alarm, contentDescription = null, tint = NeonCyan,
                modifier = Modifier.size(38.dp))
        }
        Spacer(Modifier.height(18.dp))
        Text("No reminders yet", style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Create your first hardcore alarm and let Zolarm make sure you actually get up.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(18.dp))
        Button(onClick = onAddAlarm) { Text("Create reminder") }
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
        if (!ZolarmPermissions.canDrawOverlays(context)) add("Draw over other apps")
        if (!ZolarmPermissions.canScheduleExactAlarms(context)) add("Exact alarms")
        if (!ZolarmPermissions.isIgnoringBatteryOptimizations(context)) add("Battery optimisation")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !ZolarmPermissions.hasRuntime(context, Manifest.permission.POST_NOTIFICATIONS)
        ) add("Notifications")
        if (!ZolarmPermissions.hasRuntime(context, Manifest.permission.CAMERA)) add("Camera")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
            !ZolarmPermissions.hasRuntime(context, Manifest.permission.ACTIVITY_RECOGNITION)
        ) add("Physical activity")
        if (!ZolarmPermissions.canBypassDnd(context)) add("Do Not Disturb access")
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
                Icon(Icons.Filled.Warning, contentDescription = null,
                    tint = MaterialTheme.colorScheme.error)
                Spacer(Modifier.width(8.dp))
                Text("Zolarm needs full power", style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Missing: " + missing.joinToString(", "),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onGrantRuntime, modifier = Modifier.weight(1f)) { Text("Runtime") }
                Button(onClick = onOpenOverlaySettings, modifier = Modifier.weight(1f)) { Text("Overlay") }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onOpenExactAlarmSettings, modifier = Modifier.weight(1f)) { Text("Exact") }
                Button(onClick = onOpenBatterySettings, modifier = Modifier.weight(1f)) { Text("Battery") }
            }
            Spacer(Modifier.height(8.dp))
            Button(onClick = onOpenDndSettings, modifier = Modifier.fillMaxWidth()) {
                Text("Allow alarm to bypass Silent / DND")
            }
        }
    }
}
