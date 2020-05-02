package bachelor.test.locationapp.presenter

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import bachelor.test.locationapp.model.Model
import bachelor.test.locationapp.model.ModelImpl
import bachelor.test.locationapp.model.Observable
import bachelor.test.locationapp.view.MainScreenContract

private const val POSITION_LOCATION_BYTE_ARRAY_SIZE = 14

class PresenterImpl(private val context: Context, private val view: MainScreenContract.View):
    MainScreenContract.Presenter,
    Observer
{
    private var model: Model? = null
    private var broadcastReceiver: BroadcastReceiver = BluetoothBroadcastReceiver(this)

    override fun start() {
        model = ModelImpl(context)
        model?.addObserver(this)
        context.registerReceiver(broadcastReceiver, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED))
    }

    override fun stop() {
        if (model?.terminateBluetoothConnection() != null){
            if (model?.terminateBluetoothConnection()!!) {
                model?.deleteObserver(this)
            }
        }
    }

    override fun onConnectClicked() {
        model?.initializeBluetoothConnection()
    }

    override fun onStartClicked() {
        model?.startDataTransfer()
        view.swapStartButton(false)
    }

    override fun onStopClicked() {
        model?.stopDataTransfer()
        view.swapStartButton(true)
    }

    override fun onBluetoothEnabled() {
        view.showMessage("Bluetooth successfully turned on")
        model?.initializeBluetoothConnection()
    }

    override fun onBluetoothNotEnabled(observable: Observable) {
        view.showMessage("Bluetooth not enabled. Enabling now")
    }


    override fun onBluetoothConnectionSuccess(observable: Observable, success: Boolean) {
        if (success) {
            view.enableConnectButton(false)
            view.swapStartButton(true)
            view.showMessage("Tag connected")
        }
        else {
            view.enableConnectButton(true)
            view.showMessage("Connection to tag failed")
        }
    }

    override fun onBluetoothDisconnectionSuccess(observable: Observable, success: Boolean) {
        view.enableConnectButton(true)
        view.showMessage("Tag disconnected")
    }

    @ExperimentalUnsignedTypes
    override fun onBluetoothCharacteristicChange(observable: Observable, args: Any) {
        try{
            args as ByteArray
            // Because we set the location mode to 0 in Bluetooth Service, here we can expect an array of 14 Bytes
            // The official decawave doc says that only 13 Bytes should be returned
            // I don't know where that extra first Byte is coming from though.
            if (args.size == POSITION_LOCATION_BYTE_ARRAY_SIZE) {
                val location = getLocationFromByteArray(args)
                view.showPosition(location)
            }
        } catch (e: TypeCastException){
            println(e)
        }
    }

    @ExperimentalUnsignedTypes
    private fun getLocationFromByteArray(locationByteArray: ByteArray): LocationData {
        // Since received byte arrays are encoded in little endian, reverse the order for each position
        val xByteArray = byteArrayOf(locationByteArray[4], locationByteArray[3], locationByteArray[2], locationByteArray[1])
        val xPosition = xByteArray.getUIntAt(0).toDouble() / 1000

        val yByteArray = byteArrayOf(locationByteArray[8], locationByteArray[7], locationByteArray[6], locationByteArray[5])
        val yPosition = yByteArray.getUIntAt(0).toDouble() / 1000

        val zByteArray = byteArrayOf(locationByteArray[12], locationByteArray[11], locationByteArray[10], locationByteArray[9])
        val zPosition = zByteArray.getUIntAt(0).toDouble() / 1000

        val qualityFactor = locationByteArray[13].toUByte().toInt()
        return LocationData(xPosition, yPosition, zPosition, qualityFactor)
    }

    @ExperimentalUnsignedTypes
    private fun ByteArray.getUIntAt(idx: Int) =
        ((this[idx].toInt() and 0xFF) shl 24) or
                ((this[idx + 1].toInt() and 0xFF) shl 16) or
                ((this[idx + 2].toInt() and 0xFF) shl 8) or
                (this[idx + 3].toInt() and 0xFF)
}