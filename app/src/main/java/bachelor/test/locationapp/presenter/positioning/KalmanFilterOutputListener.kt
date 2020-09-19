package bachelor.test.locationapp.presenter.positioning

interface KalmanFilterOutputListener {
    fun onNewEstimate(uwbLocationData: LocationData, filteredLocationData: LocationData)
}