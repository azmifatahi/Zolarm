package com.zolarm.app.core

import android.content.Context
import com.zolarm.app.R
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

    fun shortLabels(context: Context): List<String> = listOf(
        context.getString(R.string.day_mon),
        context.getString(R.string.day_tue),
        context.getString(R.string.day_wed),
        context.getString(R.string.day_thu),
        context.getString(R.string.day_fri),
        context.getString(R.string.day_sat),
        context.getString(R.string.day_sun)
    )

    fun fullLabels(context: Context): List<String> = listOf(
        context.getString(R.string.day_monday),
        context.getString(R.string.day_tuesday),
        context.getString(R.string.day_wednesday),
        context.getString(R.string.day_thursday),
        context.getString(R.string.day_friday),
        context.getString(R.string.day_saturday),
        context.getString(R.string.day_sunday)
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

    fun toDisplayString(context: Context, mask: Int): String = when (mask) {
        NONE      -> context.getString(R.string.repeat_once)
        EVERY_DAY -> context.getString(R.string.repeat_every_day)
        WEEKDAYS  -> context.getString(R.string.repeat_weekdays)
        WEEKENDS  -> context.getString(R.string.repeat_weekends)
        else -> {
            val labels = fullLabels(context)
            orderedBits.mapIndexedNotNull { index, _ ->
                if (isOn(mask, index)) labels[index].take(3) else null
            }.joinToString("، ")
        }
    }

    /** Fallback without Context (logs / non-UI). */
    fun toDisplayString(mask: Int): String = when (mask) {
        NONE -> "Once"
        EVERY_DAY -> "Every day"
        WEEKDAYS -> "Weekdays"
        WEEKENDS -> "Weekends"
        else -> "Custom"
    }
}
