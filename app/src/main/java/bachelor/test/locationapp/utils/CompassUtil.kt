package bachelor.test.locationapp.utils

import bachelor.test.locationapp.presenter.recording.Directions

// Yaw values for the 4 compass directions (determined empirically)
private const val COMPASS_DIRECTION_YAW_NORTH = 12
private const val COMPASS_DIRECTION_YAW_EAST = 97
private const val COMPASS_DIRECTION_YAW_SOUTH = -174
private const val COMPASS_DIRECTION_YAW_WEST = -85
private const val COMPASS_DIRECTION_YAW_THRESHOLD = 2   // in degrees

object CompassUtil {

    fun getCompassDirection(compassDirectionYaw: Double): Directions? {
        return when {
            compassDirectionYaw.isRoughly(COMPASS_DIRECTION_YAW_NORTH) -> Directions.N
            compassDirectionYaw.isRoughly(COMPASS_DIRECTION_YAW_EAST) -> Directions.E
            compassDirectionYaw.isRoughly(COMPASS_DIRECTION_YAW_SOUTH) -> Directions.S
            compassDirectionYaw.isRoughly(COMPASS_DIRECTION_YAW_WEST) -> Directions.W
            else -> null
        }
    }

    private fun Double.isRoughly(compassDirectionConstant: Int): Boolean {
        return  this > compassDirectionConstant - COMPASS_DIRECTION_YAW_THRESHOLD &&
                this < compassDirectionConstant + COMPASS_DIRECTION_YAW_THRESHOLD
    }
}