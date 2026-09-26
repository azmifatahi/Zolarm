package com.zolarm.app.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import com.zolarm.app.MainActivity
import com.zolarm.app.R
import com.zolarm.app.core.AlarmExtras
import com.zolarm.app.core.AlarmPayload
import com.zolarm.app.core.NotificationChannels
import com.zolarm.app.ui.overlay.ZolarmOverlayActivity

class ZolarmForegroundService : Service() {

    private var alarmPlayer: ZolarmAlarmPlayer? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var currentPayload: AlarmPayload? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        NotificationChannels.ensureAll(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                Log.i(TAG, "Stop requested")
                tearDown()
                return START_NOT_STICKY
            }
            ACTION_START_ALARM -> {
                val payload = AlarmExtras.read(intent)
                if (payload == null) {
                    Log.w(TAG, "No payload - stopping")
                    stopSelf()
                    return START_NOT_STICKY
                }
                currentPayload = payload
                beginAlarm(payload)
                return START_STICKY
            }
            else -> {
                stopSelf()
                return START_NOT_STICKY
            }
        }
    }

    private fun beginAlarm(payload: AlarmPayload) {
        acquireWakeLock()
        promoteToForeground(payload)
        startAlarmAudio()
        launchOverlay(payload)
    }

    private fun startAlarmAudio() {
        if (alarmPlayer == null) alarmPlayer = ZolarmAlarmPlayer(applicationContext)
        alarmPlayer?.start()
    }

    private fun launchOverlay(payload: AlarmPayload) {
        val overlay = AlarmExtras.write(
            Intent(this, ZolarmOverlayActivity::class.java), payload
        ).addFlags(
            Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_SINGLE_TOP or
                Intent.FLAG_ACTIVITY_NO_USER_ACTION or
                Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
        )
        try { startActivity(overlay) }
        catch (t: Throwable) { Log.e(TAG, "Could not launch overlay", t) }
    }

    private fun promoteToForeground(payload: AlarmPayload) {
        val fullScreenIntent = PendingIntent.getActivity(
            this, payload.id.toInt(),
            AlarmExtras.write(Intent(this, ZolarmOverlayActivity::class.java), payload)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val stopIntent = PendingIntent.getService(
            this, STOP_REQUEST_CODE,
            Intent(this, ZolarmForegroundService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val openApp = PendingIntent.getActivity(
            this, OPEN_APP_REQUEST_CODE,
            Intent(this, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification: Notification = NotificationCompat.Builder(this, NotificationChannels.ALARM_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_zolarm)
            .setContentTitle(getString(R.string.notif_alarm_title, payload.formattedTime()))
            .setContentText(payload.label.ifBlank { getString(R.string.notif_alarm_text) })
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setAutoCancel(false)
            .setContentIntent(openApp)
            .setFullScreenIntent(fullScreenIntent, true)
            .addAction(0, getString(R.string.action_dismiss), stopIntent)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID, notification,
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun tearDown() {
        runCatching { alarmPlayer?.stop() }
        alarmPlayer = null
        releaseWakeLock()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION") stopForeground(true)
        }
        stopSelf()
    }

    override fun onDestroy() {
        runCatching { alarmPlayer?.stop() }
        alarmPlayer = null
        releaseWakeLock()
        super.onDestroy()
    }

    private fun acquireWakeLock() {
        if (wakeLock?.isHeld == true) return
        val pm = getSystemService<PowerManager>() ?: return
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Zolarm::AlarmWakeLock").apply {
            setReferenceCounted(false)
            acquire(WAKE_LOCK_TIMEOUT_MS)
        }
    }

    private fun releaseWakeLock() {
        runCatching { if (wakeLock?.isHeld == true) wakeLock?.release() }
        wakeLock = null
    }

    companion object {
        private const val TAG = "ZolarmService"
        const val ACTION_START_ALARM = "com.zolarm.app.action.START_ALARM"
        const val ACTION_STOP = "com.zolarm.app.action.STOP_ALARM"

        private const val NOTIFICATION_ID = 0x201
        private const val STOP_REQUEST_CODE = 0x202
        private const val OPEN_APP_REQUEST_CODE = 0x203
        private const val WAKE_LOCK_TIMEOUT_MS = 30L * 60L * 1000L

        fun startAlarm(context: Context, payload: AlarmPayload) {
            val intent = AlarmExtras.write(
                Intent(context, ZolarmForegroundService::class.java), payload
            ).setAction(ACTION_START_ALARM)
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, ZolarmForegroundService::class.java).setAction(ACTION_STOP)
            runCatching { context.startService(intent) }
        }
    }
}
