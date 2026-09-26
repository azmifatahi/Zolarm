package com.zolarm.app.core

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.os.Build
import androidx.core.content.getSystemService
import com.zolarm.app.R

object NotificationChannels {

    const val ALARM_CHANNEL_ID = "zolarm_alarm_channel"
    const val SERVICE_CHANNEL_ID = "zolarm_service_channel"

    fun ensureAll(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = context.getSystemService<NotificationManager>() ?: return

        if (nm.getNotificationChannel(ALARM_CHANNEL_ID) == null) {
            val alarmChannel = NotificationChannel(
                ALARM_CHANNEL_ID,
                context.getString(R.string.channel_alarm_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.channel_alarm_desc)
                enableLights(true)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 600, 400, 600, 400, 900)
                setShowBadge(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                setSound(
                    null,
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                setBypassDnd(true)
            }
            nm.createNotificationChannel(alarmChannel)
        }

        if (nm.getNotificationChannel(SERVICE_CHANNEL_ID) == null) {
            val serviceChannel = NotificationChannel(
                SERVICE_CHANNEL_ID,
                context.getString(R.string.channel_service_name),
                NotificationManager.IMPORTANCE_MIN
            ).apply {
                description = context.getString(R.string.channel_service_desc)
                setShowBadge(false)
            }
            nm.createNotificationChannel(serviceChannel)
        }
    }
}
