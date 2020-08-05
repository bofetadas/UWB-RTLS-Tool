package bachelor.test.locationapp.presenter.positioning

import org.apache.commons.math3.filter.KalmanFilter
import org.apache.commons.math3.linear.ArrayRealVector

class KalmanFilterImpl(private val kalmanFilterCallback: KalmanFilterCallback) {
    private lateinit var kalmanFilter: KalmanFilter
    private val kalmanFilterConfigurator = KalmanFilterConfigurator()
    private val kalmanFilterStrategies = KalmanFilterStrategies()
    private var predictStrategy: (accData: AccelerationData) -> Unit = kalmanFilterStrategies.notConfigured
    private var correctStrategy: (locationData: LocationData) -> Unit = kalmanFilterStrategies.notConfigured

    fun configure(initialLocation: LocationData){
        kalmanFilter = kalmanFilterConfigurator.configureKalmanFilter(initialLocation)
        predictStrategy = kalmanFilterStrategies.predict
        correctStrategy = kalmanFilterStrategies.correct
    }

    fun predict(accData: AccelerationData){
        predictStrategy.invoke(accData)
    }

    fun correct(locationData: LocationData){
        correctStrategy.invoke(locationData)
    }

    private inner class KalmanFilterStrategies {
        val predict: (accData: AccelerationData) -> Unit  = { accData ->
            val controlVector = ArrayRealVector(doubleArrayOf(accData.xAcc.toDouble(), accData.yAcc.toDouble(), accData.zAcc.toDouble()))
            kalmanFilter.predict(controlVector)
        }

        val correct: (locationData: LocationData) -> Unit = { locationData ->
            val measurementVector = ArrayRealVector(doubleArrayOf(locationData.xPos.toDouble(), locationData.yPos.toDouble(), locationData.zPos.toDouble()))
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

        val notConfigured: (any: Any) -> Unit = { throw IllegalAccessError("You need to call KalmanFilterImpl().configure before making use of it.")}
    }
}