package bachelor.test.locationapp.presenter

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import bachelor.test.locationapp.model.FileWriter
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
    private val fileWriter = FileWriter(context)
    private var recording = false
    private val vibratorFeedback = VibratorFeedback(context)
    private val timer = Timer(this)
    private val accelerometerReader = AccelerometerReader(context, this)

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
        accelerometerReader.registerListener()
    }

    override fun onDisconnectClicked() {
        if (model?.terminateBluetoothConnection()!!){
            view.enableConnectButton(true)
        }
    }

    override fun onRecordStartClicked(inputData: InputData) {
        val success = fileWriter.createFile(inputData.xInput, inputData.yInput, inputData.zInput, inputData.direction)
        if (success) {
            recording = true
            model?.startDataTransfer()
            timer.startTimer(inputData.timePeriod)
            view.showMessage("Data recording successfully initialized")
            vibratorFeedback.vibrateOnRecordStart()
            view.showRecordStopScreen()
            accelerometerReader.registerListener()
        } else {
            view.showMessage("File already exists. Please look into data directory to resolve the issue")
        }
    }

    override fun onRecordStopClicked() {
        timer.stopTimer()
    }

    override fun onTimerDone() {
        model?.stopDataTransfer()
        vibratorFeedback.vibrateOnRecordStop()
        view.dismissRecordStopScreen()
        accelerometerReader.unregisterListener()
        recording = false
    }

    override fun onAccelerometerUpdate(accData: AccelerometerData) {
        view.showAccelerometerData(accData)
    }

    override fun onStartClicked() {
        view.showRecordingDialog()
    }

    override fun onRegularDataTransferStart() {
        model?.startDataTransfer()
        accelerometerReader.registerListener()
        view.swapStartButton(false)
    }

    override fun onStopClicked() {
        model?.stopDataTransfer()
        accelerometerReader.unregisterListener()
        view.swapStartButton(true)
        recording = false
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
        accelerometerReader.unregisterListener()
        recording = false
    }

    override fun onBluetoothCharacteristicChange(observable: Observable, args: Any) {
        try{
            args as ByteArray
            // Because we set the location mode to 0 in Bluetooth Service, here we can expect an array of 14 Bytes
            // The official decawave doc says that only 13 Bytes should be returned
            // I don't know where that extra first Byte is coming from though.
            if (args.size == POSITION_LOCATION_BYTE_ARRAY_SIZE) {
                val location = getLocationFromByteArray(args)
                view.showPosition(location)
                if (recording) {
                    fileWriter.writeToFile(location.toString())
                }
            }
        } catch (e: TypeCastException){
            println(e)
        }
    }

    private fun getLocationFromByteArray(locationByteArray: ByteArray): LocationData {
        // Since received byte arrays are encoded in little endian, reverse the order for each position
        val xByteArray = byteArrayOf(locationByteArray[4], locationByteArray[3], locationByteArray[2], locationByteArray[1])
        val xPosition = xByteArray.transformIntoSignedInteger().toDouble() / 1000

        val yByteArray = byteArrayOf(locationByteArray[8], locationByteArray[7], locationByteArray[6], locationByteArray[5])
        val yPosition = yByteArray.transformIntoSignedInteger().toDouble() / 1000

        val zByteArray = byteArrayOf(locationByteArray[12], locationByteArray[11], locationByteArray[10], locationByteArray[9])
        val zPosition = zByteArray.transformIntoSignedInteger().toDouble() / 1000

        val qualityFactor = locationByteArray[13].toInt()
        return LocationData(xPosition, yPosition, zPosition, qualityFactor)
    }

    private fun ByteArray.transformIntoSignedInteger() =
        ((this[0].toInt() and 0xFF) shl 24) or
                ((this[1].toInt() and 0xFF) shl 16) or
                ((this[2].toInt() and 0xFF) shl 8) or
                (this[3].toInt() and 0xFF)
}