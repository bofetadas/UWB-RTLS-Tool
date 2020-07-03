package bachelor.test.locationapp.presenter.positioning

interface IMUInputListener {
    fun onGravitySensorUpdate(values: FloatArray)
    fun onLinearAccelerometerUpdate(values: FloatArray)
    fun onMagnetometerUpdate(values: FloatArray)
}