package bachelor.test.locationapp.view

import bachelor.test.locationapp.presenter.BasePresenter
import bachelor.test.locationapp.positioning.DistanceData
import bachelor.test.locationapp.positioning.LocationData

interface MainScreenContract {
    interface View : BaseView<Presenter> {
        fun showPosition(locationData: LocationData)
        fun showDistances(distances: DistanceData)
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
    }
}