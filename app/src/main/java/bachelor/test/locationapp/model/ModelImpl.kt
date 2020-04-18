package bachelor.test.locationapp.model

import android.content.Context

class ModelImpl(private val context: Context): Model {

    private lateinit var bluetoothService : BluetoothService
    override fun initializeBluetooth() {
        bluetoothService = BluetoothService(context)
        bluetoothService.initializeBluetooth()

    }

    override fun getLocation(): ByteArray {
        return bluetoothService.getLocationCharacteristic()
    }


}