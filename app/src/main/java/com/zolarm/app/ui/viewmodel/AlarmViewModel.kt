package com.zolarm.app.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.zolarm.app.data.AlarmEntity
import com.zolarm.app.data.AlarmRepository
import com.zolarm.app.data.ZolarmDatabase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AlarmViewModel(
    private val repository: AlarmRepository
) : ViewModel() {

    val alarms: StateFlow<List<AlarmEntity>> = repository.alarms
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = emptyList()
        )

    fun toggleAlarm(alarm: AlarmEntity, enabled: Boolean) = viewModelScope.launch {
        repository.setEnabled(alarm, enabled)
    }

    fun saveAlarm(alarm: AlarmEntity) = viewModelScope.launch {
        repository.upsert(alarm)
    }

    fun deleteAlarm(alarm: AlarmEntity) = viewModelScope.launch {
        repository.delete(alarm)
    }

    companion object {
        fun factory(context: Context): ViewModelProvider.Factory {
            val appContext = context.applicationContext
            return viewModelFactory {
                initializer {
                    AlarmViewModel(
                        AlarmRepository(
                            dao = ZolarmDatabase.get(appContext).alarmDao(),
                            appContext = appContext
                        )
                    )
                }
            }
        }
    }
}
