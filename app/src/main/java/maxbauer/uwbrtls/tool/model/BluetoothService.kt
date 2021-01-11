package maxbauer.uwbrtls.tool.model

import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import java.util.*

const val GET_LOCATION_CHARACTERISTIC = "003BBDF2-C634-4B3D-AB56-7EC889B89A37"
private const val GET_PROXY_POSITIONS_CHARACTERISTIC = "F4A67D7D-379D-4183-9C03-4B6EA5103291"
private const val SET_LOCATION_MODE_CHARACTERISTIC = "A02B947E-DF97-4516-996A-1882521E0EAD"
private const val DESCRIPTOR = "00002902-0000-1000-8000-00805F9B34FB"
private const val TAG_MAC = "F0:74:2F:98:DE:90"
private val POSITION_MODE = byteArrayOf(0x00)

class BluetoothService(private val model: ModelImpl) {

    private var tagConnection: BluetoothGatt? = null
    private var tagIsConnected = true

    private val bluetoothAdapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager = model.context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private val BluetoothAdapter.isDisabled: Boolean
        get() = !isEnabled

    fun initialize(){
        // Ensures Bluetooth is available on the device and it is enabled.
        bluetoothAdapter?.takeIf { it.isDisabled }?.apply {
            model.onBluetoothNotEnabled()
            bluetoothAdapter?.enable()
            return
        }
        scanLeDevice()
    }

    fun terminate(): Boolean{
        if (tagIsConnected){
            disableLocationDataNotifications(GET_PROXY_POSITIONS_CHARACTERISTIC)
            tagConnection?.disconnect()
            tagConnection?.close()
            tagConnection = null
            tagIsConnected = false
            return true
        }
        return false
    }

    private fun scanLeDevice() {
        val scanner = bluetoothAdapter?.bluetoothLeScanner
        scanner?.startScan(object: ScanCallback(){

            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                super.onScanResult(callbackType, result)
                val device = result?.device
                if (device?.address == TAG_MAC){
                    scanner.stopScan(this)
                    device.connectGatt(model.context, false, gattCallback)
                }
            }

            override fun onScanFailed(errorCode: Int) {
                println(errorCode.toString())
            }
        })
    }

    // Various callback methods defined by the BLE API.
    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    tagConnection = gatt
                    tagIsConnected = true
                    tagConnection!!.discoverServices()
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    tagIsConnected = false
                    tagConnection = null
                    model.onDisconnectionSuccess(true)
                }
            }
        }

        // New services discovered
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    // Set location mode to 0 (Position only mode)
                    val setLocationModeCharacteristic = gatt.services[2].getCharacteristic(UUID.fromString(
                        SET_LOCATION_MODE_CHARACTERISTIC
                    ))
                    setLocationModeCharacteristic.value = POSITION_MODE
                    val success = gatt.writeCharacteristic(setLocationModeCharacteristic)
                    if (!success){
                        model.onConnectionSuccess(false)
                    }
                }
                else -> {
                    // Unsuccessful service discovery
                    model.onConnectionSuccess(false)
                }
            }
        }

        // Check if the position mode set in 'onServicesDiscovered' was successful
        // If yes subscribe to PROXY_POSITIONS in order to disable automatic disconnection from tag
        // If no inform UI about failed connection attempt
        override fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int){
            if (characteristic?.uuid == UUID.fromString(SET_LOCATION_MODE_CHARACTERISTIC) && status == BluetoothGatt.GATT_SUCCESS){
                if (characteristic?.value!!.contentEquals(POSITION_MODE)) {
                    // Subscribing to changes of PROXY_POSITIONS characteristic
                    // We're not interested in this characteristic but subscribing to it prevents us
                    // from automatically being disconnected from tag while inactive
                    enableCharacteristicNotifications(GET_PROXY_POSITIONS_CHARACTERISTIC)
                    model.onConnectionSuccess(true)
                } else {
                    model.onConnectionSuccess(false)
                }
            } else {
                model.onConnectionSuccess(false)
            }
        }

        // Remote characteristic changes handling
        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
            // Only forward incoming characteristic notification data if it is position data
            if (characteristic!! == tagConnection!!.services[2].getCharacteristic(UUID.fromString(
                    GET_LOCATION_CHARACTERISTIC
                ))){
                model.onCharacteristicChange(characteristic.value)
            }
        }
    }

    fun enableCharacteristicNotifications(characteristicString: String) {
        if (tagIsConnected){
            val characteristic = tagConnection!!.services[2].getCharacteristic(UUID.fromString(characteristicString))
            tagConnection?.setCharacteristicNotification(characteristic, true)
            val descriptor = characteristic.getDescriptor(UUID.fromString(DESCRIPTOR))
            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            tagConnection?.writeDescriptor(descriptor)
        }
        else{
            initialize()
        }
    }

    fun disableLocationDataNotifications(characteristicString: String){
        if (tagIsConnected){
            val characteristic = tagConnection!!.services[2].getCharacteristic(UUID.fromString(characteristicString))
            tagConnection?.setCharacteristicNotification(characteristic, false)
            val descriptor = characteristic.getDescriptor(UUID.fromString(DESCRIPTOR))
            descriptor.value = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
            tagConnection?.writeDescriptor(descriptor)
        }
    }
}