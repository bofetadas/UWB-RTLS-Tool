package bachelor.test.locationapp.presenter.positioning

import android.content.Context
import android.hardware.SensorManager
import java.util.*
import kotlin.collections.ArrayList

private const val DETECTION_THRESHOLD_X = 0.25
private const val DETECTION_THRESHOLD_Y = 0.25
private const val DETECTION_THRESHOLD_Z = 0.5

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

    // World acceleration values
    private var worldAccValues = Collections.synchronizedList(ArrayList<AccelerationData>())

    fun start(){
        sensorEventListenerImpl.registerListener()
    }

    fun stop(){
        sensorEventListenerImpl.unregisterListener()
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

        return MovementData(
            evaluateAcc(xAccMean, DETECTION_THRESHOLD_X),
            evaluateAcc(yAccMean, DETECTION_THRESHOLD_Y),
            evaluateAcc(zAccMean, DETECTION_THRESHOLD_Z))
    }

    override fun onGravitySensorUpdate(values: FloatArray){
        gravityValues[0] = values[0]
        gravityValues[1] = values[1]
        gravityValues[2] = values[2]
        calculateWorldAcceleration()
    }

    override fun onLinearAccelerometerUpdate(values: FloatArray){
        linearAccValues[0] = values[0]
        linearAccValues[1] = values[1]
        linearAccValues[2] = values[2]
        calculateWorldAcceleration()
    }

    override fun onMagnetometerUpdate(values: FloatArray){
        magnetValues = values
        calculateWorldAcceleration()
    }

    private fun calculateWorldAcceleration() {
        val resultVector = FloatArray(4) {0f}
        // Rotation matrices
        val R = FloatArray(16)
        val I = FloatArray(16)
        if (SensorManager.getRotationMatrix(R, I, gravityValues, magnetValues)) {
            val inv = FloatArray(16)
            android.opengl.Matrix.invertM(inv, 0, R, 0)
            android.opengl.Matrix.multiplyMV(resultVector, 0, inv, 0, linearAccValues, 0)
        }
        resultVector[0] = eliminateNoise(resultVector[0], DETECTION_THRESHOLD_X)
        resultVector[1] = eliminateNoise(resultVector[1], DETECTION_THRESHOLD_Y)
        resultVector[2] = eliminateNoise(resultVector[2], DETECTION_THRESHOLD_Z)

        // Negating values in order to have positive values in North, East and Up directions.
        val accelerationData = AccelerationData(-resultVector[0], -resultVector[1], -resultVector[2])
        worldAccValues.add(accelerationData)
        outputListener.onWorldAccelerationCalculated(accelerationData)
    }

    private fun eliminateNoise(acc: Float, threshold: Double): Float {
        return if (acc > -threshold && acc < threshold) 0f else acc
    }

    private fun evaluateAcc(acc: Float, threshold: Double): Movement {
        return when {
            acc < -threshold -> Movement.NEGATIVE
            acc > threshold -> Movement.POSITIVE
            else -> Movement.NONE
        }
    }
}