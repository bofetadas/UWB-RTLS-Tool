package bachelor.test.locationapp.view

import bachelor.test.locationapp.presenter.BasePresenter
import bachelor.test.locationapp.presenter.positioning.AccelerationData
import bachelor.test.locationapp.presenter.positioning.LocationData
import bachelor.test.locationapp.presenter.positioning.MovementData
import bachelor.test.locationapp.presenter.recording.InputData

interface MainScreenContract {
    interface View : BaseView<Presenter> {
        fun showPosition(locationData: LocationData)
        fun showAcceleration(accData: AccelerationData)
        fun showMovement(movementData: MovementData)
        fun enableConnectButton(enabled: Boolean)
        fun swapStartButton(start: Boolean)
        fun showMessage(message: String?)
        fun showRecordingDialog()
        fun showRecordStopScreen()
        fun dismissRecordStopScreen()
    }

    interface Presenter: BasePresenter{
        fun start()
        fun stop()
        fun onConnectClicked()
        fun onDisconnectClicked()
        fun onStartClicked()
        fun onStopClicked()
        fun onRegularDataTransferStart()
        fun onRecordingDataTransferStart(inputData: InputData)
        fun onRecordStopClicked()
        fun onTimerDone()
        fun onLocationUpdate(locationData: LocationData)
        fun onAccelerometerUpdate(accData: AccelerationData)
        fun onMovementUpdate(movementData: MovementData)
    }
}