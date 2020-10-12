package bachelor.test.locationapp.model

interface BluetoothCallbacks {
    fun onBluetoothNotEnabled()
    fun onConnectionSuccess(success: Boolean)
    fun onDisconnectionSuccess(success: Boolean)
    fun onCharacteristicRead(bytes: ByteArray)
    fun onCharacteristicChange(bytes: ByteArray)
}