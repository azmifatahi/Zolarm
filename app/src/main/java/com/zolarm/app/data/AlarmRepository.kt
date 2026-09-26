package com.zolarm.app.data

import android.content.Context
import com.zolarm.app.core.AlarmScheduler
import kotlinx.coroutines.flow.Flow

class AlarmRepository(
    private val dao: AlarmDao,
    private val appContext: Context
) {
    val alarms: Flow<List<AlarmEntity>> = dao.observeAll()

    suspend fun getEnabled(): List<AlarmEntity> = dao.getEnabled()
    suspend fun getById(id: Long): AlarmEntity? = dao.getById(id)

    suspend fun upsert(alarm: AlarmEntity): Long {
        val newId = dao.upsert(alarm)
        val persisted = if (alarm.id == 0L) alarm.copy(id = newId) else alarm
        if (persisted.enabled) AlarmScheduler.schedule(appContext, persisted)
        else AlarmScheduler.cancel(appContext, persisted)
        return newId
    }

    suspend fun setEnabled(alarm: AlarmEntity, enabled: Boolean) {
        dao.setEnabled(alarm.id, enabled)
        val updated = alarm.copy(enabled = enabled)
        if (enabled) AlarmScheduler.schedule(appContext, updated)
        else AlarmScheduler.cancel(appContext, updated)
    }

    suspend fun delete(alarm: AlarmEntity) {
        AlarmScheduler.cancel(appContext, alarm)
        dao.delete(alarm)
    }

    suspend fun rescheduleAll() {
        dao.getEnabled().forEach { AlarmScheduler.schedule(appContext, it) }
    }
}
