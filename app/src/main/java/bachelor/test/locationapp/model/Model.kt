package bachelor.test.locationapp.model

interface Model: Observable, BluetoothCallbacks {
    fun initializeBluetoothConnection()
    fun terminateBluetoothConnection(): Boolean?
    fun requestLocation()
    fun subscribeToUWBLocationUpdates()
    fun stopDataTransfer()
}