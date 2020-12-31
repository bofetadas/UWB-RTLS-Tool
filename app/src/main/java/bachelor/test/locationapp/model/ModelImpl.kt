package bachelor.test.locationapp.model

import android.content.Context
import bachelor.test.locationapp.presenter.Observer
import java.util.*

class ModelImpl(val context: Context): Model {

    private val observerList = ArrayList<Observer>()
    private var bluetoothService: BluetoothService? = null

    override fun initializeBluetoothConnection() {
        bluetoothService = BluetoothService(this)
        bluetoothService?.initialize()
    }

    override fun terminateBluetoothConnection(): Boolean? {
        return bluetoothService?.terminate()
    }

    override fun startDataTransfer() {
        bluetoothService?.enableCharacteristicNotifications(GET_LOCATION_CHARACTERISTIC)
    }

    override fun stopDataTransfer() {
        bluetoothService?.disableLocationDataNotifications(GET_LOCATION_CHARACTERISTIC)
    }

    override fun onConnectionSuccess(success: Boolean) {
        observerList.forEach { observer ->
            observer.onBluetoothConnectionSuccess(this, success)
        }
    }

    override fun onDisconnectionSuccess(success: Boolean) {
        observerList.forEach { observer ->
            observer.onBluetoothDisconnectionSuccess(this, success)
        }
    }

    override fun onCharacteristicChange(args: Any) {
        observerList.forEach {observer ->
            observer.onBluetoothCharacteristicChange(this, args)
        }
    }

    override fun addObserver(observer: Observer) {
        observerList.add(observer)
    }

    override fun deleteObserver(observer: Observer) {
        observerList.remove(observer)
    }

    override fun onBluetoothNotEnabled() {
        observerList.forEach { observer ->
            observer.onBluetoothNotEnabled(this)
        }
    }
}