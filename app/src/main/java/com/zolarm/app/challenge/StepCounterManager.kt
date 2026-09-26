package com.zolarm.app.challenge

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import androidx.core.content.getSystemService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class StepCounterManager(context: Context) {

    private val sensorManager: SensorManager? = context.getSystemService()
    private val counterSensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    private val detectorSensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

    private val _steps = MutableStateFlow(0)
    val steps: StateFlow<Int> = _steps.asStateFlow()

    private val _isAvailable = MutableStateFlow(counterSensor != null || detectorSensor != null)
    val isAvailable: StateFlow<Boolean> = _isAvailable.asStateFlow()

    private var baseline: Float? = null
    private var running = false

    private val listener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            when (event.sensor.type) {
                Sensor.TYPE_STEP_COUNTER -> {
                    val baselineValue = baseline ?: event.values[0].also { baseline = it }
                    val delta = (event.values[0] - baselineValue).coerceAtLeast(0f)
                    _steps.value = delta.toInt()
                }
                Sensor.TYPE_STEP_DETECTOR -> {
                    _steps.value += 1
                }
            }
        }
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
    }

    fun start() {
        if (running) return
        val sm = sensorManager ?: return
        _steps.value = 0
        baseline = null
        val sensor = counterSensor ?: detectorSensor
        if (sensor == null) {
            _isAvailable.value = false
            Log.w(TAG, "No step sensor available on this device")
            return
        }
        _isAvailable.value = true
        running = true
        sm.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
        Log.i(TAG, "Step challenge started using " + sensor.name)
    }

    fun stop() {
        if (!running) return
        running = false
        runCatching { sensorManager?.unregisterListener(listener) }
        baseline = null
    }

    fun refreshBaseline() {
        baseline = null
        _steps.value = 0
    }

    private companion object { const val TAG = "ZolarmSteps" }
}
