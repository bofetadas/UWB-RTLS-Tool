package bachelor.test.locationapp.presenter.positioning

data class LocationData(val xPos: Float = 1f, val yPos: Float = 2.7f, val zPos: Float = 1f, val qualityFactor: Int = -1) {

    override fun toString(): String {
        return "$xPos, $yPos, $zPos, $qualityFactor"
    }
}