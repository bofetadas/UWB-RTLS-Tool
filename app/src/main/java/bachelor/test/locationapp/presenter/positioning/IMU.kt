package bachelor.test.locationapp.presenter.positioning

import android.content.Context
import android.hardware.SensorManager

private const val GRAVITY = 9.81399f
private const val DECLINATION = 3.39

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
    private var accelerationValues = FloatArray(3) {0f}
    private var gravityValues = FloatArray(3) {0f}
    private var magnetValues = FloatArray(3)

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
    }

    override fun onMagnetometerUpdate(values: FloatArray) {
        magnetValues = values
    }

    @Synchronized
    fun getIMUData(): IMUData {
        // Calculate rotation matrix from gravity and magnetic sensor data
        val rotationMatrix = FloatArray(9)
        SensorManager.getRotationMatrix(rotationMatrix, null, gravityValues, magnetValues)

        // Calculate world acceleration and orientation
        val acceleration = calculateAcceleration(rotationMatrix)
        val orientation = calculateOrientation(rotationMatrix)
        return IMUData(acceleration, orientation)
    }

    // Transform device acceleration into world acceleration
    private fun calculateAcceleration(rotationMatrix: FloatArray): AccelerationData {
        val acceleration = FloatArray(3)
        acceleration[0] = rotationMatrix[0] * accelerationValues[0] + rotationMatrix[1] * accelerationValues[1] + rotationMatrix[2] * accelerationValues[2]
        acceleration[1] = rotationMatrix[3] * accelerationValues[0] + rotationMatrix[4] * accelerationValues[1] + rotationMatrix[5] * accelerationValues[2]
        acceleration[2] = rotationMatrix[6] * accelerationValues[0] + rotationMatrix[7] * accelerationValues[1] + rotationMatrix[8] * accelerationValues[2]
        return AccelerationData(-acceleration[0].toDouble(), -acceleration[1].toDouble(), -acceleration[2].toDouble() + GRAVITY)
    }

    // Transform device orientation into world orientation
    private fun calculateOrientation(rotationMatrix: FloatArray): OrientationData {
        val orientation = FloatArray(3)
        SensorManager.getOrientation(rotationMatrix, orientation)
        val yaw = Math.toDegrees(orientation[0].toDouble()) + DECLINATION
        val pitch = Math.toDegrees(orientation[1].toDouble())
        val roll = Math.toDegrees(orientation[2].toDouble())
        return OrientationData(yaw, pitch, roll)
    }
}