package com.zolarm.app.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.zolarm.app.R
import com.zolarm.app.challenge.CameraChallenge
import com.zolarm.app.challenge.StepCounterManager
import com.zolarm.app.core.AlarmPayload
import com.zolarm.app.data.ChallengeType
import com.zolarm.app.ui.components.ZolarmLogo
import com.zolarm.app.ui.theme.NeonCyan
import com.zolarm.app.ui.theme.NeonLime
import com.zolarm.app.ui.theme.NeonViolet

@Composable
fun ZolarmOverlayScreen(
    payload: AlarmPayload,
    onDismissed: () -> Unit
) {
    var challengeCompleted by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "ring")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 750),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val dismissContainer by animateColorAsState(
        targetValue = if (challengeCompleted) NeonLime
        else MaterialTheme.colorScheme.surfaceContainerHigh,
        animationSpec = tween(400),
        label = "dismissColor"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .systemBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                ZolarmLogo(modifier = Modifier.size(36.dp))
                Spacer(Modifier.width(10.dp))
                Text(
                    text = stringResource(R.string.dashboard_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = NeonCyan
                )
            }

            Spacer(Modifier.height(28.dp))

            Text(
                text = payload.formattedTime(),
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.scale(pulse)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = payload.label.ifBlank { stringResource(R.string.reminder_default) },
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(28.dp))

            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when (payload.challengeType) {
                        ChallengeType.STEPS -> StepChallengeContent(
                            stepGoal = payload.stepGoal,
                            onCompleted = { challengeCompleted = true }
                        )
                        ChallengeType.CAMERA -> CameraChallengeContent(
                            onCompleted = {
                                challengeCompleted = true
                                onDismissed() // إيقاف المنبّه فوراً بعد التقاط الصورة
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = { if (challengeCompleted) onDismissed() },
                enabled = challengeCompleted,
                shape = RoundedCornerShape(22.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = dismissContainer,
                    contentColor = if (challengeCompleted) MaterialTheme.colorScheme.background
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledContainerColor = dismissContainer,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.fillMaxWidth().height(64.dp)
            ) {
                Icon(
                    imageVector = if (challengeCompleted) Icons.Filled.Check else Icons.Filled.Lock,
                    contentDescription = null
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = if (challengeCompleted) stringResource(R.string.overlay_dismiss)
                    else stringResource(R.string.overlay_locked),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = if (challengeCompleted) stringResource(R.string.overlay_hint_done)
                else stringResource(R.string.overlay_hint_locked),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun StepChallengeContent(
    stepGoal: Int,
    onCompleted: () -> Unit
) {
    val context = LocalContext.current
    val stepManager = remember { StepCounterManager(context) }
    val steps by stepManager.steps.collectAsState()
    val sensorAvailable by stepManager.isAvailable.collectAsState()

    DisposableEffect(Unit) {
        stepManager.start()
        onDispose { stepManager.stop() }
    }

    LaunchedEffect(steps) {
        if (steps >= stepGoal) onCompleted()
    }

    val progress = (steps.toFloat() / stepGoal.toFloat()).coerceIn(0f, 1f)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.DirectionsWalk, contentDescription = null, tint = NeonCyan)
            Spacer(Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.step_challenge),
                style = MaterialTheme.typography.labelMedium,
                color = NeonCyan,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(18.dp))

        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = { progress },
                strokeWidth = 10.dp,
                color = if (progress >= 1f) NeonLime else NeonCyan,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                modifier = Modifier.size(148.dp)
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = steps.toString(),
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "/ " + stepGoal + " steps",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.steps_progress, steps, stepGoal),
            style = MaterialTheme.typography.titleMedium,
            color = if (progress >= 1f) NeonLime else MaterialTheme.colorScheme.onSurface
        )

        if (!sensorAvailable) {
            Spacer(Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.no_step_sensor),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun CameraChallengeContent(
    onCompleted: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(8.dp).background(NeonViolet, CircleShape))
            Spacer(Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.camera_challenge),
                style = MaterialTheme.typography.labelMedium,
                color = NeonViolet,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(14.dp))
        CameraChallenge(
            modifier = Modifier.fillMaxWidth(),
            onCaptured = onCompleted
        )
    }
}
