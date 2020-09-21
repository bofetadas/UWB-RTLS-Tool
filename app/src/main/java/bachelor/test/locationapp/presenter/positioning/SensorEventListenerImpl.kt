package bachelor.test.locationapp.presenter.positioning

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class SensorEventListenerImpl(context: Context, private val imu: IMUInputListener): SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    // Sensors
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)
    private val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    fun registerListener(){
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME)
        sensorManager.registerListener(this, gravitySensor, SensorManager.SENSOR_DELAY_GAME)
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_GAME)
    }

    fun unregisterListener(){
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        when (event?.sensor?.stringType) {
            Sensor.STRING_TYPE_ACCELEROMETER -> {
                imu.onAccelerometerUpdate(event.values)
            }
            Sensor.STRING_TYPE_GRAVITY -> {
                imu.onGravitySensorUpdate(event.values)
            }
            Sensor.STRING_TYPE_MAGNETIC_FIELD -> {
                imu.onMagnetometerUpdate(event.values)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}