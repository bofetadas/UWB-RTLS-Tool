package bachelor.test.locationapp.presenter.positioning

import android.content.Context
import android.hardware.SensorManager

private const val ACC_DETECTION_THRESHOLD_X = 0.05f
private const val ACC_DETECTION_THRESHOLD_Y = 0.05f
private const val ACC_DETECTION_THRESHOLD_Z = 0.1f
private const val GRAVITY_HAMBURG = 9.81399f

/*
 * This class' purpose is to figure out whether or not the mobile phone experienced movement on any
 * of the three world axis.
 * It calculates the movement on the device's axises and translates this into the world coordinate system.
 * This information is used to determine later whether new position data coming from the tag should
 * be applied or dropped.
 */
class IMU(context: Context, private val outputListener: IMUOutputListener): IMUInputListener {

    private val sensorEventListenerImpl = SensorEventListenerImpl(context, this)
    // Sensor values arrays
    private var gravityValues = FloatArray(3) {0f}
    private var linearAccValues = FloatArray(4) {0f}
    private var accValues = FloatArray(3) {0f}
    private var magnetValues = FloatArray(3)

    fun start(){
        sensorEventListenerImpl.registerListener()
    }

    fun stop(){
        sensorEventListenerImpl.unregisterListener()
    }

    override fun onGravitySensorUpdate(values: FloatArray) {
        gravityValues[0] = values[0]
        gravityValues[1] = values[1]
        gravityValues[2] = values[2]
    }

    override fun onLinearAccelerometerUpdate(values: FloatArray) {
        linearAccValues[0] = values[0]
        linearAccValues[1] = values[1]
        linearAccValues[2] = values[2]
    }

    override fun onAccelerometerUpdate(values: FloatArray) {
        accValues = values
    }

    override fun onMagnetometerUpdate(values: FloatArray) {
        magnetValues = values
    }

    /*@Synchronized
    fun calculateAcceleration(): AccelerationData {
        // Rotation matrices
        val R = FloatArray(16)
        val I = FloatArray(16)
        var accelerationData = AccelerationData()
        if (SensorManager.getRotationMatrix(R, I, gravityValues, magnetValues)) {
            val resultVector = FloatArray(4) {0f}
            val inv = FloatArray(16)
            android.opengl.Matrix.invertM(inv, 0, R, 0)
            android.opengl.Matrix.multiplyMV(resultVector, 0, inv, 0, linearAccValues, 0)
            resultVector[0] = eliminateNoise(resultVector[0], ACC_DETECTION_THRESHOLD_X)
            resultVector[1] = eliminateNoise(resultVector[1], ACC_DETECTION_THRESHOLD_Y)
            resultVector[2] = eliminateNoise(resultVector[2], ACC_DETECTION_THRESHOLD_Z)

            // Negating values in order to have positive values in North, East and Up directions.
            accelerationData = AccelerationData(-resultVector[0].toDouble(), -resultVector[1].toDouble(), -resultVector[2].toDouble(), System.currentTimeMillis().toFloat())
            outputListener.onAccelerationCalculated(accelerationData)
        }
        return accelerationData
    }*/

    @Synchronized
    fun calculateAcceleration(): AccelerationData {
        /*val timestampBeforeAccelerationCalculation = System.currentTimeMillis() - initialTimestamp
        println("Before: $timestampBeforeAccelerationCalculation")*/
        // Rotation matrices
        val R = FloatArray(9)
        //val Rinv = FloatArray(16)
        val absoluteAcceleration = FloatArray(3)
        //Calculate rotation matrix from gravity and magnetic sensor data
        SensorManager.getRotationMatrix(R, null, gravityValues, magnetValues)

        //World coordinate system transformation for acceleration
        absoluteAcceleration[0] = R[0] * accValues[0] + R[1] * accValues[1] + R[2] * accValues[2]
        absoluteAcceleration[1] = R[3] * accValues[0] + R[4] * accValues[1] + R[5] * accValues[2]
        absoluteAcceleration[2] = R[6] * accValues[0] + R[7] * accValues[1] + R[8] * accValues[2]
        val accelerationData = AccelerationData(-absoluteAcceleration[0].toDouble(), -absoluteAcceleration[1].toDouble(), -absoluteAcceleration[2].toDouble() + GRAVITY_HAMBURG, System.currentTimeMillis().toFloat())
        /*val Q = FloatArray(4)
        val sensorDataVector = floatArrayOf(gravityValues[0], gravityValues[1], gravityValues[2], gyroscopeValues[0], gyroscopeValues[1], gyroscopeValues[2], magnetValues[0], magnetValues[1], magnetValues[2])
        //val I = FloatArray(9)
        SensorManager.getQuaternionFromVector(Q, sensorDataVector)
        SensorManager.getRotationMatrixFromVector(R, Q)
        android.opengl.Matrix.invertM(Rinv, 0, R, 0)
        android.opengl.Matrix.multiplyMV(absoluteAcceleration, 0, Rinv, 0, linearAccValues, 0)
        val accelerationData = AccelerationData(-resultVector[0], -resultVector[1], -resultVector[2], System.currentTimeMillis().toFloat())*/
        /*val timestampAfterAccelerationCalculation = System.currentTimeMillis() - initialTimestamp
        println("After: $timestampAfterAccelerationCalculation")
        val accelerationCalculationLength = (timestampAfterAccelerationCalculation - timestampBeforeAccelerationCalculation) / 1000.0
        println("Acceleration Calculation Length: ${accelerationCalculationLength}s")*/
        outputListener.onAccelerationCalculated(accelerationData)
        return accelerationData
    }

    private fun eliminateNoise(acc: Float, threshold: Float): Float {
        return if (acc > -threshold && acc < threshold) 0f else acc
    }
}