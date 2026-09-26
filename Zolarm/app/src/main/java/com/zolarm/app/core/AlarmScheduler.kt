package com.zolarm.app.core

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.content.getSystemService
import com.zolarm.app.data.AlarmEntity
import com.zolarm.app.receiver.ZolarmAlarmReceiver
import com.zolarm.app.ui.overlay.ZolarmOverlayActivity
import java.util.Calendar

object AlarmScheduler {

    private const val TAG = "ZolarmScheduler"
    const val ACTION_FIRE_ALARM = "com.zolarm.app.action.FIRE_ALARM"

    fun schedule(context: Context, alarm: AlarmEntity) {
        if (!alarm.enabled) {
            cancel(context, alarm)
            return
        }
        val am = context.getSystemService<AlarmManager>() ?: return
        val triggerAt = nextTriggerMillis(alarm)
        val operation = alarmPendingIntent(context, alarm)

        try {
            am.setAlarmClock(
                AlarmManager.AlarmClockInfo(triggerAt, showIntent(context, alarm)),
                operation
            )
            Log.i(TAG, "Scheduled alarm " + alarm.id + " for " + java.util.Date(triggerAt))
        } catch (t: Throwable) {
            Log.w(TAG, "setAlarmClock failed, falling back", t)
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, operation)
                } else {
                    am.setExact(AlarmManager.RTC_WAKEUP, triggerAt, operation)
                }
            } catch (t2: SecurityException) {
                Log.e(TAG, "Exact alarm permission missing - inexact fallback", t2)
                am.set(AlarmManager.RTC_WAKEUP, triggerAt, operation)
            }
        }
    }

    fun cancel(context: Context, alarm: AlarmEntity) {
        val am = context.getSystemService<AlarmManager>() ?: return
        am.cancel(alarmPendingIntent(context, alarm))
        Log.i(TAG, "Cancelled alarm " + alarm.id)
    }

    fun cancelById(context: Context, id: Long) {
        val am = context.getSystemService<AlarmManager>() ?: return
        am.cancel(alarmPendingIntent(context, id))
    }

    fun nextTriggerMillis(alarm: AlarmEntity, from: Long = System.currentTimeMillis()): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = from
            set(Calendar.HOUR_OF_DAY, alarm.hour)
            set(Calendar.MINUTE, alarm.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (cal.timeInMillis <= from) cal.add(Calendar.DAY_OF_YEAR, 1)

        if (alarm.repeatDays == RepeatDays.NONE) return cal.timeInMillis

        repeat(8) {
            val index = RepeatDays.indexOf(cal.get(Calendar.DAY_OF_WEEK))
            if (RepeatDays.isOn(alarm.repeatDays, index)) return cal.timeInMillis
            cal.add(Calendar.DAY_OF_YEAR, 1)
            setTimeOfDay(cal, alarm.hour, alarm.minute)
        }
        return cal.timeInMillis
    }

    private fun setTimeOfDay(cal: Calendar, hour: Int, minute: Int) {
        cal.set(Calendar.HOUR_OF_DAY, hour)
        cal.set(Calendar.MINUTE, minute)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
    }

    private fun requestCode(id: Long): Int = (id xor (id ushr 32)).toInt()

    private fun alarmPendingIntent(context: Context, alarm: AlarmEntity): PendingIntent =
        alarmPendingIntent(context, alarm.id, AlarmExtras.write(Intent(), alarm))

    private fun alarmPendingIntent(context: Context, id: Long, base: Intent? = null): PendingIntent {
        val intent = (base ?: Intent()).apply {
            setClass(context, ZolarmAlarmReceiver::class.java)
            action = ACTION_FIRE_ALARM + "." + id
        }
        return PendingIntent.getBroadcast(
            context, requestCode(id), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun showIntent(context: Context, alarm: AlarmEntity): PendingIntent {
        val intent = AlarmExtras.write(Intent(context, ZolarmOverlayActivity::class.java), alarm)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        return PendingIntent.getActivity(
            context, requestCode(alarm.id), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
