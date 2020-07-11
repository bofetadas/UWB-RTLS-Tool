package bachelor.test.locationapp.presenter.positioning

import android.content.Context
import bachelor.test.locationapp.view.MainScreenContract

private const val POSITION_LOCATION_BYTE_ARRAY_SIZE = 14
private const val UWB_X_WEIGHT = 1f
private const val UWB_Y_WEIGHT = 1f
private const val UWB_Z_WEIGHT = 0.8f
private const val IMU_X_WEIGHT = 1f - UWB_X_WEIGHT
private const val IMU_Y_WEIGHT = 1f - UWB_Y_WEIGHT
private const val IMU_Z_WEIGHT = 1f - UWB_Z_WEIGHT

// Entry class for handling positioning logic
class PositioningImpl(context: Context, private val presenter: MainScreenContract.Presenter): Positioning, IMUOutputListener {

    private val converter = ByteArrayToLocationDataConverter()
    private val imu = IMU(context, this)
    private var previousLocation = LocationData()

    override fun startIMU() {
        imu.start()
    }

    override fun stopIMU() {
        imu.stop()
    }

    override fun calculateLocation(byteArray: ByteArray) {
        val location = if (byteArray.size == POSITION_LOCATION_BYTE_ARRAY_SIZE) {
            converter.getLocationFromByteArray(byteArray)
        } else {
            previousLocation
        }
        val displacement = imu.getDisplacementData()
        imu.resetMemberVariablesForNextIteration()
        val filteredLocation = mergeLocationDataWithDisplacementData(location, displacement)
        presenter.onLocationUpdate(filteredLocation)
    }

    // IMU callback
    override fun onAccelerationCalculated(accelerationData: AccelerationData) {
        presenter.onAccelerometerUpdate(accelerationData)
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
}