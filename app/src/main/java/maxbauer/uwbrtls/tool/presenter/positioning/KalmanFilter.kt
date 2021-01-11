package maxbauer.uwbrtls.tool.presenter.positioning

interface KalmanFilter {
    fun configure(initialLocationData: LocationData)
    fun predict(accelerationData: AccelerationData)
    fun update(locationData: LocationData, accelerationData: AccelerationData, orientationData: OrientationData)
}