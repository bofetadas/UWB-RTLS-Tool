package bachelor.test.locationapp.presenter.positioning

class TrapezoidalIntegration {

    @Synchronized
    fun integrate(previousTimestamp: Long, values: ArrayList<Float>): Float{
        val timeInterval: Float = (System.currentTimeMillis() - previousTimestamp) / 1000f
        val h = timeInterval / (values.size - 1)
        var sum = 0.0f
        sum += values.removeAt(0)
        sum += values.removeAt(values.size - 1)
        for (v in values){
            sum += 2 * v
        }
        sum *= (h/2)
        return sum
    }
}