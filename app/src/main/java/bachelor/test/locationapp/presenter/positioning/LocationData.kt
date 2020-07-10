package bachelor.test.locationapp.presenter.positioning

data class LocationData(var xPos: Float, var yPos: Float, var zPos: Float, val qualityFactor: Int) {

    override fun toString(): String {
        return "$xPos, $yPos, $zPos, $qualityFactor"
    }
}