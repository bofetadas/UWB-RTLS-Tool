package bachelor.test.locationapp.model

import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.os.Handler
import java.util.*

private const val TAG_MAC = "F0:74:2F:98:DE:90"
private const val LOCATION_CHARACTERISTIC = "003BBDF2-C634-4B3D-AB56-7EC889B89A37"
private const val DESCRIPTOR = "00002902-0000-1000-8000-00805f9b34fb"

private const val SCAN_PERIOD = 10000L

private const val BLUETOOTH_NOT_ENABLED = "Bluetooth not enabled. Please enable Bluetooth and restart the app."

class BluetoothService(private val context: Context) {

    private lateinit var location: ByteArray
    private val handler = Handler()

    private val bluetoothAdapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private val BluetoothAdapter.isDisabled: Boolean
        get() = !isEnabled

    fun getLocationCharacteristic(): ByteArray{
        return location
    }

    fun initializeBluetooth(){
        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        bluetoothAdapter?.takeIf { it.isDisabled }?.apply {
            println(BLUETOOTH_NOT_ENABLED)
        }
        println("SCANNING FOR BLUETOOTH DEVICES")
        scanLeDevice()
    }

    private fun scanLeDevice() {
        val scanner = bluetoothAdapter?.bluetoothLeScanner

        handler.postDelayed({
            scanner?.stopScan(object: ScanCallback(){
                override fun onScanResult(callbackType: Int, result: ScanResult?) {
                    println("SCAN STOPPED")
                }
            })
        }, SCAN_PERIOD)

        println("STARTING SCAN")
        scanner?.startScan(object: ScanCallback(){
            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                super.onScanResult(callbackType, result)
                val device = result?.device
                if (device?.address == TAG_MAC){
                    println("FOUND TAG")
                    println("CONNECTING TO TAG...")
                    device.connectGatt(context, false, gattCallback)
                }
            }

            override fun onScanFailed(errorCode: Int) {
                println("SCAN FAILED")
                println(errorCode.toString())
            }
        })
    }

    // Various callback methods defined by the BLE API.
    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    println("CONNECTED TO TAG")
                    println("DISCOVERING SERVICES...")
                    gatt.discoverServices()
                }
            }
        }

        // New services discovered
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    println("SERVICES DISCOVERED")
                    println("ENABLING NOTIFICATIONS...")
                    val characteristic = gatt.services[2].getCharacteristic(UUID.fromString(LOCATION_CHARACTERISTIC))
                    gatt.setCharacteristicNotification(characteristic, true)
                    val descriptor = characteristic.getDescriptor(UUID.fromString(DESCRIPTOR))
                    descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    gatt.writeDescriptor(descriptor)
                    println("NOTIFICATIONS ENABLED")
                }
                else -> println("NO SERVICES DISCOVERED")
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?
        ) {
            location = characteristic!!.value
        }
    }
}