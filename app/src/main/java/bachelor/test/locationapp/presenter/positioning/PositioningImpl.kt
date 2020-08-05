package bachelor.test.locationapp.presenter.positioning

import android.content.Context
import bachelor.test.locationapp.view.MainScreenContract

private const val UWB_X_WEIGHT = 1f
private const val UWB_Y_WEIGHT = 1f
private const val UWB_Z_WEIGHT = 0.8f
private const val IMU_X_WEIGHT = 1f - UWB_X_WEIGHT
private const val IMU_Y_WEIGHT = 1f - UWB_Y_WEIGHT
private const val IMU_Z_WEIGHT = 1f - UWB_Z_WEIGHT

// Entry class for handling positioning logic
class PositioningImpl(context: Context, private val presenter: MainScreenContract.Presenter): Positioning, IMUOutputListener, KalmanFilterCallback {

    private var previousLocation = LocationData()
    private val converter = ByteArrayToLocationDataConverter()
    private val imu = IMU(context, this)
    private val kalmanFilterImpl = KalmanFilterImpl(this)
    private val kalmanFilterImplStrategies = KalmanFilterImplStrategies()
    private var kalmanFilterImplStrategy: (locationData: LocationData, accData: AccelerationData) -> Unit = kalmanFilterImplStrategies.configureStrategy

    override fun startIMU() {
        imu.start()
    }

    override fun stopIMU() {
        imu.stop()
    }

    override fun calculateLocation(byteArray: ByteArray) {
        //val displacement = imu.getDisplacementData()
        //imu.resetMemberVariablesForNextIteration()
        val uwbLocation = converter.getLocationFromByteArray(byteArray)
        val imuAcceleration = imu.calculateAcceleration()
        kalmanFilterImplStrategy.invoke(uwbLocation, imuAcceleration)

        //kalmanFilterImpl.predict(controlVector)
        //kalmanFilterImpl.correct(location)
        //val filteredLocation = mergeLocationDataWithDisplacementData(location, displacement)
    }

    // IMU callback
    override fun onAccelerationCalculated(accelerationData: AccelerationData) {
        presenter.onAccelerometerUpdate(accelerationData)
    }
    // Kalman Filter callback
    override fun onNewEstimate(locationData: LocationData) {
        presenter.onLocationUpdate(locationData)
    }

    private fun mergeLocationDataWithDisplacementData(location: LocationData, displacement: DisplacementData): LocationData {
        val xDifference = previousLocation.xPos - location.xPos
        val yDifference = previousLocation.yPos - location.yPos
        val zDifference = previousLocation.zPos - location.zPos
        // X evaluation
        val filteredXPos = previousLocation.xPos - (UWB_X_WEIGHT * xDifference + IMU_X_WEIGHT * displacement.xDispl)
        // Y evaluation
        val filteredYPos = previousLocation.yPos - (UWB_Y_WEIGHT * yDifference + IMU_Y_WEIGHT * displacement.yDispl)
        // Z evaluation
        val filteredZPos = previousLocation.zPos - (UWB_Z_WEIGHT * zDifference + IMU_Z_WEIGHT * displacement.zDispl)
        previousLocation = LocationData(filteredXPos, filteredYPos, filteredZPos, location.qualityFactor)
        return previousLocation
    }

    private inner class KalmanFilterImplStrategies {
        val configureStrategy: (locationData: LocationData, accData: AccelerationData) -> Unit = { locationData, _ ->
            kalmanFilterImpl.configure(locationData)
            kalmanFilterImplStrategy = estimateStrategy
        }

        val estimateStrategy: (locationData: LocationData, accelerationData: AccelerationData) -> Unit = { locationData, accData ->
            kalmanFilterImpl.predict()
            kalmanFilterImpl.correct(locationData, accData)
        }
    }
}