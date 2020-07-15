package bachelor.test.locationapp.presenter

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import bachelor.test.locationapp.model.Model
import bachelor.test.locationapp.model.ModelImpl
import bachelor.test.locationapp.model.Observable
import bachelor.test.locationapp.presenter.positioning.*
import bachelor.test.locationapp.presenter.recording.InputData
import bachelor.test.locationapp.presenter.recording.Recording
import bachelor.test.locationapp.presenter.recording.RecordingImpl
import bachelor.test.locationapp.presenter.recording.RecordingModes
import bachelor.test.locationapp.view.MainScreenContract

class PresenterImpl(private val context: Context, private val view: MainScreenContract.View): MainScreenContract.Presenter, Observer {

    private var model: Model? = null
    private var broadcastReceiver: BroadcastReceiver = BluetoothBroadcastReceiver(this)
    private val positioningImpl: Positioning = PositioningImpl(context, this)
    private val recordingImpl: Recording = RecordingImpl(context, this)
    private var recording = false
    private var recordingMode = RecordingModes.P

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
        view.showRecordingDialog()
    }

    override fun onStopClicked() {
        model?.stopDataTransfer()
        positioningImpl.stopIMU()
        view.swapStartButton(true)
        recording = false
    }

    override fun onUserWantsToRecordData() {
        view.showRecordingOptionsDialog()
    }

    override fun onRegularDataTransferStart() {
        model?.startDataTransfer()
        positioningImpl.startIMU()
        view.swapStartButton(false)
        view.showLocationViews()
    }

    override fun onRecordingDataTransferStart(inputData: InputData) {
        val success = recordingImpl.createFile(inputData.mode, inputData.xInput, inputData.yInput, inputData.zInput, inputData.direction)
        if (success) {
            model?.startDataTransfer()
            recording = true
            recordingImpl.startTimer(inputData.timePeriod)
            recordingImpl.vibrateOnRecordStart()
            recordingMode = inputData.mode
            if (recordingMode == RecordingModes.P) {
                view.showLocationViews()
            }
            else if (recordingMode == RecordingModes.D){
                view.showDisplacementViews()
            }
            positioningImpl.startIMU()
            view.showRecordStopScreen()
            view.showMessage("Data recording successfully initialized")
        } else {
            view.showMessage("File already exists. Please look into data directory to resolve the issue")
        }
    }

    override fun onRecordStopClicked() {
        recordingImpl.stopTimer()
    }

    override fun onTimerDone() {
        recording = false
        model?.stopDataTransfer()
        positioningImpl.stopIMU()
        view.dismissRecordStopScreen()
    }

    // Positioning callback functions
    override fun onLocationUpdate(locationData: LocationData) {
        view.showPosition(locationData)
        if (recording){
            recordingImpl.writeToFile(locationData.toString())
        }
    }

    override fun onAccelerometerUpdate(accData: AccelerationData) {
        view.showAcceleration(accData)
    }

    override fun onMovementUpdate(movementData: MovementData) {
        view.showMovement(movementData)
    }

    override fun onDisplacementUpdate(displacementData: UWBIMUDisplacementData) {
        view.showDisplacement(displacementData)
        if (recording){
            recordingImpl.writeToFile(displacementData.toString())
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
        try {
            args as ByteArray
            positioningImpl.getLocalizationData(recordingMode, args)
        } catch(e: TypeCastException){
            throw e
        }
    }
}