package com.example.developmentofuserinterfaceshw1

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.SystemClock
import android.util.Log

enum class SensorSpeed(val label: String, val delayMillis: Long) {
    FAST("Fast", 240L),
    NORMAL("Normal", 360L),
    SLOW("Slow", 530L)
}

class TiltController(
    context: Context,
    private val onTiltLeft: () -> Unit,
    private val onTiltRight: () -> Unit,
    private val onSpeedChanged: (SensorSpeed) -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private var lastMoveTime = 0L
    private var lastSpeedChangeTime = 0L
    private var neutralX = 0f
    private var neutralY = 0f
    private var calibrationSamples = 0
    private var calibrationTotalX = 0f
    private var calibrationTotalY = 0f
    private var currentSpeed = SensorSpeed.NORMAL

    val isAvailable: Boolean
        get() = accelerometer != null

    fun start() {
        resetCalibration()
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (!updateCalibration(event)) {
            return
        }

        val deltaX = event.values[0] - neutralX
        val deltaY = event.values[1] - neutralY

        updateSpeedFromYAxis(deltaY)

        Log.d(
            TAG,
            "rawX=${event.values[0]}, neutralX=$neutralX, deltaX=$deltaX"
        )

        val now = SystemClock.elapsedRealtime()
        if (now - lastMoveTime < MOVE_COOLDOWN_MS) {
            return
        }

        when {
            deltaX > TILT_THRESHOLD -> {
                Log.d(TAG, "LEFT triggered")
                onTiltLeft()
                lastMoveTime = now
            }
            deltaX < -TILT_THRESHOLD -> {
                Log.d(TAG, "RIGHT triggered")
                onTiltRight()
                lastMoveTime = now
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    private fun resetCalibration() {
        lastMoveTime = 0L
        lastSpeedChangeTime = 0L
        neutralX = 0f
        neutralY = 0f
        calibrationSamples = 0
        calibrationTotalX = 0f
        calibrationTotalY = 0f
        currentSpeed = SensorSpeed.NORMAL
        onSpeedChanged(SensorSpeed.NORMAL)
    }

    private fun updateCalibration(event: SensorEvent): Boolean {
        if (calibrationSamples < CALIBRATION_SAMPLE_COUNT) {
            calibrationTotalX += event.values[0]
            calibrationTotalY += event.values[1]
            calibrationSamples++

            if (calibrationSamples == CALIBRATION_SAMPLE_COUNT) {
                neutralX = calibrationTotalX / CALIBRATION_SAMPLE_COUNT
                neutralY = calibrationTotalY / CALIBRATION_SAMPLE_COUNT
                Log.d(TAG, "Calibration complete: neutralX=$neutralX, neutralY=$neutralY")
            }
            return false
        }

        return true
    }

    private fun updateSpeedFromYAxis(delta: Float) {
        val targetSpeed = when (currentSpeed) {
            SensorSpeed.FAST -> {
                if (delta > -NORMAL_RETURN_THRESHOLD) SensorSpeed.NORMAL else SensorSpeed.FAST
            }
            SensorSpeed.SLOW -> {
                if (delta < NORMAL_RETURN_THRESHOLD) SensorSpeed.NORMAL else SensorSpeed.SLOW
            }
            SensorSpeed.NORMAL -> {
                when {
                    delta <= -SPEED_CHANGE_THRESHOLD -> SensorSpeed.FAST
                    delta >= SPEED_CHANGE_THRESHOLD -> SensorSpeed.SLOW
                    else -> SensorSpeed.NORMAL
                }
            }
        }

        val now = SystemClock.elapsedRealtime()
        if (targetSpeed != currentSpeed && now - lastSpeedChangeTime >= SPEED_CHANGE_COOLDOWN_MS) {
            currentSpeed = targetSpeed
            lastSpeedChangeTime = now
            onSpeedChanged(targetSpeed)
        }
    }

    companion object {
        private const val TAG = "TiltController"
        private const val TILT_THRESHOLD = 2.7f
        private const val MOVE_COOLDOWN_MS = 280L
        private const val SPEED_CHANGE_THRESHOLD = 1.8f
        private const val NORMAL_RETURN_THRESHOLD = 0.9f
        private const val SPEED_CHANGE_COOLDOWN_MS = 180L
        private const val CALIBRATION_SAMPLE_COUNT = 12
    }
}
