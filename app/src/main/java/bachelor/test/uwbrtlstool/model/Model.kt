package bachelor.test.uwbrtlstool.model

interface Model: Observable, BluetoothCallbacks {
    fun initializeBluetoothConnection()
    fun terminateBluetoothConnection(): Boolean?
    fun startDataTransfer()
    fun stopDataTransfer()
}