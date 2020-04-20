package bachelor.test.locationapp.view

import bachelor.test.locationapp.presenter.BasePresenter
import bachelor.test.locationapp.presenter.LocationData

interface MainScreenContract {
    interface View : BaseView<Presenter> {
        fun showPosition(locationData: LocationData)
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