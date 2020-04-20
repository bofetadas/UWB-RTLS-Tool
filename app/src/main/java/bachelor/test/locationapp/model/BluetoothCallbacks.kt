package bachelor.test.locationapp.model

interface BluetoothCallbacks {
    fun onBluetoothNotEnabled()
    fun onConnectionSuccess(success: Boolean)
    fun onDisconnectionSuccess(success: Boolean)
    fun onCharacteristicChange(args: Any)
}