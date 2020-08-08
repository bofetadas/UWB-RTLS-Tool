package bachelor.test.locationapp.presenter.positioning

import org.apache.commons.math3.filter.KalmanFilter
import org.apache.commons.math3.linear.ArrayRealVector

class KalmanFilterImpl(private val kalmanFilterCallback: KalmanFilterCallback) {
    private lateinit var kalmanFilter: KalmanFilter
    private val kalmanFilterConfigurator = KalmanFilterConfigurator()
    private val kalmanFilterStrategies = KalmanFilterStrategies()
    private var predictStrategy: (p0: Any?, p1: Any?) -> Unit = kalmanFilterStrategies.notConfigured
    private var correctStrategy: (locationData: LocationData, accData: AccelerationData) -> Unit = kalmanFilterStrategies.notConfigured

    fun configure(initialLocation: LocationData){
        kalmanFilter = kalmanFilterConfigurator.configureKalmanFilter(initialLocation)
        predictStrategy = kalmanFilterStrategies.predict
        correctStrategy = kalmanFilterStrategies.correct
    }

    fun predict(){
        predictStrategy.invoke(null, null)
    }

    fun correct(locationData: LocationData, accelerationData: AccelerationData){
        correctStrategy.invoke(locationData, accelerationData)
    }

    private inner class KalmanFilterStrategies {
        val predict: (p0: Any?, p1: Any?) -> Unit  = {_, _ ->
            kalmanFilter.predict()
        }

        val correct: (locationData: LocationData, accelerationData: AccelerationData) -> Unit = { locationData, accelerationData ->
            val measurementVector = ArrayRealVector(doubleArrayOf(locationData.xPos.toDouble(), locationData.yPos.toDouble(), locationData.zPos.toDouble(), accelerationData.xAcc.toDouble(), accelerationData.yAcc.toDouble(), accelerationData.zAcc.toDouble()))
            kalmanFilter.correct(measurementVector)
            val currentEstimate = kalmanFilter.stateEstimation
            val currentEstimateError = kalmanFilter.errorCovariance
            val estimatedLocationData = LocationData(currentEstimate[0].toFloat(), currentEstimate[1].toFloat(), currentEstimate[2].toFloat())
            println("State Vector")
            for (v in currentEstimate){
                println(v)
            }
            println("State Estimate Error")
            for (e in currentEstimateError){
                for (entry in e){
                    println(entry)
                }
            }
            kalmanFilterCallback.onNewEstimate(estimatedLocationData)
        }

        val notConfigured: (p0: Any?, p1: Any?) -> Unit = {_, _ -> throw IllegalAccessError("You need to call KalmanFilterImpl().configure before making use of it.")}
    }
}