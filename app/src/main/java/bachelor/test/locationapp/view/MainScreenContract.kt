package bachelor.test.locationapp.view

import bachelor.test.locationapp.positioning.AccelerometerData
import bachelor.test.locationapp.positioning.DistanceData
import bachelor.test.locationapp.positioning.LocationData
import bachelor.test.locationapp.presenter.BasePresenter

interface MainScreenContract {
    interface View : BaseView<Presenter> {
        fun showPosition(locationData: LocationData)
        fun showDistances(distances: DistanceData)
        fun showAccelerometerData(accData: AccelerometerData)
        fun enableConnectButton(enabled: Boolean)
        fun swapStartButton(start: Boolean)
        fun showMessage(message: String?)
    }

    interface Presenter: BasePresenter{
        fun start()
        fun stop()
        fun onStartClicked()
        fun onStopClicked()
        fun onConnectClicked()
        fun onAccelerationUpdate(accData: AccelerometerData)
    }
}