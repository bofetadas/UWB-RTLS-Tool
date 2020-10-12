package bachelor.test.locationapp.presenter

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import bachelor.test.locationapp.model.Model
import bachelor.test.locationapp.model.ModelImpl
import bachelor.test.locationapp.model.Observable
import bachelor.test.locationapp.presenter.positioning.*
import bachelor.test.locationapp.presenter.recording.Directions
import bachelor.test.locationapp.presenter.recording.InputData
import bachelor.test.locationapp.presenter.recording.Recording
import bachelor.test.locationapp.presenter.recording.RecordingImpl
import bachelor.test.locationapp.utils.ByteArrayUtil
import bachelor.test.locationapp.view.MainScreenContract

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
        positioningImpl.stop()
        view.swapStartButton(true)
    }

    override fun onRegularDataTransferStart() {
        // Request the current position to initialize the kalman filter with.
        // Callback is handled in 'onBluetoothCharacteristicRead'.
        model?.requestLocation()
    }

    override fun onMovementRecordingDataTransferStart() {
        val success = recordingImpl.createRecordingMovementFile()
        if (success) {
            recording = true
            // Request the current position to initialize the kalman filter with.
            // Callback is handled in 'onBluetoothCharacteristicRead'.
            model?.requestLocation()
        }
        else {
            view.showMessage("File already exists. Please look into data directory to resolve the issue")
        }
    }

    override fun onFixedPositionRecordingDataTransferStart(inputData: InputData) {
        val success = recordingImpl.createRecordingFixedPositionFile(inputData.x, inputData.y, inputData.z, inputData.direction)
        if (success){
            recording = true
            recordingImpl.startTimer(inputData.timePeriod)
            recordingImpl.vibrateOnRecordStart()
            positioningImpl.initialize(LocationData(inputData.x.toDouble(), inputData.y.toDouble(), inputData.z.toDouble()))
            model?.subscribeToUWBLocationUpdates()
            view.showMessage("Data recording successfully initialized")
            view.showRecordStopScreen()
        }
        else {
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
        positioningImpl.stop()
        recording = false
    }

    override fun onBluetoothCharacteristicChange(observable: Observable, bytes: ByteArray) {
        positioningImpl.calculateLocation(bytes)
    }

    override fun onBluetoothCharacteristicRead(observable: Observable, bytes: ByteArray) {
        if (bytes.size == POSITION_BYTE_ARRAY_SIZE) {
            if (recording){
                recordingImpl.vibrateOnRecordStart()
                view.showMessage("Data recording successfully initialized")
                view.showRecordStopScreen()
            }
            else {
                view.swapStartButton(false)
            }
            val location = ByteArrayUtil.getUWBLocationFromByteArray(bytes)
            positioningImpl.initialize(location)
            model?.subscribeToUWBLocationUpdates()
        }
        else {
            model?.requestLocation()
            view.showMessage("Too small packet size. Are all sensors running?")
        }
    }
}