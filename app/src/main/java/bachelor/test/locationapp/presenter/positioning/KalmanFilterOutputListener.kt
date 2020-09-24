package bachelor.test.locationapp.presenter.positioning

interface KalmanFilterOutputListener {
    fun onNewStateVectorEstimate(uwbLocationData: LocationData, filteredLocationData: LocationData, rawAccelerationData: AccelerationData, filteredAccelerationData: AccelerationData)
}