package bachelor.test.locationapp.presenter.positioning

import android.content.Context
import bachelor.test.locationapp.view.MainScreenContract

private const val POSITION_LOCATION_BYTE_ARRAY_SIZE = 14
private const val UWB_X_WEIGHT = 1f
private const val UWB_Y_WEIGHT = 1f
private const val UWB_Z_WEIGHT = 0.5f

// Entry class for handling positioning logic
class PositioningImpl(context: Context, private val presenter: MainScreenContract.Presenter): Positioning, IMUOutputListener {

    private val converter = ByteArrayToLocationDataConverter()
    private val imu = IMU(context, this)
    private var previousLocation = LocationData(1f, 2.7f, 1.7f, -1)

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
    override fun onWorldAccelerationCalculated(accelerationData: AccelerationData) {
        presenter.onAccelerometerUpdate(accelerationData)
    }

    private fun mergeLocationDataWithDisplacementData(location: LocationData, displacement: DisplacementData): LocationData {
        val xDifference = previousLocation.xPos - location.xPos
        val yDifference = previousLocation.yPos - location.yPos
        val zDifference = previousLocation.zPos - location.zPos
        /*println("X Displacement UWB: $xDifference")
        println("X Displacement IMU: ${displacement.xDisplacement}")
        println("Y Displacement UWB: $yDifference")
        println("Y Displacement IMU: ${displacement.yDisplacement}")
        println("Z Displacement UWB: $zDifference")
        println("Z Displacement IMU: ${displacement.zDisplacement}")*/
        // X evaluation
        val filteredXPos = previousLocation.xPos - (UWB_X_WEIGHT * xDifference + (1 - UWB_X_WEIGHT) * displacement.xDisplacement)
        // Y evaluation
        val filteredYPos = previousLocation.yPos - (UWB_Y_WEIGHT * yDifference + (1 - UWB_Y_WEIGHT) * displacement.yDisplacement)
        // Z evaluation
        val filteredZPos = previousLocation.zPos - (UWB_Z_WEIGHT * zDifference + (1 - UWB_Z_WEIGHT) * displacement.zDisplacement)
        println("\n\n")
        /*println("FILTERED X: $filteredXPos")
        println("UWB X: ${location.xPos}")
        println("IMU X: ${previousLocation.xPos + displacement.xDisplacement}")
        println("FILTERED Y: $filteredYPos")
        println("UWB Y: ${location.yPos}")
        println("IMU Y: ${previousLocation.yPos + displacement.yDisplacement}")*/
        println("FILTERED Z: $filteredZPos")
        println("UWB Z: ${location.zPos}")
        println("IMU Z: ${previousLocation.zPos + displacement.zDisplacement}")
        previousLocation.xPos = location.xPos
        previousLocation.yPos = location.yPos
        previousLocation.zPos = location.zPos
        return LocationData(filteredXPos, filteredYPos, filteredZPos, location.qualityFactor)
    }
}