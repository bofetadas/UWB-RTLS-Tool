package bachelor.test.locationapp.presenter.positioning

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class SensorEventListenerImpl(context: Context, private val imu: IMUInputListener): SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    // Sensors
    private val gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)
    private val linearAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
    private val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    fun registerListener(){
        sensorManager.registerListener(this, gravitySensor, SensorManager.SENSOR_DELAY_FASTEST)
        sensorManager.registerListener(this, linearAccelerometer, SensorManager.SENSOR_DELAY_FASTEST)
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_FASTEST)
    }

    fun unregisterListener(){
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        when (event?.sensor?.stringType) {
            Sensor.STRING_TYPE_GRAVITY -> {
                imu.onGravitySensorUpdate(event.values)
            }
            Sensor.STRING_TYPE_LINEAR_ACCELERATION -> {
                imu.onLinearAccelerometerUpdate(event.values)
            }
            Sensor.STRING_TYPE_MAGNETIC_FIELD -> {
                imu.onMagnetometerUpdate(event.values)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}