package com.zolarm.app.core

import android.content.Intent
import com.zolarm.app.data.AlarmEntity
import com.zolarm.app.data.ChallengeType

data class AlarmPayload(
    val id: Long,
    val hour: Int,
    val minute: Int,
    val label: String,
    val challengeType: ChallengeType,
    val stepGoal: Int
) {
    fun formattedTime(): String = "%02d:%02d".format(hour, minute)
}

object AlarmExtras {

    private const val K_ID = "zolarm.extra.ALARM_ID"
    private const val K_HOUR = "zolarm.extra.HOUR"
    private const val K_MINUTE = "zolarm.extra.MINUTE"
    private const val K_LABEL = "zolarm.extra.LABEL"
    private const val K_CHALLENGE = "zolarm.extra.CHALLENGE"
    private const val K_STEP_GOAL = "zolarm.extra.STEP_GOAL"

    fun write(intent: Intent, payload: AlarmPayload): Intent = intent.apply {
        putExtra(K_ID, payload.id)
        putExtra(K_HOUR, payload.hour)
        putExtra(K_MINUTE, payload.minute)
        putExtra(K_LABEL, payload.label)
        putExtra(K_CHALLENGE, payload.challengeType.name)
        putExtra(K_STEP_GOAL, payload.stepGoal)
    }

    fun write(intent: Intent, alarm: AlarmEntity): Intent = write(
        intent,
        AlarmPayload(
            id = alarm.id,
            hour = alarm.hour,
            minute = alarm.minute,
            label = alarm.label,
            challengeType = alarm.challengeType,
            stepGoal = alarm.stepGoal
        )
    )

    fun read(intent: Intent?): AlarmPayload? {
        if (intent == null) return null
        val id = intent.getLongExtra(K_ID, -1L)
        if (id <= 0L) return null
        return AlarmPayload(
            id = id,
            hour = intent.getIntExtra(K_HOUR, 7),
            minute = intent.getIntExtra(K_MINUTE, 0),
            label = intent.getStringExtra(K_LABEL) ?: "Zolarm",
            challengeType = runCatching {
                ChallengeType.valueOf(intent.getStringExtra(K_CHALLENGE) ?: ChallengeType.STEPS.name)
            }.getOrDefault(ChallengeType.STEPS),
            stepGoal = intent.getIntExtra(K_STEP_GOAL, 10)
        )
    }
}
