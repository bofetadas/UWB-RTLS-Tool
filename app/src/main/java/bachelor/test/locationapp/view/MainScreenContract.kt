package bachelor.test.locationapp.view

import bachelor.test.locationapp.presenter.BasePresenter
import bachelor.test.locationapp.presenter.positioning.AccelerationData
import bachelor.test.locationapp.presenter.positioning.LocationData
import bachelor.test.locationapp.presenter.positioning.OrientationData
import bachelor.test.locationapp.presenter.recording.InputData

interface MainScreenContract {
    interface View : BaseView<Presenter> {
        fun showUWBPosition(uwbLocationData: LocationData)
        fun showFilteredPosition(filteredLocationData: LocationData)
        fun showAcceleration(accData: AccelerationData)
        fun showOrientation(orientationData: OrientationData)
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
        // Contains raw UWB Positions as well as Filtered Positions for recording purposes.
        // In regular, non-recording mode, uwb data won't be needed
        fun onLocationUpdate(uwbLocationData: LocationData, filteredLocationData: LocationData)
        fun onAccelerometerUpdate(accelerationData: AccelerationData)
        fun onOrientationUpdate(orientationData: OrientationData)
    }
}