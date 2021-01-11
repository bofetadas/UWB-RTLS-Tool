package maxbauer.uwbrtls.tool.presenter

import maxbauer.uwbrtls.tool.model.Observable

interface Observer {
    fun onBluetoothNotEnabled(observable: Observable)
    fun onBluetoothEnabled()
    fun onBluetoothConnectionSuccess(observable: Observable, success: Boolean)
    fun onBluetoothDisconnectionSuccess(observable: Observable, success: Boolean)
    fun onBluetoothCharacteristicChange(observable: Observable, args: Any)
}