package bachelor.test.locationapp.presenter.positioning

interface IMUOutputListener {
    fun onAccelerationCalculated(accelerationData: AccelerationData)
}