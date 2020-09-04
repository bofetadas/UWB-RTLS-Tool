package bachelor.test.locationapp.presenter.positioning

import android.content.Context
import bachelor.test.locationapp.view.MainScreenContract

private const val POSITION_BYTE_ARRAY_SIZE = 14

// Entry class for handling positioning logic
class PositioningImpl(context: Context, private val presenter: MainScreenContract.Presenter): Positioning, IMUOutputListener, KalmanFilterOutputListener {

    private val converter = ByteArrayToLocationDataConverter()
    private val imu = IMU(context, this)
    private val kalmanFilterImpl = KalmanFilterImpl(this)
    private val kalmanFilterImplStrategies = KalmanFilterImplStrategies()
    private var kalmanFilterImplStrategy: (locationData: LocationData, accelerationData: AccelerationData) -> Unit = kalmanFilterImplStrategies.configureStrategy

    override fun startIMU() {
        imu.start()
    }

    override fun stopIMU() {
        imu.stop()
    }

    override fun calculateLocation(byteArray: ByteArray) {
        if (byteArray.size != POSITION_BYTE_ARRAY_SIZE) return
        val uwbLocation = converter.getLocationFromByteArray(byteArray)
        val imuAcceleration = imu.calculateAcceleration()
        kalmanFilterImplStrategy.invoke(uwbLocation, imuAcceleration)
    }

    // IMU callback
    override fun onAccelerationCalculated(accelerationData: AccelerationData) {
        presenter.onAccelerometerUpdate(accelerationData)
    }
    // Kalman Filter callback
    override fun onNewEstimate(locationData: LocationData) {
        presenter.onLocationUpdate(locationData)
        //UnityPlayer.UnitySendMessage("BluetoothLE", "onMessageReceived", filteredLocation.toString())
    }

    private inner class KalmanFilterImplStrategies {
        val configureStrategy: (locationData: LocationData, accData: AccelerationData) -> Unit = { locationData, _ ->
            kalmanFilterImpl.configure(locationData)
            kalmanFilterImplStrategy = estimateStrategy
        }

        val estimateStrategy: (locationData: LocationData, accelerationData: AccelerationData) -> Unit = { locationData, accelerationData ->
            kalmanFilterImpl.predict(accelerationData)
            kalmanFilterImpl.correct(locationData, accelerationData)
        }
    }
}