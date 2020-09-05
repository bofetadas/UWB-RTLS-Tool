package bachelor.test.locationapp.presenter.positioning

import org.ejml.data.DMatrixRMaj
import org.ejml.dense.row.CommonOps_DDRM.*
import org.ejml.dense.row.factory.LinearSolverFactory_DDRM
import org.ejml.interfaces.linsol.LinearSolverDense
import kotlin.math.pow
import kotlin.math.sqrt

// For every 100ms when a new UWB location result comes in, we want the Kalman Filter to calculate
// the best estimate.
private const val TIME_DELTA = 0.1

class KalmanFilterImpl(private val kalmanFilterOutputListener: KalmanFilterOutputListener): KalmanFilter {

    // State estimate matrices
    // x
    private lateinit var stateVector: DMatrixRMaj
    // P
    private lateinit var stateNoiseCovarianceMatrix: DMatrixRMaj

    // Kinematics matrices
    // F
    private lateinit var stateTransitionMatrix: DMatrixRMaj
    // Q
    private lateinit var processNoiseCovarianceMatrix: DMatrixRMaj

    // Measurement matrices
    // H
    private lateinit var measurementTransitionMatrix: DMatrixRMaj
    // R
    private lateinit var measurementNoiseCovarianceMatrix: DMatrixRMaj

    // Helper matrices during Kalman Filter estimation process
    private lateinit var a: DMatrixRMaj
    private lateinit var b: DMatrixRMaj
    private lateinit var y: DMatrixRMaj
    private lateinit var S: DMatrixRMaj
    private lateinit var S_inv: DMatrixRMaj
    private lateinit var c: DMatrixRMaj
    private lateinit var d: DMatrixRMaj
    private lateinit var K: DMatrixRMaj

    private lateinit var solver: LinearSolverDense<DMatrixRMaj>
    private val kalmanFilterStrategies = KalmanFilterStrategies()
    private var predictStrategy: (locationData: LocationData?, accelerationData: AccelerationData?) -> Unit = kalmanFilterStrategies.notConfigured
    private var correctStrategy: (locationData: LocationData, accData: AccelerationData) -> Unit = kalmanFilterStrategies.notConfigured

    override fun configure(initialLocationData: LocationData){
        stateVector = DMatrixRMaj(9, 1)
        stateTransitionMatrix = DMatrixRMaj(9, 9)
        processNoiseCovarianceMatrix = DMatrixRMaj(9, 9)
        stateNoiseCovarianceMatrix = DMatrixRMaj(9, 9)
        measurementTransitionMatrix = DMatrixRMaj(6, 9)
        measurementNoiseCovarianceMatrix = DMatrixRMaj(6, 6)

        val dimenX = stateTransitionMatrix.numCols
        val dimenZ = measurementTransitionMatrix.numRows

        a = DMatrixRMaj(dimenX, 1)
        b = DMatrixRMaj(dimenX, dimenX)
        y = DMatrixRMaj(dimenZ, 1)
        S = DMatrixRMaj(dimenZ, dimenZ)
        S_inv = DMatrixRMaj(dimenZ, dimenZ)
        c = DMatrixRMaj(dimenZ, dimenX)
        d = DMatrixRMaj(dimenX, dimenZ)
        K = DMatrixRMaj(dimenX, dimenZ)

        setStateVector(initialLocationData)
        setStateCovarianceMatrix()
        setStateTransitionMatrix()
        setProcessCovarianceMatrix()
        setMeasurementTransitionMatrix()
        setMeasurementCovarianceMatrix()

        solver = LinearSolverFactory_DDRM.symmPosDef(dimenX)

        // Set appropriate strategies since the filter is now configured
        predictStrategy = kalmanFilterStrategies.predict
        correctStrategy = kalmanFilterStrategies.correct
    }

    override fun predict(accelerationData: AccelerationData){
        predictStrategy.invoke(null, accelerationData)
    }

    override fun correct(locationData: LocationData, accelerationData: AccelerationData){
        correctStrategy.invoke(locationData, accelerationData)
    }

    private fun setStateVector(initialLocationData: LocationData) {
        // x
        stateVector[0, 0] = initialLocationData.xPos
        stateVector[1, 0] = initialLocationData.yPos
        stateVector[2, 0] = initialLocationData.zPos
        stateVector[3, 0] = 0.01
        stateVector[4, 0] = 0.01
        stateVector[5, 0] = 0.01
        stateVector[6, 0] = 0.001
        stateVector[7, 0] = 0.001
        stateVector[8, 0] = 0.001
    }

