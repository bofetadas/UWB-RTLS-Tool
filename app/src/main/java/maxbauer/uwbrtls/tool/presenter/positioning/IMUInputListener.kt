package maxbauer.uwbrtls.tool.presenter.positioning

interface IMUInputListener {
    fun onAccelerometerUpdate(values: FloatArray)
    fun onGravitySensorUpdate(values: FloatArray)
    fun onMagnetometerUpdate(values: FloatArray)
}