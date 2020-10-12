package bachelor.test.locationapp.presenter.positioning

import android.content.Context
import bachelor.test.locationapp.utils.ByteArrayUtil
import bachelor.test.locationapp.utils.CompassUtil
import bachelor.test.locationapp.view.MainScreenContract

const val POSITION_BYTE_ARRAY_SIZE = 14

// Entry class for handling positioning logic
class PositioningImpl(context: Context, private val presenter: MainScreenContract.Presenter): Positioning, KalmanFilterOutputListener {

    private val imu = IMU(context)
    private val kalmanFilterImpl = KalmanFilterImpl(this)

    override fun initialize(initLocationData: LocationData) {
        imu.start()
        kalmanFilterImpl.configure(initLocationData)
    }

    override fun stop() {
        imu.stop()
    }

    override fun calculateLocation(byteArray: ByteArray) {
        if (byteArray.size != POSITION_BYTE_ARRAY_SIZE) return
        val uwbLocation = ByteArrayUtil.getUWBLocationFromByteArray(byteArray)
        val imuData = imu.getIMUData()
        val imuAcceleration = imuData.accelerationData
        val imuOrientation = imuData.orientationData
        val currentCompassDirection = CompassUtil.getCompassDirection(imuOrientation.yaw)

        kalmanFilterImpl.predict(imuAcceleration)
        kalmanFilterImpl.update(uwbLocation, imuAcceleration, imuOrientation)

        presenter.onAccelerometerUpdate(imuData.accelerationData)
        presenter.onOrientationUpdate(imuData.orientationData)
        presenter.onCompassDirectionUpdate(currentCompassDirection)
    }

    // Kalman Filter callback
    override fun onNewStateVectorEstimate(uwbLocationData: LocationData, filteredLocationData: LocationData, rawAccelerationData: AccelerationData, filteredAccelerationData: AccelerationData) {
        presenter.onNewStateVectorEstimate(uwbLocationData, filteredLocationData, rawAccelerationData, filteredAccelerationData)
    }
}