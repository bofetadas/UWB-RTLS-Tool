package bachelor.test.locationapp.presenter.positioning

interface KalmanFilterOutputListener {
    fun onNewEstimate(locationData: LocationData)
}