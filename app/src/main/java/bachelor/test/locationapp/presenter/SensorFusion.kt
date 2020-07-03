package bachelor.test.locationapp.presenter

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import bachelor.test.locationapp.model.Movement
import bachelor.test.locationapp.model.MovementData
import bachelor.test.locationapp.view.MainScreenContract

private const val DETECTION_THRESHOLD_X = 0.25
private const val DETECTION_THRESHOLD_Y = 0.25
private const val DETECTION_THRESHOLD_Z = 0.6

/*
 * This class' purpose is to figure out whether or not the mobile phone within the VR goggles experienced
 * movement on any of the three world axis. By figuring out
 */
class SensorFusion(context: Context, val presenter: MainScreenContract.Presenter):
    SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    // Sensors
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val linearAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
    private val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    private val gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)

    // Sensor values arrays
    private var linearAccValues = FloatArray(4) {0f}
    private var accValues = FloatArray(4) {0f}
    private var gravityValues = FloatArray(4) {0f}
    private var magnetValues = FloatArray(3)

    // World acceleration values
    private var worldAccValues = ArrayList<AccelerometerData>()

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
            Sensor.STRING_TYPE_GRAVITY -> {
                gravityValues[0] = event.values[0]
                gravityValues[1] = event.values[1]
                gravityValues[2] = event.values[2]
            }
        }
        val worldAcc = calculateWorldAcceleration()
        worldAccValues.add(worldAcc)
        presenter.onAccelerometerUpdate(worldAcc)
    }

    fun getMovementData(): MovementData {
        var xAcc = 0f
        var yAcc = 0f
        var zAcc = 0f
        for (acc in worldAccValues){
            xAcc += acc.xAcc
            yAcc += acc.yAcc
            zAcc += acc.zAcc
        }
        val xAccMean = xAcc / worldAccValues.size
        val yAccMean = yAcc / worldAccValues.size
        val zAccMean = zAcc / worldAccValues.size

        // Clear list for next time interval
        worldAccValues.clear()

        return MovementData(evaluateAcc(xAccMean, DETECTION_THRESHOLD_X), evaluateAcc(yAccMean, DETECTION_THRESHOLD_Y), evaluateAcc(zAccMean, DETECTION_THRESHOLD_Z))
    }

    fun registerListener(){
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST)
        sensorManager.registerListener(this, linearAccelerometer, SensorManager.SENSOR_DELAY_FASTEST)
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_FASTEST)
        sensorManager.registerListener(this, gravitySensor, SensorManager.SENSOR_DELAY_FASTEST)
    }

    fun unregisterListener(){
        sensorManager.unregisterListener(this)
    }

    private fun calculateWorldAcceleration(): AccelerometerData{
        val resultVector = FloatArray(4) {0f}
        // Rotation matrices
        val R = FloatArray(16)
        val I = FloatArray(16)
        if (SensorManager.getRotationMatrix(R, I, gravityValues, magnetValues)) {
            val inv = FloatArray(16)
            android.opengl.Matrix.invertM(inv, 0, R, 0)
            android.opengl.Matrix.multiplyMV(resultVector, 0, inv, 0, linearAccValues, 0)
        }
        /*for (i in resultVector.indices){
            if (resultVector[i] > -threshold && resultVector[i] < threshold) resultVector[i] = 0f
        }*/
        if (resultVector[0] > -DETECTION_THRESHOLD_X && resultVector[0] < DETECTION_THRESHOLD_X) resultVector[0] = 0f
        if (resultVector[1] > -DETECTION_THRESHOLD_Y && resultVector[1] < DETECTION_THRESHOLD_Y) resultVector[1] = 0f
        if (resultVector[2] > -DETECTION_THRESHOLD_Z && resultVector[2] < DETECTION_THRESHOLD_Z) resultVector[2] = 0f

        return AccelerometerData(-resultVector[0], -resultVector[1], -resultVector[2])
    }

    private fun evaluateAcc(acc: Float, threshold: Double): Movement{
        return when {
            acc < -threshold -> Movement.NEGATIVE
            acc > threshold -> Movement.POSITIVE
            else -> Movement.NONE
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int){}
}