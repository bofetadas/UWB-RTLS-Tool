package bachelor.test.locationapp.presenter.positioning

interface KalmanFilter {
    fun configure(initialLocationData: LocationData)
    fun predict()
    fun correct(locationData: LocationData, accelerationData: AccelerationData)
}