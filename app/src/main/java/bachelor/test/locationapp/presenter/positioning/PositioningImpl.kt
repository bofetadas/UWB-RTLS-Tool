package bachelor.test.locationapp.presenter.positioning

import android.content.Context
import bachelor.test.locationapp.view.MainScreenContract
import kotlin.math.abs

private const val POSITION_BYTE_ARRAY_SIZE = 14
// 1m/s**2 standard deviation from static position
private const val STANDARD_DEVIATION = 0.01
// Factor to scale std if needed in noisy environments
private const val FACTOR = 25

// Entry class for handling positioning logic
class PositioningImpl(context: Context, private val presenter: MainScreenContract.Presenter): Positioning, KalmanFilterOutputListener {

    private val converter = ByteArrayToLocationDataConverter()
    private val imu = IMU(context)
    private val kalmanFilterImpl = KalmanFilterImpl(this)
    private val kalmanFilterImplStrategies = KalmanFilterImplStrategies()
    private var kalmanFilterImplStrategy: (uwbLocationData: LocationData, accelerationData: AccelerationData) -> Unit = kalmanFilterImplStrategies.configureStrategy

    override fun startIMU() {
        imu.start()
    }

    override fun stopIMU() {
        imu.stop()
    }

    override fun calculateLocation(byteArray: ByteArray) {
        if (byteArray.size != POSITION_BYTE_ARRAY_SIZE) return
        val uwbLocation = converter.getUWBLocationFromByteArray(byteArray)
        val imuAcceleration = imu.getIMUAcceleration()
        kalmanFilterImplStrategy.invoke(uwbLocation, imuAcceleration)
    }

    override fun resetKalmanFilter() {
        kalmanFilterImplStrategy = kalmanFilterImplStrategies.configureStrategy
    }

    // Kalman Filter callback
    override fun onNewStateVectorEstimate(uwbLocationData: LocationData, filteredLocationData: LocationData, rawAccelerationData: AccelerationData, filteredAccelerationData: AccelerationData) {
        presenter.onMovementDetected(abs(filteredAccelerationData.yAcc) > FACTOR * STANDARD_DEVIATION)
    }

    private inner class KalmanFilterImplStrategies {
        val configureStrategy: (uwbLocationData: LocationData, accData: AccelerationData) -> Unit = { uwbLocationData, _ ->
            kalmanFilterImpl.configure(uwbLocationData)
            kalmanFilterImplStrategy = estimateStrategy
        }

        val estimateStrategy: (uwbLocationData: LocationData, accelerationData: AccelerationData) -> Unit = { uwbLocationData, accelerationData ->
            kalmanFilterImpl.predict(accelerationData)
            kalmanFilterImpl.update(uwbLocationData, accelerationData)
        }
    }
}