package com.zolarm.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zolarm.app.core.RepeatDays
import com.zolarm.app.data.AlarmEntity
import com.zolarm.app.data.ChallengeType
import com.zolarm.app.ui.theme.NeonCyan
import com.zolarm.app.ui.theme.NeonLime
import com.zolarm.app.ui.theme.NeonViolet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditAlarmScreen(
    initialAlarm: AlarmEntity?,
    onSave: (AlarmEntity) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isEditing = initialAlarm != null
    var label by remember { mutableStateOf(initialAlarm?.label ?: "Wake up") }
    var challengeType by remember {
        mutableStateOf(initialAlarm?.challengeType ?: ChallengeType.STEPS)
    }
    var stepGoal by remember { mutableIntStateOf(initialAlarm?.stepGoal ?: 10) }
    var repeatMask by remember { mutableIntStateOf(initialAlarm?.repeatDays ?: 0) }

    val timePickerState = rememberTimePickerState(
        initialHour = initialAlarm?.hour ?: 7,
        initialMinute = initialAlarm?.minute ?: 0,
        is24Hour = true
    )

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditing) "Edit reminder" else "New reminder",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    TimePicker(
                        state = timePickerState,
                        colors = TimePickerDefaults.colors(
                            selectorColor = NeonCyan,
                            timeSelectorSelectedContainerColor = NeonCyan,
                            timeSelectorSelectedContentColor = MaterialTheme.colorScheme.background,
                            timeSelectorUnselectedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            timeSelectorUnselectedContentColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }

            OutlinedTextField(
                value = label,
                onValueChange = { label = it.take(48) },
                label = { Text("Task label") },
                placeholder = { Text("e.g. Gym, Study, Stand up") },
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                modifier = Modifier.fillMaxWidth()
            )

            SectionTitle("Dismissal challenge")

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = challengeType == ChallengeType.STEPS,
                    onClick = { challengeType = ChallengeType.STEPS },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = NeonLime,
                        activeContentColor = MaterialTheme.colorScheme.background,
                        inactiveContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Icon(Icons.Filled.DirectionsWalk, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Steps")
                }
                SegmentedButton(
                    selected = challengeType == ChallengeType.CAMERA,
                    onClick = { challengeType = ChallengeType.CAMERA },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = NeonViolet,
                        activeContentColor = MaterialTheme.colorScheme.background,
                        inactiveContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Icon(Icons.Filled.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Camera")
                }
            }

            if (challengeType == ChallengeType.STEPS) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Required steps", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                text = stepGoal.toString(),
                                style = MaterialTheme.typography.titleMedium,
                                color = NeonLime,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Slider(
                            value = stepGoal.toFloat(),
                            onValueChange = { stepGoal = it.toInt().coerceIn(5, 100) },
                            valueRange = 5f..100f,
                            steps = 18,
                            colors = SliderDefaults.colors(
                                thumbColor = NeonLime,
                                activeTrackColor = NeonLime,
                                inactiveTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                            )
                        )
                        Text(
                            text = "Zolarm will not let you dismiss until you physically walk these steps.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            SectionTitle("Repeat")

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RepeatDays.shortLabels.forEachIndexed { index, shortLabel ->
                    val selected = RepeatDays.isOn(repeatMask, index)
                    FilterChip(
                        selected = selected,
                        onClick = { repeatMask = RepeatDays.toggle(repeatMask, index) },
                        label = { Text(shortLabel) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeonCyan,
                            selectedLabelColor = MaterialTheme.colorScheme.background,
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                QuickRepeatChip("Every day", RepeatDays.EVERY_DAY) { repeatMask = it }
                QuickRepeatChip("Weekdays", RepeatDays.WEEKDAYS) { repeatMask = it }
                QuickRepeatChip("Weekends", RepeatDays.WEEKENDS) { repeatMask = it }
                QuickRepeatChip("Once", RepeatDays.NONE) { repeatMask = it }
            }

            Spacer(Modifier.height(4.dp))

            Text(
                text = "Schedule: " + RepeatDays.toDisplayString(repeatMask),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    val alarm = AlarmEntity(
                        id = initialAlarm?.id ?: 0L,
                        hour = timePickerState.hour,
                        minute = timePickerState.minute,
                        label = label.ifBlank { "Reminder" },
                        challengeType = challengeType,
                        stepGoal = stepGoal,
                        repeatDays = repeatMask,
                        enabled = true,
                        createdAt = initialAlarm?.createdAt ?: System.currentTimeMillis()
                    )
                    onSave(alarm)
                },
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth().height(58.dp)
            ) {
                Icon(Icons.Filled.Check, contentDescription = null)
                Spacer(Modifier.width(10.dp))
                Text(
                    text = if (isEditing) "Save changes" else "Arm Zolarm",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 4.dp)
    )
}

@Composable
private fun QuickRepeatChip(label: String, mask: Int, onSelect: (Int) -> Unit) {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        onClick = { onSelect(mask) }
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = NeonCyan,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
        )
    }
}