    private fun setStateTransitionMatrix() {
        // F
        stateTransitionMatrix[0, 0] =  1.0
        stateTransitionMatrix[0, 1] =  0.0
        stateTransitionMatrix[0, 2] =  0.0
        stateTransitionMatrix[0, 3] =  TIME_DELTA
        stateTransitionMatrix[0, 4] =  0.0
        stateTransitionMatrix[0, 5] =  0.0
        stateTransitionMatrix[0, 6] =  0.5 * TIME_DELTA.pow(2)
        stateTransitionMatrix[0, 7] =  0.0
        stateTransitionMatrix[0, 8] =  0.0
        stateTransitionMatrix[1, 0] =  0.0
        stateTransitionMatrix[1, 1] =  1.0
        stateTransitionMatrix[1, 2] =  0.0
        stateTransitionMatrix[1, 3] =  0.0
        stateTransitionMatrix[1, 4] =  TIME_DELTA
        stateTransitionMatrix[1, 5] =  0.0
        stateTransitionMatrix[1, 6] =  0.0
        stateTransitionMatrix[1, 7] =  0.5 * TIME_DELTA.pow(2)
        stateTransitionMatrix[1, 8] =  0.0
        stateTransitionMatrix[2, 0] =  0.0
        stateTransitionMatrix[2, 1] =  0.0
        stateTransitionMatrix[2, 2] =  1.0
        stateTransitionMatrix[2, 3] =  0.0
        stateTransitionMatrix[2, 4] =  0.0
        stateTransitionMatrix[2, 5] =  TIME_DELTA
        stateTransitionMatrix[2, 6] =  0.0
        stateTransitionMatrix[2, 7] =  0.0
        stateTransitionMatrix[2, 8] =  0.5 * TIME_DELTA.pow(2)
        stateTransitionMatrix[3, 0] =  0.0
        stateTransitionMatrix[3, 1] =  0.0
        stateTransitionMatrix[3, 2] =  0.0
        stateTransitionMatrix[3, 3] =  1.0
        stateTransitionMatrix[3, 4] =  0.0
        stateTransitionMatrix[3, 5] =  0.0
        stateTransitionMatrix[3, 6] =  TIME_DELTA
        stateTransitionMatrix[3, 7] =  0.0
        stateTransitionMatrix[3, 8] =  0.0
        stateTransitionMatrix[4, 0] =  0.0
        stateTransitionMatrix[4, 1] =  0.0
        stateTransitionMatrix[4, 2] =  0.0
        stateTransitionMatrix[4, 3] =  0.0
        stateTransitionMatrix[4, 4] =  1.0
        stateTransitionMatrix[4, 5] =  0.0
        stateTransitionMatrix[4, 6] =  0.0
        stateTransitionMatrix[4, 7] =  TIME_DELTA
        stateTransitionMatrix[4, 8] =  0.0
        stateTransitionMatrix[5, 0] =  0.0
        stateTransitionMatrix[5, 1] =  0.0
        stateTransitionMatrix[5, 2] =  0.0
        stateTransitionMatrix[5, 3] =  0.0
        stateTransitionMatrix[5, 4] =  0.0
        stateTransitionMatrix[5, 5] =  1.0
        stateTransitionMatrix[5, 6] =  0.0
        stateTransitionMatrix[5, 7] =  0.0
        stateTransitionMatrix[5, 8] =  TIME_DELTA
        stateTransitionMatrix[6, 0] =  0.0
        stateTransitionMatrix[6, 1] =  0.0
        stateTransitionMatrix[6, 2] =  0.0
        stateTransitionMatrix[6, 3] =  0.0
        stateTransitionMatrix[6, 4] =  0.0
        stateTransitionMatrix[6, 5] =  0.0
        stateTransitionMatrix[6, 6] =  1.0
        stateTransitionMatrix[6, 7] =  0.0
        stateTransitionMatrix[6, 8] =  0.0
        stateTransitionMatrix[7, 0] =  0.0
        stateTransitionMatrix[7, 1] =  0.0
        stateTransitionMatrix[7, 2] =  0.0
        stateTransitionMatrix[7, 3] =  0.0
        stateTransitionMatrix[7, 4] =  0.0
        stateTransitionMatrix[7, 5] =  0.0
        stateTransitionMatrix[7, 6] =  0.0
        stateTransitionMatrix[7, 7] =  1.0
        stateTransitionMatrix[7, 8] =  0.0
        stateTransitionMatrix[8, 0] =  0.0
        stateTransitionMatrix[8, 1] =  0.0
        stateTransitionMatrix[8, 2] =  0.0
        stateTransitionMatrix[8, 3] =  0.0
        stateTransitionMatrix[8, 4] =  0.0
        stateTransitionMatrix[8, 5] =  0.0
        stateTransitionMatrix[8, 6] =  0.0
        stateTransitionMatrix[8, 7] =  0.0
        stateTransitionMatrix[8, 8] =  1.0
    }

