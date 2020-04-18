package bachelor.test.locationapp.model

interface Model {
    fun initializeBluetooth()
    fun getLocation(): ByteArray
}