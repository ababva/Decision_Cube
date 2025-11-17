package com.decisioncube.app.util

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

class ShakeDetector(
    private val context: Context,
    private val onShake: () -> Unit
) : SensorEventListener {

    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null
    private var lastUpdate: Long = 0
    private var lastX = 0f
    private var lastY = 0f
    private var lastZ = 0f

    companion object {
        private const val SHAKE_THRESHOLD = 1000
        private const val UPDATE_THRESHOLD = 100
    }

    fun start() {
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        accelerometer?.let { sensor ->
            sensorManager?.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stop() {
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let { sensorEvent ->
            val currentTime = System.currentTimeMillis()

            if (currentTime - lastUpdate > UPDATE_THRESHOLD) {
                val timeDiff = currentTime - lastUpdate
                lastUpdate = currentTime

                val x = sensorEvent.values[0]
                val y = sensorEvent.values[1]
                val z = sensorEvent.values[2]

                val speed = sqrt(
                    ((x - lastX) * (x - lastX) +
                            (y - lastY) * (y - lastY) +
                            (z - lastZ) * (z - lastZ)).toDouble()
                ) / timeDiff * 10000

                if (speed > SHAKE_THRESHOLD) {
                    onShake()
                }

                lastX = x
                lastY = y
                lastZ = z
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}

