package com.example.healthmate.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class StepCounterManager(context: Context) {

    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val stepSensor: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

    val isAvailable: Boolean get() = stepSensor != null

    /**
     * Emits the number of steps taken since the sensor was first registered.
     * Returns a flow of 0 if the device has no step-counter sensor.
     */
    val stepCount: Flow<Int> = callbackFlow {
        if (stepSensor == null) {
            trySend(0)
            close()
            return@callbackFlow
        }

        var initialStepCount: Int? = null

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val totalSteps = event.values[0].toInt()
                if (initialStepCount == null) {
                    initialStepCount = totalSteps
                }
                trySend(totalSteps - (initialStepCount ?: totalSteps))
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(
            listener,
            stepSensor,
            SensorManager.SENSOR_DELAY_UI
        )

        awaitClose { sensorManager.unregisterListener(listener) }
    }
}
