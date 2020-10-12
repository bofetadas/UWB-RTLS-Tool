package bachelor.test.locationapp.model

import android.content.Context
import bachelor.test.locationapp.presenter.Observer
import java.util.*

class ModelImpl(val context: Context): Model {

    private val observerList = ArrayList<Observer>()
    private var bluetoothService : BluetoothService? = null

    override fun initializeBluetoothConnection() {
        bluetoothService = BluetoothService(this)
        bluetoothService?.initialize()
    }

    override fun terminateBluetoothConnection(): Boolean? {
        return bluetoothService?.terminate()
    }

    override fun requestLocation() {
        bluetoothService?.requestLocationData()
    }

    override fun subscribeToUWBLocationUpdates() {
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

    override fun onCharacteristicRead(bytes: ByteArray) {
        observerList.forEach {observer ->
            observer.onBluetoothCharacteristicRead(this, bytes)
        }
    }

    override fun onCharacteristicChange(bytes: ByteArray) {
        observerList.forEach {observer ->
            observer.onBluetoothCharacteristicChange(this, bytes)
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