package bachelor.test.locationapp.presenter.positioning

data class LocationData(var xPos: Double, var yPos: Double, var zPos: Double, val qualityFactor: Int) {

    override fun toString(): String {
        return "$xPos, $yPos, $zPos, $qualityFactor"
    }
}