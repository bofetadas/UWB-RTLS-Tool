package bachelor.test.locationapp.presenter.positioning

import android.content.Context
import android.hardware.SensorManager

private const val GRAVITY = 9.81399f
private const val DECLINATION = 3.39
private const val LOW_PASS_FILTER_ALPHA = 0.1f

/*
 * This class' purpose is to figure out whether or not the mobile phone experienced movement on any
 * of the three world axis.
 * It calculates the movement on the device's axises and translates this into the world coordinate system.
 * This information is used to determine later whether new position data coming from the tag should
 * be applied or dropped.
 */
class IMU(context: Context): IMUInputListener {

    private val sensorEventListenerImpl = SensorEventListenerImpl(context, this)
    // Sensor values arrays
    private var accelerationValues = FloatArray(3)
    private var gravityValues = FloatArray(3)
    private var gravityValuesLowPass = FloatArray(3)
    private var magnetValues = FloatArray(3)
    private var magnetValuesLowPass = FloatArray(3)

    fun start(){
        sensorEventListenerImpl.registerListener()
    }

    fun stop(){
        sensorEventListenerImpl.unregisterListener()
    }

    override fun onAccelerometerUpdate(values: FloatArray) {
        accelerationValues = values
    }

    override fun onGravitySensorUpdate(values: FloatArray) {
        gravityValues = values
        gravityValuesLowPass = lowPass(values, gravityValuesLowPass)
    }

    override fun onMagnetometerUpdate(values: FloatArray) {
        magnetValues = values
        magnetValuesLowPass = lowPass(values, magnetValuesLowPass)
    }

    fun getIMUAcceleration(): AccelerationData {
        // Calculate world acceleration and orientation
        val acceleration = calculateAcceleration()
        return acceleration
    }

    // Transform device acceleration into world acceleration
    private fun calculateAcceleration(): AccelerationData {
        // Calculate rotation matrix from gravity and magnetic sensor data
        val rotationMatrix = FloatArray(9)
        SensorManager.getRotationMatrix(rotationMatrix, null, gravityValues, magnetValues)
        // Multiply measured acceleration with rotation matrix to get acceleration relative to NEU
        val xAcc = rotationMatrix[0] * accelerationValues[0] + rotationMatrix[1] * accelerationValues[1] + rotationMatrix[2] * accelerationValues[2]
        val yAcc = rotationMatrix[3] * accelerationValues[0] + rotationMatrix[4] * accelerationValues[1] + rotationMatrix[5] * accelerationValues[2]
        val zAcc = rotationMatrix[6] * accelerationValues[0] + rotationMatrix[7] * accelerationValues[1] + rotationMatrix[8] * accelerationValues[2]
        return AccelerationData(xAcc.toDouble(), yAcc.toDouble(), zAcc.toDouble() - GRAVITY)
    }

    private fun lowPass(inputValues: FloatArray, outputValues: FloatArray): FloatArray {
        for (i in inputValues.indices) {
            outputValues[i] = outputValues[i] + LOW_PASS_FILTER_ALPHA * (inputValues[i] - outputValues[i])
        }
        return outputValues
    }
}