    private fun setStateCovarianceMatrix(){
        // P
        stateNoiseCovarianceMatrix[0, 0] =  1.0
        stateNoiseCovarianceMatrix[0, 1] =  0.0
        stateNoiseCovarianceMatrix[0, 2] =  0.0
        stateNoiseCovarianceMatrix[0, 3] =  0.0
        stateNoiseCovarianceMatrix[0, 4] =  0.0
        stateNoiseCovarianceMatrix[0, 5] =  0.0
        stateNoiseCovarianceMatrix[0, 6] =  0.0
        stateNoiseCovarianceMatrix[0, 7] =  0.0
        stateNoiseCovarianceMatrix[0, 8] =  0.0
        stateNoiseCovarianceMatrix[1, 0] =  0.0
        stateNoiseCovarianceMatrix[1, 1] =  1.0
        stateNoiseCovarianceMatrix[1, 2] =  0.0
        stateNoiseCovarianceMatrix[1, 3] =  0.0
        stateNoiseCovarianceMatrix[1, 4] =  0.0
        stateNoiseCovarianceMatrix[1, 5] =  0.0
        stateNoiseCovarianceMatrix[1, 6] =  0.0
        stateNoiseCovarianceMatrix[1, 7] =  0.0
        stateNoiseCovarianceMatrix[1, 8] =  0.0
        stateNoiseCovarianceMatrix[2, 0] =  0.0
        stateNoiseCovarianceMatrix[2, 1] =  0.0
        stateNoiseCovarianceMatrix[2, 2] =  1.0
        stateNoiseCovarianceMatrix[2, 3] =  0.0
        stateNoiseCovarianceMatrix[2, 4] =  0.0
        stateNoiseCovarianceMatrix[2, 5] =  0.0
        stateNoiseCovarianceMatrix[2, 6] =  0.0
        stateNoiseCovarianceMatrix[2, 7] =  0.0
        stateNoiseCovarianceMatrix[2, 8] =  0.0
        stateNoiseCovarianceMatrix[3, 0] =  0.0
        stateNoiseCovarianceMatrix[3, 1] =  0.0
        stateNoiseCovarianceMatrix[3, 2] =  0.0
        stateNoiseCovarianceMatrix[3, 3] =  0.1
        stateNoiseCovarianceMatrix[3, 4] =  0.0
        stateNoiseCovarianceMatrix[3, 5] =  0.0
        stateNoiseCovarianceMatrix[3, 6] =  0.0
        stateNoiseCovarianceMatrix[3, 7] =  0.0
        stateNoiseCovarianceMatrix[3, 8] =  0.0
        stateNoiseCovarianceMatrix[4, 0] =  0.0
        stateNoiseCovarianceMatrix[4, 1] =  0.0
        stateNoiseCovarianceMatrix[4, 2] =  0.0
        stateNoiseCovarianceMatrix[4, 3] =  0.0
        stateNoiseCovarianceMatrix[4, 4] =  0.1
        stateNoiseCovarianceMatrix[4, 5] =  0.0
        stateNoiseCovarianceMatrix[4, 6] =  0.0
        stateNoiseCovarianceMatrix[4, 7] =  0.0
        stateNoiseCovarianceMatrix[4, 8] =  0.0
        stateNoiseCovarianceMatrix[5, 0] =  0.0
        stateNoiseCovarianceMatrix[5, 1] =  0.0
        stateNoiseCovarianceMatrix[5, 2] =  0.0
        stateNoiseCovarianceMatrix[5, 3] =  0.0
        stateNoiseCovarianceMatrix[5, 4] =  0.0
        stateNoiseCovarianceMatrix[5, 5] =  0.1
        stateNoiseCovarianceMatrix[5, 6] =  0.0
        stateNoiseCovarianceMatrix[5, 7] =  0.0
        stateNoiseCovarianceMatrix[5, 8] =  0.0
        stateNoiseCovarianceMatrix[6, 0] =  0.0
        stateNoiseCovarianceMatrix[6, 1] =  0.0
        stateNoiseCovarianceMatrix[6, 2] =  0.0
        stateNoiseCovarianceMatrix[6, 3] =  0.0
        stateNoiseCovarianceMatrix[6, 4] =  0.0
        stateNoiseCovarianceMatrix[6, 5] =  0.0
        stateNoiseCovarianceMatrix[6, 6] =  0.1
        stateNoiseCovarianceMatrix[6, 7] =  0.0
        stateNoiseCovarianceMatrix[6, 8] =  0.0
        stateNoiseCovarianceMatrix[7, 0] =  0.0
        stateNoiseCovarianceMatrix[7, 1] =  0.0
        stateNoiseCovarianceMatrix[7, 2] =  0.0
        stateNoiseCovarianceMatrix[7, 3] =  0.0
        stateNoiseCovarianceMatrix[7, 4] =  0.0
        stateNoiseCovarianceMatrix[7, 5] =  0.0
        stateNoiseCovarianceMatrix[7, 6] =  0.0
        stateNoiseCovarianceMatrix[7, 7] =  0.1
        stateNoiseCovarianceMatrix[7, 8] =  0.0
        stateNoiseCovarianceMatrix[8, 0] =  0.0
        stateNoiseCovarianceMatrix[8, 1] =  0.0
        stateNoiseCovarianceMatrix[8, 2] =  0.0
        stateNoiseCovarianceMatrix[8, 3] =  0.0
        stateNoiseCovarianceMatrix[8, 4] =  0.0
        stateNoiseCovarianceMatrix[8, 5] =  0.0
        stateNoiseCovarianceMatrix[8, 6] =  0.0
        stateNoiseCovarianceMatrix[8, 7] =  0.0
        stateNoiseCovarianceMatrix[8, 8] =  0.1
    }

