package bachelor.test.locationapp.presenter

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import bachelor.test.locationapp.model.Model
import bachelor.test.locationapp.model.ModelImpl
import bachelor.test.locationapp.model.Observable
import bachelor.test.locationapp.view.MainScreenContract

private const val POSITIONS_ARRAY_SIZE = 14
private const val DISTANCES_ARRAY_SIZE = 29

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

    override fun onBluetoothCharacteristicChange(observable: Observable, args: Any) {
        try{
            args as ByteArray
            // Because we set the location mode to 0 in Bluetooth Service, here we can expect an array of 14 Bytes
            // The official decawave doc says that only 13 Bytes should be returned
            // I don't know where that extra first Byte is coming from though.
            if (args.size == POSITIONS_ARRAY_SIZE) {
                val location = getLocationFromByteArray(args)
                view.showPosition(location)
            }
            // Assuming we receive distance data from 4 anchors. When only 3 anchors are available, the code will fail to execute and nothing will be computed.
            else if(args.size == DISTANCES_ARRAY_SIZE){
                val distances = getDistancesFromByteArray(args)
                view.showDistances(distances)
            }
        } catch (e: TypeCastException){
            println(e)
        }
    }

    private fun getLocationFromByteArray(args: ByteArray): LocationData {
        // Since received byte arrays are encoded in little endian, reverse the order for each position
        val xByteArray = byteArrayOf(args[4], args[3], args[2], args[1])
        val xPosition = xByteArray.transformIntoSignedInteger().toDouble() / 1000

        val yByteArray = byteArrayOf(args[8], args[7], args[6], args[5])
        val yPosition = yByteArray.transformIntoSignedInteger().toDouble() / 1000

        val zByteArray = byteArrayOf(args[12], args[11], args[10], args[9])
        val zPosition = zByteArray.transformIntoSignedInteger().toDouble() / 1000

        val qualityFactor = args[13].toInt()
        return LocationData(xPosition, yPosition, zPosition, qualityFactor)
    }

    private fun getDistancesFromByteArray(args: ByteArray): DistanceData {
        val locationDataMode = args[0]
        val anchorCount = args[1]

        val firstID = String.format("%02X", args[3]) + String.format("%02X", args[2])
        val firstDistance = byteArrayOf(args[7], args[6], args[5], args[4]).transformIntoSignedInteger().toFloat() / 1000
        val firstDistanceObject = DistanceObject(firstID, firstDistance)

        val secondID = String.format("%02X", args[10]) + String.format("%02X", args[9])
        val secondDistance = byteArrayOf(args[14], args[13], args[12], args[11]).transformIntoSignedInteger().toFloat() / 1000
        val secondDistanceObject = DistanceObject(secondID, secondDistance)

        val thirdID = String.format("%02X", args[17]) + String.format("%02X", args[16])
        val thirdDistance = byteArrayOf(args[21], args[20], args[19], args[18]).transformIntoSignedInteger().toFloat() / 1000
        val thirdDistanceObject = DistanceObject(thirdID, thirdDistance)

        val fourthID = String.format("%02X", args[24]) + String.format("%02X", args[23])
        val fourthDistance = byteArrayOf(args[28], args[27], args[26], args[25]).transformIntoSignedInteger().toFloat() / 1000
        val fourthDistanceObject = DistanceObject(fourthID, fourthDistance)

        return DistanceData(firstDistanceObject, secondDistanceObject, thirdDistanceObject, fourthDistanceObject)
    }

    private fun ByteArray.transformIntoSignedInteger() =
        ((this[0].toInt() and 0xFF) shl 24) or
                ((this[1].toInt() and 0xFF) shl 16) or
                ((this[2].toInt() and 0xFF) shl 8) or
                (this[3].toInt() and 0xFF)
}