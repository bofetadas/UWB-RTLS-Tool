package bachelor.test.uwbrtlstool.view

import bachelor.test.uwbrtlstool.presenter.BasePresenter
import bachelor.test.uwbrtlstool.presenter.positioning.AccelerationData
import bachelor.test.uwbrtlstool.presenter.positioning.LocationData
import bachelor.test.uwbrtlstool.presenter.positioning.OrientationData
import bachelor.test.uwbrtlstool.presenter.recording.Directions
import bachelor.test.uwbrtlstool.presenter.recording.InputData

interface MainScreenContract {
    interface View : BaseView<Presenter> {
        fun showUWBPosition(uwbLocationData: LocationData)
        fun showFilteredPosition(filteredLocationData: LocationData)
        fun showAcceleration(accData: AccelerationData)
        fun showOrientation(orientationData: OrientationData)
        fun showCompassDirection(direction: String)
        fun enableConnectButton(enabled: Boolean)
        fun swapStartButton(start: Boolean)
        fun showMessage(message: String?)
        fun showRecordingOptionsDialog()
        fun showRecordingDetailsDialog()
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
        fun onRecordingDataTransferStart(inputData: InputData?)
        fun onRecordStopClicked()
        fun onTimerDone()
        // Contains raw and filtered positions as well as raw and filtered accelerations for recording purposes.
        // In regular, non-recording mode, only 'filteredLocationData' is used.
        fun onNewStateVectorEstimate(uwbLocationData: LocationData, filteredLocationData: LocationData, rawAccelerationData: AccelerationData, filteredAccelerationData: AccelerationData)
        fun onAccelerometerUpdate(accelerationData: AccelerationData)
        fun onOrientationUpdate(orientationData: OrientationData)
        fun onCompassDirectionUpdate(direction: Directions?)
    }
}