package bachelor.test.locationapp.presenter.positioning

import android.content.Context
import bachelor.test.locationapp.view.MainScreenContract

private const val POSITION_LOCATION_BYTE_ARRAY_SIZE = 14

// Entry class for handling positioning logic
class PositioningImpl(context: Context, private val presenter: MainScreenContract.Presenter): Positioning, IMUOutputListener {

    private val converter = ByteArrayToLocationDataConverter()
    private val imu = IMU(context, this)
    private var previousLocation = LocationData(1.8, 1.7, 1.7, -1)

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
        val movement = imu.getMovementData()
        val filteredLocation = mergeLocationDataWithMovementData(location, movement)
        presenter.onLocationUpdate(filteredLocation)
        presenter.onMovementUpdate(movement)
    }

    // IMU callback
    override fun onWorldAccelerationCalculated(accelerationData: AccelerationData) {
        presenter.onAccelerometerUpdate(accelerationData)
    }

    private fun mergeLocationDataWithMovementData(location: LocationData, movement: MovementData): LocationData {
        var filteredXPos = previousLocation.xPos
        var filteredYPos = previousLocation.yPos
        var filteredZPos = previousLocation.zPos
        // X evaluation
        if ((movement.xAxis == Movement.POSITIVE && location.xPos > previousLocation.xPos) || (movement.xAxis == Movement.NEGATIVE && location.xPos < previousLocation.xPos)){
            filteredXPos = location.xPos
            previousLocation.xPos = filteredXPos
        }
        // Y evaluation
        if ((movement.yAxis == Movement.POSITIVE && location.yPos > previousLocation.yPos) || (movement.yAxis == Movement.NEGATIVE && location.yPos < previousLocation.yPos)){
            filteredYPos = location.yPos
            previousLocation.yPos = filteredYPos
        }
        // Z evaluation
        if ((movement.zAxis == Movement.POSITIVE && location.zPos > previousLocation.zPos) || (movement.zAxis == Movement.NEGATIVE && location.zPos < previousLocation.zPos)){
            filteredZPos = location.zPos
            previousLocation.zPos = filteredZPos
        }
        return LocationData(
            filteredXPos,
            filteredYPos,
            filteredZPos,
            location.qualityFactor
        )
    }
}