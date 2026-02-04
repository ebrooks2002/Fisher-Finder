package com.github.ebrooks2002.fisherfinder.location
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Communicates with system hardware to receive device rotation data.
 *
 * This class creates sensor objects, and registers listeners to them to detect changes in rotation.
 * It then exposes calculated compass heading as a flow.
 *
 * @constructor The application context to retrieve the SensorManager object.
 * @author E. Brooks
 */

class RotationSensor (private val context: Context) {
    private val client = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    /**
     * Returns a flow of Floats representing the current azimuth rotation of the device.
     */
    fun getRotationUpdates(): Flow<Float> = callbackFlow {
        // create sensor objects
        val rotationVector = client.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

        if (rotationVector == null) {
            close()
            return@callbackFlow
        }

        val rotationMatrix = FloatArray(9)
        val orientationAngles = FloatArray(3)
        val sensorListener = object: SensorEventListener {

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                    // Android calculates the tilt-compensated matrix for you here
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)

                    SensorManager.getOrientation(rotationMatrix, orientationAngles)
                    val azimuthInRadians = orientationAngles[0]
                    val azimuthInDegrees = Math.toDegrees(azimuthInRadians.toDouble()).toFloat()
                    val heading = (azimuthInDegrees + 360) % 360
                    trySend(heading)
                }
            }
        }
        client.registerListener(sensorListener, rotationVector, SensorManager.SENSOR_DELAY_UI)

        awaitClose {
            client.unregisterListener(sensorListener)
        }
    }

}