    private fun setProcessCovarianceMatrix() {
        // Complete Covariance Q
        /*processNoiseCovarianceMatrix[0, 0] =  0.25 * TIME_DELTA.pow(4)
        processNoiseCovarianceMatrix[0, 1] =  0.25 * TIME_DELTA.pow(4)
        processNoiseCovarianceMatrix[0, 2] =  0.25 * TIME_DELTA.pow(4)
        processNoiseCovarianceMatrix[0, 3] =  0.5 * TIME_DELTA.pow(3)
        processNoiseCovarianceMatrix[0, 4] =  0.5 * TIME_DELTA.pow(3)
        processNoiseCovarianceMatrix[0, 5] =  0.5 * TIME_DELTA.pow(3)
        processNoiseCovarianceMatrix[0, 6] =  0.5 * TIME_DELTA.pow(2)
        processNoiseCovarianceMatrix[0, 7] =  0.5 * TIME_DELTA.pow(2)
        processNoiseCovarianceMatrix[0, 8] =  0.5 * TIME_DELTA.pow(2)
        processNoiseCovarianceMatrix[1, 0] =  0.25 * TIME_DELTA.pow(4)
        processNoiseCovarianceMatrix[1, 1] =  0.25 * TIME_DELTA.pow(4)
        processNoiseCovarianceMatrix[1, 2] =  0.25 * TIME_DELTA.pow(4)
        processNoiseCovarianceMatrix[1, 3] =  0.5 * TIME_DELTA.pow(3)
        processNoiseCovarianceMatrix[1, 4] =  0.5 * TIME_DELTA.pow(3)
        processNoiseCovarianceMatrix[1, 5] =  0.5 * TIME_DELTA.pow(3)
        processNoiseCovarianceMatrix[1, 6] =  0.5 * TIME_DELTA.pow(2)
        processNoiseCovarianceMatrix[1, 7] =  0.5 * TIME_DELTA.pow(2)
        processNoiseCovarianceMatrix[1, 8] =  0.5 * TIME_DELTA.pow(2)
        processNoiseCovarianceMatrix[2, 0] =  0.25 * TIME_DELTA.pow(4)
        processNoiseCovarianceMatrix[2, 1] =  0.25 * TIME_DELTA.pow(4)
        processNoiseCovarianceMatrix[2, 2] =  0.25 * TIME_DELTA.pow(4)
        processNoiseCovarianceMatrix[2, 3] =  0.5 * TIME_DELTA.pow(3)
        processNoiseCovarianceMatrix[2, 4] =  0.5 * TIME_DELTA.pow(3)
        processNoiseCovarianceMatrix[2, 5] =  0.5 * TIME_DELTA.pow(3)
        processNoiseCovarianceMatrix[2, 6] =  0.5 * TIME_DELTA.pow(2)
        processNoiseCovarianceMatrix[2, 7] =  0.5 * TIME_DELTA.pow(2)
        processNoiseCovarianceMatrix[2, 8] =  0.5 * TIME_DELTA.pow(2)
        processNoiseCovarianceMatrix[3, 0] =  0.5 * TIME_DELTA.pow(3)
        processNoiseCovarianceMatrix[3, 1] =  0.5 * TIME_DELTA.pow(3)
        processNoiseCovarianceMatrix[3, 2] =  0.5 * TIME_DELTA.pow(3)
        processNoiseCovarianceMatrix[3, 3] =  TIME_DELTA.pow(2)
        processNoiseCovarianceMatrix[3, 4] =  TIME_DELTA.pow(2)
        processNoiseCovarianceMatrix[3, 5] =  TIME_DELTA.pow(2)
        processNoiseCovarianceMatrix[3, 6] =  TIME_DELTA
        processNoiseCovarianceMatrix[3, 7] =  TIME_DELTA
        processNoiseCovarianceMatrix[3, 8] =  TIME_DELTA
        processNoiseCovarianceMatrix[4, 0] =  0.5 * TIME_DELTA.pow(3)
        processNoiseCovarianceMatrix[4, 1] =  0.5 * TIME_DELTA.pow(3)
        processNoiseCovarianceMatrix[4, 2] =  0.5 * TIME_DELTA.pow(3)
        processNoiseCovarianceMatrix[4, 3] =  TIME_DELTA.pow(2)
        processNoiseCovarianceMatrix[4, 4] =  TIME_DELTA.pow(2)
        processNoiseCovarianceMatrix[4, 5] =  TIME_DELTA.pow(2)
        processNoiseCovarianceMatrix[4, 6] =  TIME_DELTA
        processNoiseCovarianceMatrix[4, 7] =  TIME_DELTA
        processNoiseCovarianceMatrix[4, 8] =  TIME_DELTA
        processNoiseCovarianceMatrix[5, 0] =  0.5 * TIME_DELTA.pow(3)
        processNoiseCovarianceMatrix[5, 1] =  0.5 * TIME_DELTA.pow(3)
        processNoiseCovarianceMatrix[5, 2] =  0.5 * TIME_DELTA.pow(3)
        processNoiseCovarianceMatrix[5, 3] =  TIME_DELTA.pow(2)
        processNoiseCovarianceMatrix[5, 4] =  TIME_DELTA.pow(2)
        processNoiseCovarianceMatrix[5, 5] =  TIME_DELTA.pow(2)
        processNoiseCovarianceMatrix[5, 6] =  TIME_DELTA
        processNoiseCovarianceMatrix[5, 7] =  TIME_DELTA
        processNoiseCovarianceMatrix[5, 8] =  TIME_DELTA
        processNoiseCovarianceMatrix[6, 0] =  0.5 * TIME_DELTA.pow(2)
        processNoiseCovarianceMatrix[6, 1] =  0.5 * TIME_DELTA.pow(2)
        processNoiseCovarianceMatrix[6, 2] =  0.5 * TIME_DELTA.pow(2)
        processNoiseCovarianceMatrix[6, 3] =  TIME_DELTA
        processNoiseCovarianceMatrix[6, 4] =  TIME_DELTA
        processNoiseCovarianceMatrix[6, 5] =  TIME_DELTA
        processNoiseCovarianceMatrix[6, 6] =  1.0
        processNoiseCovarianceMatrix[6, 7] =  1.0
        processNoiseCovarianceMatrix[6, 8] =  1.0
        processNoiseCovarianceMatrix[7, 0] =  0.5 * TIME_DELTA.pow(2)
        processNoiseCovarianceMatrix[7, 1] =  0.5 * TIME_DELTA.pow(2)
        processNoiseCovarianceMatrix[7, 2] =  0.5 * TIME_DELTA.pow(2)
        processNoiseCovarianceMatrix[7, 3] =  TIME_DELTA
        processNoiseCovarianceMatrix[7, 4] =  TIME_DELTA
        processNoiseCovarianceMatrix[7, 5] =  TIME_DELTA
        processNoiseCovarianceMatrix[7, 6] =  1.0
        processNoiseCovarianceMatrix[7, 7] =  1.0
        processNoiseCovarianceMatrix[7, 8] =  1.0
        processNoiseCovarianceMatrix[8, 0] =  0.5 * TIME_DELTA.pow(2)
        processNoiseCovarianceMatrix[8, 1] =  0.5 * TIME_DELTA.pow(2)
        processNoiseCovarianceMatrix[8, 2] =  0.5 * TIME_DELTA.pow(2)
        processNoiseCovarianceMatrix[8, 3] =  TIME_DELTA
        processNoiseCovarianceMatrix[8, 4] =  TIME_DELTA
        processNoiseCovarianceMatrix[8, 5] =  TIME_DELTA
        processNoiseCovarianceMatrix[8, 6] =  1.0
        processNoiseCovarianceMatrix[8, 7] =  1.0
        processNoiseCovarianceMatrix[8, 8] =  1.0*/
        /*for (i in processNoiseCovarianceMatrix.data.indices){
            processNoiseCovarianceMatrix[i] *= MAX_ACCELERATION_VARIANCE
        }*/

        // Simple Experimental Q
        processNoiseCovarianceMatrix[0, 0] =  0.1
        processNoiseCovarianceMatrix[0, 1] =  0.0
        processNoiseCovarianceMatrix[0, 2] =  0.0
        processNoiseCovarianceMatrix[0, 3] =  0.0
        processNoiseCovarianceMatrix[0, 4] =  0.0
        processNoiseCovarianceMatrix[0, 5] =  0.0
        processNoiseCovarianceMatrix[0, 6] =  0.0
        processNoiseCovarianceMatrix[0, 7] =  0.0
        processNoiseCovarianceMatrix[0, 8] =  0.0
        processNoiseCovarianceMatrix[1, 0] =  0.0
        processNoiseCovarianceMatrix[1, 1] =  0.1
        processNoiseCovarianceMatrix[1, 2] =  0.0
        processNoiseCovarianceMatrix[1, 3] =  0.0
        processNoiseCovarianceMatrix[1, 4] =  0.0
        processNoiseCovarianceMatrix[1, 5] =  0.0
        processNoiseCovarianceMatrix[1, 6] =  0.0
        processNoiseCovarianceMatrix[1, 7] =  0.0
        processNoiseCovarianceMatrix[1, 8] =  0.0
        processNoiseCovarianceMatrix[2, 0] =  0.0
        processNoiseCovarianceMatrix[2, 1] =  0.0
        processNoiseCovarianceMatrix[2, 2] =  0.01
        processNoiseCovarianceMatrix[2, 3] =  0.0
        processNoiseCovarianceMatrix[2, 4] =  0.0
        processNoiseCovarianceMatrix[2, 5] =  0.0
        processNoiseCovarianceMatrix[2, 6] =  0.0
        processNoiseCovarianceMatrix[2, 7] =  0.0
        processNoiseCovarianceMatrix[2, 8] =  0.0
        processNoiseCovarianceMatrix[3, 0] =  0.0
        processNoiseCovarianceMatrix[3, 1] =  0.0
        processNoiseCovarianceMatrix[3, 2] =  0.0
        processNoiseCovarianceMatrix[3, 3] =  0.2
        processNoiseCovarianceMatrix[3, 4] =  0.0
        processNoiseCovarianceMatrix[3, 5] =  0.0
        processNoiseCovarianceMatrix[3, 6] =  0.0
        processNoiseCovarianceMatrix[3, 7] =  0.0
        processNoiseCovarianceMatrix[3, 8] =  0.0
        processNoiseCovarianceMatrix[4, 0] =  0.0
        processNoiseCovarianceMatrix[4, 1] =  0.0
        processNoiseCovarianceMatrix[4, 2] =  0.0
        processNoiseCovarianceMatrix[4, 3] =  0.0
        processNoiseCovarianceMatrix[4, 4] =  0.2
        processNoiseCovarianceMatrix[4, 5] =  0.0
        processNoiseCovarianceMatrix[4, 6] =  0.0
        processNoiseCovarianceMatrix[4, 7] =  0.0
        processNoiseCovarianceMatrix[4, 8] =  0.0
        processNoiseCovarianceMatrix[5, 0] =  0.0
        processNoiseCovarianceMatrix[5, 1] =  0.0
        processNoiseCovarianceMatrix[5, 2] =  0.0
        processNoiseCovarianceMatrix[5, 3] =  0.0
        processNoiseCovarianceMatrix[5, 4] =  0.0
        processNoiseCovarianceMatrix[5, 5] =  0.02
        processNoiseCovarianceMatrix[5, 6] =  0.0
        processNoiseCovarianceMatrix[5, 7] =  0.0
        processNoiseCovarianceMatrix[5, 8] =  0.0
        processNoiseCovarianceMatrix[6, 0] =  0.0
        processNoiseCovarianceMatrix[6, 1] =  0.0
        processNoiseCovarianceMatrix[6, 2] =  0.0
        processNoiseCovarianceMatrix[6, 3] =  0.0
        processNoiseCovarianceMatrix[6, 4] =  0.0
        processNoiseCovarianceMatrix[6, 5] =  0.0
        processNoiseCovarianceMatrix[6, 6] =  0.5
        processNoiseCovarianceMatrix[6, 7] =  0.0
        processNoiseCovarianceMatrix[6, 8] =  0.0
        processNoiseCovarianceMatrix[7, 0] =  0.0
        processNoiseCovarianceMatrix[7, 1] =  0.0
        processNoiseCovarianceMatrix[7, 2] =  0.0
        processNoiseCovarianceMatrix[7, 3] =  0.0
        processNoiseCovarianceMatrix[7, 4] =  0.0
        processNoiseCovarianceMatrix[7, 5] =  0.0
        processNoiseCovarianceMatrix[7, 6] =  0.0
        processNoiseCovarianceMatrix[7, 7] =  0.5
        processNoiseCovarianceMatrix[7, 8] =  0.0
        processNoiseCovarianceMatrix[8, 0] =  0.0
        processNoiseCovarianceMatrix[8, 1] =  0.0
        processNoiseCovarianceMatrix[8, 2] =  0.0
        processNoiseCovarianceMatrix[8, 3] =  0.0
        processNoiseCovarianceMatrix[8, 4] =  0.0
        processNoiseCovarianceMatrix[8, 5] =  0.0
        processNoiseCovarianceMatrix[8, 6] =  0.0
        processNoiseCovarianceMatrix[8, 7] =  0.0
        processNoiseCovarianceMatrix[8, 8] =  0.05
    }

