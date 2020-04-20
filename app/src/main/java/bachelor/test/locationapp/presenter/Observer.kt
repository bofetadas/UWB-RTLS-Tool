package bachelor.test.locationapp.presenter

import bachelor.test.locationapp.model.Observable

interface Observer {
    fun onBluetoothNotEnabled(observable: Observable)
    fun onBluetoothEnabled()
    fun onBluetoothConnectionSuccess(observable: Observable, success: Boolean)
    fun onBluetoothDisconnectionSuccess(observable: Observable, success: Boolean)
    fun onBluetoothCharacteristicChange(observable: Observable, args: Any)
}