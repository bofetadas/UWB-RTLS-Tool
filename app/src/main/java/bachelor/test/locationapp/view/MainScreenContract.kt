package bachelor.test.locationapp.view

import bachelor.test.locationapp.presenter.BasePresenter

interface MainScreenContract {
    interface View : BaseView<Presenter> {
        fun enableConnectButton(enabled: Boolean)
        fun swapStartButton(start: Boolean)
        fun showMessage(message: String?)
        fun updateDistance(distance: Double)
        fun changeBackground(mode: String)
    }

    interface Presenter: BasePresenter {
        fun start()
        fun stop()
        fun onConnectClicked()
        fun onDisconnectClicked()
        fun onStartClicked()
        fun onStopClicked()
        fun onMovementDetected(movement: Boolean)
    }
}