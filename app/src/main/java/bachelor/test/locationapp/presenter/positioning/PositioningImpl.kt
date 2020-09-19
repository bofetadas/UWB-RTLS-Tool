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
    private var kalmanFilterImplStrategy: (uwbLocationData: LocationData, accelerationData: AccelerationData) -> Unit = kalmanFilterImplStrategies.configureStrategy

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
    override fun onNewEstimate(uwbLocationData: LocationData, filteredLocationData: LocationData) {
        presenter.onLocationUpdate(uwbLocationData, filteredLocationData)
        //UnityPlayer.UnitySendMessage("BluetoothLE", "onMessageReceived", filteredLocation.toString())
    }

    private inner class KalmanFilterImplStrategies {
        val configureStrategy: (uwbLocationData: LocationData, accData: AccelerationData) -> Unit = { uwbLocationData, _ ->
            kalmanFilterImpl.configure(uwbLocationData)
            kalmanFilterImplStrategy = estimateStrategy
        }

        val estimateStrategy: (uwbLocationData: LocationData, accelerationData: AccelerationData) -> Unit = { uwbLocationData, accelerationData ->
            kalmanFilterImpl.predict(accelerationData)
            kalmanFilterImpl.correct(uwbLocationData, accelerationData)
        }
    }
}