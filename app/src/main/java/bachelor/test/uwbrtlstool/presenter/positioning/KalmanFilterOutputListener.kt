package bachelor.test.uwbrtlstool.presenter.positioning

interface KalmanFilterOutputListener {
    fun onNewStateVectorEstimate(uwbLocationData: LocationData, filteredLocationData: LocationData, rawAccelerationData: AccelerationData, filteredAccelerationData: AccelerationData)
}