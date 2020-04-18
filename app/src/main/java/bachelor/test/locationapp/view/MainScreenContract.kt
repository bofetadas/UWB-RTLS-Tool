package bachelor.test.locationapp.view

import bachelor.test.locationapp.presenter.BasePresenter

interface MainScreenContract {
    interface View : BaseView<Presenter> {
        fun showPosition(locationData: LocationData)
        fun showMessage(message: String?)
        fun swapButton()
    }

    interface Presenter: BasePresenter{
        fun start()
        fun onStartClicked()
        fun onStopClicked()
    }
}