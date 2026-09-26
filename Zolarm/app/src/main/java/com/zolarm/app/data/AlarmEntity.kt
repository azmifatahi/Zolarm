package com.zolarm.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ChallengeType { STEPS, CAMERA }

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val hour: Int,
    val minute: Int,
    val label: String = "Wake up",
    val challengeType: ChallengeType = ChallengeType.STEPS,
    val stepGoal: Int = 10,
    val repeatDays: Int = 0,
    val enabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
) {
    val timeText: String get() = "%02d:%02d".format(hour, minute)
}
