package com.zolarm.app.data

import androidx.room.TypeConverter

class ZolarmConverters {
    @TypeConverter
    fun fromChallengeType(value: ChallengeType): String = value.name

    @TypeConverter
    fun toChallengeType(value: String): ChallengeType =
        runCatching { ChallengeType.valueOf(value) }.getOrDefault(ChallengeType.STEPS)
}
