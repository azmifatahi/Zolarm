package com.zolarm.app.core

import java.util.Calendar

/**
 * Repeat days stored as a 7-bit bitmask: bit0=Mon ... bit6=Sun.
 */
object RepeatDays {

    const val NONE = 0
    const val MONDAY    = 1 shl 0
    const val TUESDAY   = 1 shl 1
    const val WEDNESDAY = 1 shl 2
    const val THURSDAY  = 1 shl 3
    const val FRIDAY    = 1 shl 4
    const val SATURDAY  = 1 shl 5
    const val SUNDAY    = 1 shl 6

    const val EVERY_DAY = 0b1111111
    const val WEEKDAYS  = MONDAY or TUESDAY or WEDNESDAY or THURSDAY or FRIDAY
    const val WEEKENDS  = SATURDAY or SUNDAY

    val orderedBits = listOf(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY)
    val shortLabels = listOf("M", "T", "W", "T", "F", "S", "S")
    val fullLabels = listOf(
        "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"
    )

    fun indexOf(calendarDayOfWeek: Int): Int = when (calendarDayOfWeek) {
        Calendar.MONDAY    -> 0
        Calendar.TUESDAY   -> 1
        Calendar.WEDNESDAY -> 2
        Calendar.THURSDAY  -> 3
        Calendar.FRIDAY    -> 4
        Calendar.SATURDAY  -> 5
        else               -> 6
    }

    fun isOn(mask: Int, index: Int): Boolean = (mask and (1 shl index)) != 0

    fun toggle(mask: Int, index: Int): Int = mask xor (1 shl index)

    fun toDisplayString(mask: Int): String = when (mask) {
        NONE      -> "Once"
        EVERY_DAY -> "Every day"
        WEEKDAYS  -> "Weekdays"
        WEEKENDS  -> "Weekends"
        else -> orderedBits.mapIndexedNotNull { index, _ ->
            if (isOn(mask, index)) fullLabels[index].take(3) else null
        }.joinToString(", ")
    }
}
