package bachelor.test.locationapp.presenter

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import bachelor.test.locationapp.model.Model
import bachelor.test.locationapp.model.ModelImpl
import bachelor.test.locationapp.model.Observable
import bachelor.test.locationapp.presenter.positioning.Positioning
import bachelor.test.locationapp.presenter.positioning.PositioningImpl
import bachelor.test.locationapp.view.MainScreenContract

class PresenterImpl(private val context: Context, private val view: MainScreenContract.View): MainScreenContract.Presenter, Observer {

    private var model: Model? = null
    private var broadcastReceiver: BroadcastReceiver = BluetoothBroadcastReceiver(this)
    private val positioningImpl: Positioning = PositioningImpl(context, this)

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
        positioningImpl.startIMU()
        model?.startDataTransfer()
        view.swapStartButton(false)
        view.changeBackground("static")
    }

    override fun onStopClicked() {
        model?.stopDataTransfer()
        positioningImpl.stopIMU()
        positioningImpl.resetKalmanFilter()
        view.swapStartButton(true)
        view.changeBackground("reset")
    }

    override fun onMovementDetected(movement: Boolean) {
        if (movement){
            view.changeBackground("dynamic")
        }
        else {
            view.changeBackground("static")
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
        view.changeBackground("reset")
        view.showMessage("Tag disconnected")
        positioningImpl.stopIMU()
    }

    override fun onBluetoothCharacteristicChange(observable: Observable, args: Any) {
        try {
            args as ByteArray
            positioningImpl.calculateLocation(args)
        } catch (e: TypeCastException) {
            throw e
        }
    }
}