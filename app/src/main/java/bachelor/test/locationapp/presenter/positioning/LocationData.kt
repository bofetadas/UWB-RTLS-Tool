package bachelor.test.locationapp.presenter.positioning

data class LocationData(val xPos: Double = 1.0, val yPos: Double = 2.7, val zPos: Double = 1.0, val qualityFactor: Int = -1) {

    override fun toString(): String {
        return "$xPos, $yPos, $zPos, $qualityFactor"
    }
}