    private fun setMeasurementTransitionMatrix() {
        // H
        measurementTransitionMatrix[0, 0] = 1.0
        measurementTransitionMatrix[0, 1] = 0.0
        measurementTransitionMatrix[0, 2] = 0.0
        measurementTransitionMatrix[0, 3] = 0.0
        measurementTransitionMatrix[0, 4] = 0.0
        measurementTransitionMatrix[0, 5] = 0.0
        measurementTransitionMatrix[0, 6] = 0.0
        measurementTransitionMatrix[0, 7] = 0.0
        measurementTransitionMatrix[0, 8] = 0.0
        measurementTransitionMatrix[1, 0] = 0.0
        measurementTransitionMatrix[1, 1] = 1.0
        measurementTransitionMatrix[1, 2] = 0.0
        measurementTransitionMatrix[1, 3] = 0.0
        measurementTransitionMatrix[1, 4] = 0.0
        measurementTransitionMatrix[1, 5] = 0.0
        measurementTransitionMatrix[1, 6] = 0.0
        measurementTransitionMatrix[1, 7] = 0.0
        measurementTransitionMatrix[1, 8] = 0.0
        measurementTransitionMatrix[2, 0] = 0.0
        measurementTransitionMatrix[2, 1] = 0.0
        measurementTransitionMatrix[2, 2] = 1.0
        measurementTransitionMatrix[2, 3] = 0.0
        measurementTransitionMatrix[2, 4] = 0.0
        measurementTransitionMatrix[2, 5] = 0.0
        measurementTransitionMatrix[2, 6] = 0.0
        measurementTransitionMatrix[2, 7] = 0.0
        measurementTransitionMatrix[2, 8] = 0.0
        measurementTransitionMatrix[3, 0] = 0.0
        measurementTransitionMatrix[3, 1] = 0.0
        measurementTransitionMatrix[3, 2] = 0.0
        measurementTransitionMatrix[3, 3] = 0.0
        measurementTransitionMatrix[3, 4] = 0.0
        measurementTransitionMatrix[3, 5] = 0.0
        measurementTransitionMatrix[3, 6] = 1.0
        measurementTransitionMatrix[3, 7] = 0.0
        measurementTransitionMatrix[3, 8] = 0.0
        measurementTransitionMatrix[4, 0] = 0.0
        measurementTransitionMatrix[4, 1] = 0.0
        measurementTransitionMatrix[4, 2] = 0.0
        measurementTransitionMatrix[4, 3] = 0.0
        measurementTransitionMatrix[4, 4] = 0.0
        measurementTransitionMatrix[4, 5] = 0.0
        measurementTransitionMatrix[4, 6] = 0.0
        measurementTransitionMatrix[4, 7] = 1.0
        measurementTransitionMatrix[4, 8] = 0.0
        measurementTransitionMatrix[5, 0] = 0.0
        measurementTransitionMatrix[5, 1] = 0.0
        measurementTransitionMatrix[5, 2] = 0.0
        measurementTransitionMatrix[5, 3] = 0.0
        measurementTransitionMatrix[5, 4] = 0.0
        measurementTransitionMatrix[5, 5] = 0.0
        measurementTransitionMatrix[5, 6] = 0.0
        measurementTransitionMatrix[5, 7] = 0.0
        measurementTransitionMatrix[5, 8] = 1.0
    }

