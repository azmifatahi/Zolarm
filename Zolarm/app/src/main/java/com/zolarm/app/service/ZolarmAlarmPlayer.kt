package com.zolarm.app.service

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.content.getSystemService

class ZolarmAlarmPlayer(private val context: Context) {

    private var player: MediaPlayer? = null
    private var previousRingerMode: Int? = null

    fun start() {
        forceMaxAlarmVolume()
        escapeSilentMode()
        startTone()
    }

    fun stop() {
        runCatching {
            player?.let {
                if (it.isPlaying) it.stop()
                it.release()
            }
        }
        player = null
        restoreRingerMode()
    }

    private fun forceMaxAlarmVolume() {
        runCatching {
            val am = context.getSystemService<AudioManager>() ?: return
            val max = am.getStreamMaxVolume(AudioManager.STREAM_ALARM)
            am.setStreamVolume(AudioManager.STREAM_ALARM, max, 0)
        }.onFailure { Log.w(TAG, "Could not force alarm volume", it) }
    }

    private fun escapeSilentMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val nm = context.getSystemService<android.app.NotificationManager>()
            if (nm?.isNotificationPolicyAccessGranted != true) {
                Log.w(TAG, "DND access not granted - cannot escape silent mode")
                return
            }
        }
        runCatching {
            val am = context.getSystemService<AudioManager>() ?: return
            previousRingerMode = am.ringerMode
            am.ringerMode = AudioManager.RINGER_MODE_NORMAL
        }.onFailure { Log.w(TAG, "Could not change ringer mode", it) }
    }

    private fun restoreRingerMode() {
        val previous = previousRingerMode ?: return
        runCatching { context.getSystemService<AudioManager>()?.ringerMode = previous }
        previousRingerMode = null
    }

    private fun startTone() {
        val uri = resolveAlarmUri() ?: return
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        runCatching {
            player = MediaPlayer().apply {
                setAudioAttributes(attrs)
                setDataSource(context, uri)
                isLooping = true
                prepare()
                start()
            }
        }.onFailure { Log.e(TAG, "Failed to start alarm tone", it) }
    }

    private fun resolveAlarmUri(): Uri? {
        val rawId = context.resources.getIdentifier("zolarm_alarm", "raw", context.packageName)
        if (rawId != 0) return Uri.parse("android.resource://" + context.packageName + "/" + rawId)
        return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
    }

    private companion object { const val TAG = "ZolarmAlarmPlayer" }
}
