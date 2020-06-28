package bachelor.test.locationapp.presenter

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import bachelor.test.locationapp.view.MainScreenContract

class AccelerometerReader(context: Context, val presenter: MainScreenContract.Presenter):
    SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    // Sensors
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val linearAcc = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
    private val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    // Sensor values arrays
    private var accValues = FloatArray(4) {0f}
    private var linearAccValues = FloatArray(4) {0f}
    private var magnetValues = FloatArray(3)

    fun registerListener(){
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this, linearAcc, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL)
    }

    fun unregisterListener(){
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        when (event?.sensor?.stringType) {
            Sensor.STRING_TYPE_ACCELEROMETER -> {
                accValues[0] = event.values[0]
                accValues[1] = event.values[1]
                accValues[2] = event.values[2]
            }
            Sensor.STRING_TYPE_LINEAR_ACCELERATION -> {
                linearAccValues[0] = event.values[0]
                linearAccValues[1] = event.values[1]
                linearAccValues[2] = event.values[2]
            }
            Sensor.STRING_TYPE_MAGNETIC_FIELD -> {
                magnetValues = event.values
            }
        }
        val worldAcc = calculateWorldAcceleration()
        presenter.onAccelerometerUpdate(worldAcc)
    }

    private fun calculateWorldAcceleration(): AccelerometerData{
        val resultVector = FloatArray(4) {0f}
        // Rotation matrices
        val R = FloatArray(16)
        val I = FloatArray(16)
        if (SensorManager.getRotationMatrix(R, I, accValues, magnetValues)) {
            val inv = FloatArray(16)
            android.opengl.Matrix.invertM(inv, 0, R, 0)
            android.opengl.Matrix.multiplyMV(resultVector, 0, inv, 0, linearAccValues, 0)
        }
        return AccelerometerData(resultVector[0], resultVector[1], resultVector[2])
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int){}
}