package bachelor.test.locationapp.presenter.positioning

interface KalmanFilterCallback {
    fun onNewEstimate(locationData: LocationData)
}