    private fun setMeasurementCovarianceMatrix() {
        // Complete Covariance R
        /*measurementNoiseCovarianceMatrix[0, 0] =  0.005
        measurementNoiseCovarianceMatrix[0, 1] =  0.0023
        measurementNoiseCovarianceMatrix[0, 2] =  0.0018
        measurementNoiseCovarianceMatrix[0, 3] =  0.0
        measurementNoiseCovarianceMatrix[0, 4] =  0.0
        measurementNoiseCovarianceMatrix[0, 5] =  0.0
        measurementNoiseCovarianceMatrix[1, 0] =  0.0023
        measurementNoiseCovarianceMatrix[1, 1] =  0.0137
        measurementNoiseCovarianceMatrix[1, 2] =  0.0036
        measurementNoiseCovarianceMatrix[1, 3] =  0.0
        measurementNoiseCovarianceMatrix[1, 4] =  0.0
        measurementNoiseCovarianceMatrix[1, 5] =  0.0
        measurementNoiseCovarianceMatrix[2, 0] =  0.0018
        measurementNoiseCovarianceMatrix[2, 1] =  0.0036
        measurementNoiseCovarianceMatrix[2, 2] =  0.029
        measurementNoiseCovarianceMatrix[2, 3] =  0.0
        measurementNoiseCovarianceMatrix[2, 4] =  0.0
        measurementNoiseCovarianceMatrix[2, 5] =  0.0
        measurementNoiseCovarianceMatrix[3, 0] =  0.0
        measurementNoiseCovarianceMatrix[3, 1] =  0.0
        measurementNoiseCovarianceMatrix[3, 2] =  0.0
        measurementNoiseCovarianceMatrix[3, 3] =  0.000641
        measurementNoiseCovarianceMatrix[3, 4] =  0.000050
        measurementNoiseCovarianceMatrix[3, 5] =  -0.000045
        measurementNoiseCovarianceMatrix[4, 0] =  0.0
        measurementNoiseCovarianceMatrix[4, 1] =  0.0
        measurementNoiseCovarianceMatrix[4, 2] =  0.0
        measurementNoiseCovarianceMatrix[4, 3] =  0.000050
        measurementNoiseCovarianceMatrix[4, 4] =  0.000276
        measurementNoiseCovarianceMatrix[4, 5] =  0.000168
        measurementNoiseCovarianceMatrix[5, 0] =  0.0
        measurementNoiseCovarianceMatrix[5, 1] =  0.0
        measurementNoiseCovarianceMatrix[5, 2] =  0.0
        measurementNoiseCovarianceMatrix[5, 3] =  -0.000045
        measurementNoiseCovarianceMatrix[5, 4] =  0.000168
        measurementNoiseCovarianceMatrix[5, 5] =  0.009926*/

        // Simple R
        measurementNoiseCovarianceMatrix[0, 0] =  0.005
        measurementNoiseCovarianceMatrix[0, 1] =  0.0
        measurementNoiseCovarianceMatrix[0, 2] =  0.0
        measurementNoiseCovarianceMatrix[0, 3] =  0.0
        measurementNoiseCovarianceMatrix[0, 4] =  0.0
        measurementNoiseCovarianceMatrix[0, 5] =  0.0
        measurementNoiseCovarianceMatrix[1, 0] =  0.0
        measurementNoiseCovarianceMatrix[1, 1] =  0.0137
        measurementNoiseCovarianceMatrix[1, 2] =  0.0
        measurementNoiseCovarianceMatrix[1, 3] =  0.0
        measurementNoiseCovarianceMatrix[1, 4] =  0.0
        measurementNoiseCovarianceMatrix[1, 5] =  0.0
        measurementNoiseCovarianceMatrix[2, 0] =  0.0
        measurementNoiseCovarianceMatrix[2, 1] =  0.0
        measurementNoiseCovarianceMatrix[2, 2] =  0.029
        measurementNoiseCovarianceMatrix[2, 3] =  0.0
        measurementNoiseCovarianceMatrix[2, 4] =  0.0
        measurementNoiseCovarianceMatrix[2, 5] =  0.0
        measurementNoiseCovarianceMatrix[3, 0] =  0.0
        measurementNoiseCovarianceMatrix[3, 1] =  0.0
        measurementNoiseCovarianceMatrix[3, 2] =  0.0
        measurementNoiseCovarianceMatrix[3, 3] =  0.0001
        measurementNoiseCovarianceMatrix[3, 4] =  0.0
        measurementNoiseCovarianceMatrix[3, 5] =  0.0
        measurementNoiseCovarianceMatrix[4, 0] =  0.0
        measurementNoiseCovarianceMatrix[4, 1] =  0.0
        measurementNoiseCovarianceMatrix[4, 2] =  0.0
        measurementNoiseCovarianceMatrix[4, 3] =  0.0
        measurementNoiseCovarianceMatrix[4, 4] =  0.0001
        measurementNoiseCovarianceMatrix[4, 5] =  0.0
        measurementNoiseCovarianceMatrix[5, 0] =  0.0
        measurementNoiseCovarianceMatrix[5, 1] =  0.0
        measurementNoiseCovarianceMatrix[5, 2] =  0.0
        measurementNoiseCovarianceMatrix[5, 3] =  0.0
        measurementNoiseCovarianceMatrix[5, 4] =  0.0
        measurementNoiseCovarianceMatrix[5, 5] =  0.009926
    }

