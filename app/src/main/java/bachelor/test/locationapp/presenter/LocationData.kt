package bachelor.test.locationapp.presenter

data class LocationData(val xPos: Double, val yPos: Double, val zPos: Double, val qualityFactor: Int) {

    override fun toString(): String {
        return "$xPos, $yPos, $zPos, $qualityFactor"
    }
}