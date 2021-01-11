package maxbauer.uwbrtls.tool.presenter.positioning

import android.content.Context
import maxbauer.uwbrtls.tool.utils.CompassUtil
import maxbauer.uwbrtls.tool.view.MainScreenContract

private const val POSITION_BYTE_ARRAY_SIZE = 14

// Entry class for handling positioning logic
class PositioningImpl(context: Context, private val presenter: MainScreenContract.Presenter): Positioning,
    KalmanFilterOutputListener {

    private val converter = ByteArrayToLocationDataConverter()
    private val imu = IMU(context)
    private val kalmanFilterImpl = KalmanFilterImpl(this)
    private val kalmanFilterImplStrategies = KalmanFilterImplStrategies()
    private var kalmanFilterImplStrategy: (uwbLocationData: LocationData, accelerationData: AccelerationData, orientationData: OrientationData) -> Unit = kalmanFilterImplStrategies.configureStrategy

    override fun startIMU() {
        imu.start()
    }

    override fun stopIMU() {
        imu.stop()
    }

    override fun calculateLocation(byteArray: ByteArray) {
        if (byteArray.size != POSITION_BYTE_ARRAY_SIZE) return
        val uwbLocation = converter.getUWBLocationFromByteArray(byteArray)
        val imuData = imu.getIMUData()
        val imuAcceleration = imuData.accelerationData
        val imuOrientation = imuData.orientationData
        val currentCompassDirection = CompassUtil.getCompassDirection(imuOrientation.yaw)
        kalmanFilterImplStrategy.invoke(uwbLocation, imuAcceleration, imuOrientation)
        presenter.onAccelerometerUpdate(imuData.accelerationData)
        presenter.onOrientationUpdate(imuData.orientationData)
        presenter.onCompassDirectionUpdate(currentCompassDirection)
    }

    override fun resetKalmanFilter() {
        kalmanFilterImplStrategy = kalmanFilterImplStrategies.configureStrategy
    }

    // Kalman Filter callback
    override fun onNewStateVectorEstimate(uwbLocationData: LocationData, filteredLocationData: LocationData, rawAccelerationData: AccelerationData, filteredAccelerationData: AccelerationData) {
        presenter.onNewStateVectorEstimate(uwbLocationData, filteredLocationData, rawAccelerationData, filteredAccelerationData)
    }

    private inner class KalmanFilterImplStrategies {
        val configureStrategy: (uwbLocationData: LocationData, accData: AccelerationData, orientationData: OrientationData) -> Unit = { uwbLocationData, _, _ ->
            kalmanFilterImpl.configure(uwbLocationData)
            kalmanFilterImplStrategy = estimateStrategy
        }

        val estimateStrategy: (uwbLocationData: LocationData, accelerationData: AccelerationData, orientationData: OrientationData) -> Unit = { uwbLocationData, accelerationData, orientationData ->
            kalmanFilterImpl.predict(accelerationData)
            kalmanFilterImpl.update(uwbLocationData, accelerationData, orientationData)
        }
    }
}