    private inner class KalmanFilterStrategies {
        val predict: (locationData: LocationData?, accelerationData: AccelerationData?) -> Unit  = { _, accelerationData ->
            // Current overall acceleration
            val overallAcceleration = sqrt(accelerationData!!.xAcc.pow(2) + accelerationData.yAcc.pow(2) + accelerationData.zAcc.pow(2))
            val length = processNoiseCovarianceMatrix.numElements
            val data = DoubleArray(length)
            System.arraycopy(processNoiseCovarianceMatrix.data, 0, data, 0, length)
            val q = DMatrixRMaj(data)
            val dimension = sqrt(length.toFloat()).toInt()
            q.numRows = dimension
            q.numCols = dimension
            for (i in q.data.indices){
                q[i] *= overallAcceleration
            }
            println("Overall Acc: $overallAcceleration")

            // x = F * x
            mult(stateTransitionMatrix, stateVector, a)
            stateVector.set(a)

            // P = F * P * F' + Q
            mult(stateTransitionMatrix, stateNoiseCovarianceMatrix, b)
            multTransB(b, stateTransitionMatrix, stateNoiseCovarianceMatrix)
            addEquals(stateNoiseCovarianceMatrix, q)
        }

        val correct: (locationData: LocationData, accelerationData: AccelerationData) -> Unit = correct@ { locationData, accelerationData ->
            // y = z - H * x
            val measurementVector = DMatrixRMaj(doubleArrayOf(locationData.xPos, locationData.yPos, locationData.zPos, accelerationData.xAcc, accelerationData.yAcc, accelerationData.zAcc))
            mult(measurementTransitionMatrix, stateVector, y)
            subtract(measurementVector, y, y)

            // S = H * P * H' + R
            mult(measurementTransitionMatrix, stateNoiseCovarianceMatrix, c)
            multTransB(c, measurementTransitionMatrix, S)
            addEquals(S, measurementNoiseCovarianceMatrix)

            // K = P * H' * S^(-1)
            if(!solver.setA(S)){
                println("RESET")
                // Reset P to avoid diverging Kalman Filter
                setStateCovarianceMatrix()
                return@correct
            }
            solver.invert(S_inv)
            multTransA(measurementTransitionMatrix, S_inv, d)
            mult(stateNoiseCovarianceMatrix, d, K)

            // x = x + K * y
            mult(K,y,a)
            addEquals(stateVector, a)

            // P = (I - k * H) * P = P - (K * H) * P = P - K * (H * P)
            mult(measurementTransitionMatrix, stateNoiseCovarianceMatrix, c)
            mult(K, c, b)
            subtractEquals(stateNoiseCovarianceMatrix, b)

            kalmanFilterOutputListener.onNewEstimate(LocationData(stateVector[0, 0], stateVector[1, 0], stateVector[2, 0]))
        }

        val notConfigured: (p0: Any?, p1: Any?) -> Unit = {_, _ -> throw IllegalAccessError("You need to call KalmanFilterImpl().configure before making use of it.")}
    }
}