package bachelor.test.uwbrtlstool.presenter

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import bachelor.test.uwbrtlstool.model.Model
import bachelor.test.uwbrtlstool.model.ModelImpl
import bachelor.test.uwbrtlstool.model.Observable
import bachelor.test.uwbrtlstool.presenter.positioning.*
import bachelor.test.uwbrtlstool.presenter.recording.Directions
import bachelor.test.uwbrtlstool.presenter.recording.InputData
import bachelor.test.uwbrtlstool.presenter.recording.Recording
import bachelor.test.uwbrtlstool.presenter.recording.RecordingImpl
import bachelor.test.uwbrtlstool.view.MainScreenContract

class PresenterImpl(private val context: Context, private val view: MainScreenContract.View): MainScreenContract.Presenter, Observer {

    private var model: Model? = null
    private var broadcastReceiver: BroadcastReceiver = BluetoothBroadcastReceiver(this)
    private val positioningImpl: Positioning = PositioningImpl(context, this)
    private val recordingImpl: Recording = RecordingImpl(context, this)
    private var recording = false

    // Android Lifecycle event functions
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

    // UI click event functions
    override fun onConnectClicked() {
        model?.initializeBluetoothConnection()
    }

    override fun onDisconnectClicked() {
        if (model?.terminateBluetoothConnection()!!){
            view.enableConnectButton(true)
        }
    }

    override fun onStartClicked() {
        view.showRecordingOptionsDialog()
    }

    override fun onStopClicked() {
        recording = false
        model?.stopDataTransfer()
        positioningImpl.stopIMU()
        positioningImpl.resetKalmanFilter()
        view.swapStartButton(true)
    }

    override fun onRegularDataTransferStart() {
        model?.startDataTransfer()
        positioningImpl.startIMU()
        view.swapStartButton(false)
    }

    override fun onRecordingDataTransferStart(inputData: InputData?) {
        val success = if (inputData == null){
            recordingImpl.createRecordingMovementFile()
        } else {
            recordingImpl.createRecordingFixedPositionFile(inputData.xInput, inputData.yInput, inputData.zInput, inputData.direction)
        }
        if (success) {
            recording = true
            model?.startDataTransfer()
            recordingImpl.startTimer(inputData?.timePeriod)
            recordingImpl.vibrateOnRecordStart()
            positioningImpl.startIMU()
            view.showMessage("Data recording successfully initialized")
            view.showRecordStopScreen()
        } else {
            view.showMessage("File already exists. Please look into data directory to resolve the issue")
        }
    }

    override fun onRecordStopClicked() {
        recordingImpl.stopTimer()
    }

    override fun onTimerDone() {
        view.dismissRecordStopScreen()
        onStopClicked()
    }

    // Positioning callback functions
    override fun onNewStateVectorEstimate(uwbLocationData: LocationData, filteredLocationData: LocationData, rawAccelerationData: AccelerationData, filteredAccelerationData: AccelerationData) {
        view.showUWBPosition(uwbLocationData)
        view.showFilteredPosition(filteredLocationData)
        if (recording){
            recordingImpl.writeToFile("$uwbLocationData | $filteredLocationData | $rawAccelerationData | $filteredAccelerationData")
        }
    }

    override fun onAccelerometerUpdate(accelerationData: AccelerationData) {
        view.showAcceleration(accelerationData)
    }

    override fun onOrientationUpdate(orientationData: OrientationData) {
        view.showOrientation(orientationData)
    }

    override fun onCompassDirectionUpdate(direction: Directions?) {
        if (direction == null){
            view.showCompassDirection("")
        }
        else {
            view.showCompassDirection(direction.toString())
        }
    }

    // Bluetooth callback functions
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
        positioningImpl.stopIMU()
        recording = false
    }

    override fun onBluetoothCharacteristicChange(observable: Observable, args: Any) {
        try{
            args as ByteArray
            positioningImpl.calculateLocation(args)
        } catch(e: TypeCastException){
            throw e
        }
    }
}