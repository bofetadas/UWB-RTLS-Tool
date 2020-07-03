package bachelor.test.locationapp.presenter.positioning

interface IMUOutputListener {
    fun onWorldAccelerationCalculated(accelerationData: AccelerationData)
}