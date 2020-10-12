package bachelor.test.locationapp.presenter.positioning

interface Positioning {
    fun initialize(initLocationData: LocationData)
    fun stop()
    fun calculateLocation(byteArray: ByteArray)
}