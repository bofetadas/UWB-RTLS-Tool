package bachelor.test.locationapp.presenter.positioning

interface IMUInputListener {
    fun onAccelerometerUpdate(values: FloatArray)
    fun onGravitySensorUpdate(values: FloatArray)
    fun onLinearAccelerometerUpdate(values: FloatArray)
    fun onMagnetometerUpdate(values: FloatArray)
}