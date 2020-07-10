package bachelor.test.locationapp.presenter.positioning

import android.content.Context
import android.hardware.SensorManager

private const val ACC_DETECTION_THRESHOLD_X = 0.05
private const val ACC_DETECTION_THRESHOLD_Y = 0.05
private const val ACC_DETECTION_THRESHOLD_Z = 0.25

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
    private var gravityValues = FloatArray(4) {0f}
    private var linearAccValues = FloatArray(4) {0f}
    private var magnetValues = FloatArray(3)

    // IMU calculations relevant variables
    private var initialTimestamp = System.currentTimeMillis()
    private var previousAccelerationData = AccelerationData(0f, 0f, 0f, 0f)
    private var previousVelocityData = VelocityData(0f, 0f, 0f, 0f)
    private var previousDisplacementData = DisplacementData(0f, 0f, 0f)

    fun start(){
        sensorEventListenerImpl.registerListener()
    }

    fun stop(){
        sensorEventListenerImpl.unregisterListener()
    }

    fun getDisplacementData(): DisplacementData {
        val displacementData = previousDisplacementData
        return displacementData
    }

    fun resetMemberVariablesForNextIteration() {
        initialTimestamp = System.currentTimeMillis()
        previousAccelerationData = AccelerationData(0f, 0f, 0f, 0f)
        previousVelocityData = VelocityData(0f, 0f, 0f, 0f)
        previousDisplacementData = DisplacementData(0f, 0f, 0f)
    }

    override fun onGravitySensorUpdate(values: FloatArray) {
        gravityValues[0] = values[0]
        gravityValues[1] = values[1]
        gravityValues[2] = values[2]
        calculateAcceleration()
    }

    override fun onLinearAccelerometerUpdate(values: FloatArray) {
        linearAccValues[0] = values[0]
        linearAccValues[1] = values[1]
        linearAccValues[2] = values[2]
        calculateAcceleration()
    }

    override fun onMagnetometerUpdate(values: FloatArray) {
        magnetValues = values
        calculateAcceleration()
    }

    @Synchronized
    private fun calculateAcceleration() {
        // Rotation matrices
        val R = FloatArray(16)
        val I = FloatArray(16)
        if (SensorManager.getRotationMatrix(R, I, gravityValues, magnetValues)) {
            val resultVector = FloatArray(4) {0f}
            val inv = FloatArray(16)
            android.opengl.Matrix.invertM(inv, 0, R, 0)
            android.opengl.Matrix.multiplyMV(resultVector, 0, inv, 0, linearAccValues, 0)
            resultVector[0] = eliminateNoise(resultVector[0], ACC_DETECTION_THRESHOLD_X)
            resultVector[1] = eliminateNoise(resultVector[1], ACC_DETECTION_THRESHOLD_Y)
            resultVector[2] = eliminateNoise(resultVector[2], ACC_DETECTION_THRESHOLD_Z)

            val currentTimestamp: Float = (System.currentTimeMillis() - initialTimestamp).toFloat() / 1000
            // Negating values in order to have positive values in North, East and Up directions.
            val accelerationData = AccelerationData(-resultVector[0], -resultVector[1], -resultVector[2], currentTimestamp)
            calculateDisplacement(accelerationData)
            outputListener.onWorldAccelerationCalculated(accelerationData)
        }
    }

    private fun eliminateNoise(acc: Float, threshold: Double): Float {
        return if (acc > -threshold && acc < threshold) 0f else acc
    }

    private fun calculateDisplacement(accelerationData: AccelerationData){
        calculateVelocity(accelerationData)
    }

    private fun calculateVelocity(accelerationData: AccelerationData){
        val xVelocity = previousVelocityData.xVel + ((accelerationData.xAcc + previousAccelerationData.xAcc) / 2) * (accelerationData.timestamp - previousAccelerationData.timestamp)
        val yVelocity = previousVelocityData.yVel + ((accelerationData.yAcc + previousAccelerationData.yAcc) / 2) * (accelerationData.timestamp - previousAccelerationData.timestamp)
        val zVelocity = previousVelocityData.zVel + ((accelerationData.zAcc + previousAccelerationData.zAcc) / 2) * (accelerationData.timestamp - previousAccelerationData.timestamp)
        previousAccelerationData = AccelerationData(accelerationData.xAcc, accelerationData.yAcc, accelerationData.zAcc, accelerationData.timestamp)
        val velocityData = VelocityData(xVelocity, yVelocity, zVelocity, accelerationData.timestamp)
        calculateDisplacement(velocityData)
    }

    private fun calculateDisplacement(velocityData: VelocityData){
        val xDisplacement = previousDisplacementData.xDisplacement + ((velocityData.xVel + previousVelocityData.xVel) / 2) * (velocityData.timestamp - previousVelocityData.timestamp)
        val yDisplacement = previousDisplacementData.yDisplacement + ((velocityData.yVel + previousVelocityData.yVel) / 2) * (velocityData.timestamp - previousVelocityData.timestamp)
        val zDisplacement = previousDisplacementData.zDisplacement + ((velocityData.zVel + previousVelocityData.zVel) / 2) * (velocityData.timestamp - previousVelocityData.timestamp)
        previousVelocityData = VelocityData(velocityData.xVel, velocityData.yVel, velocityData.zVel, velocityData.timestamp)
        previousDisplacementData = DisplacementData(xDisplacement, yDisplacement, zDisplacement)
    }
}