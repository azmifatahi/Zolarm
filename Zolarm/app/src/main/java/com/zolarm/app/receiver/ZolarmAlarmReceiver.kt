package com.zolarm.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.zolarm.app.core.AlarmExtras
import com.zolarm.app.core.AlarmScheduler
import com.zolarm.app.core.RepeatDays
import com.zolarm.app.data.ZolarmDatabase
import com.zolarm.app.service.ZolarmForegroundService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ZolarmAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action?.startsWith(AlarmScheduler.ACTION_FIRE_ALARM) != true) return

        val pendingResult = goAsync()
        val appContext = context.applicationContext

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val payload = AlarmExtras.read(intent)
                if (payload == null) {
                    Log.w(TAG, "Alarm fired without a valid payload")
                    return@launch
                }
                Log.i(TAG, "Alarm " + payload.id + " fired - starting ZolarmForegroundService")
                ZolarmForegroundService.startAlarm(appContext, payload)

                val entity = ZolarmDatabase.get(appContext).alarmDao().getById(payload.id)
                if (entity != null && entity.enabled && entity.repeatDays != RepeatDays.NONE) {
                    AlarmScheduler.schedule(appContext, entity)
                }
            } catch (t: Throwable) {
                Log.e(TAG, "Failed to handle alarm", t)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private companion object { const val TAG = "ZolarmAlarmReceiver" }
}
