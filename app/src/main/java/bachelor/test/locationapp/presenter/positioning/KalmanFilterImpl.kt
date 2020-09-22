package bachelor.test.locationapp.presenter.positioning

import org.ejml.data.DMatrixRMaj
import org.ejml.dense.row.CommonOps_DDRM.*
import org.ejml.dense.row.factory.LinearSolverFactory_DDRM
import org.ejml.interfaces.linsol.LinearSolverDense
import kotlin.math.pow
import kotlin.math.sqrt

// UWB position calculation delivery frequency (via Bluetooth in seconds)
private const val TIME_DELTA = 0.1

// Roll threshold to detect whether the device is lying on its back facing the sky
private const val ORIENTATION_ROLL_THRESHOLD = 50f

// Measurement Noise Covariance values for the Z acceleration at index [5, 5]
// Since the device's accelerometer delivers highly noisy Z acceleration values when the device is lying
// on its back with the screen facing the sky, we have to adjust the measurement noise for these situations
private const val MEASUREMENT_NOISE_RELIABLE_Z_ACCELERATION = 10.0
private const val MEASUREMENT_NOISE_UNRELIABLE_Z_ACCELERATION = 50.0

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

    // Kalman filter process matrices
    // z
    private lateinit var measurementVector: DMatrixRMaj
    // y
    private lateinit var innovationVector: DMatrixRMaj
    // S
    private lateinit var innovationCovarianceMatrix: DMatrixRMaj
    // S inverted
    private lateinit var innovationCovarianceMatrixInverted: DMatrixRMaj
    // K
    private lateinit var kalmanGainMatrix: DMatrixRMaj

    // Helper matrices
    private lateinit var a: DMatrixRMaj
    private lateinit var b: DMatrixRMaj
    private lateinit var c: DMatrixRMaj
    private lateinit var d: DMatrixRMaj

    // Linear solver
    private lateinit var solver: LinearSolverDense<DMatrixRMaj>

    // Filter strategies
    private val kalmanFilterStrategies = KalmanFilterStrategies()
    private var predictStrategy: (locationData: LocationData?, accelerationData: AccelerationData?, orientationData: OrientationData?) -> Unit = kalmanFilterStrategies.notConfigured
    private var updateStrategy: (locationData: LocationData, accData: AccelerationData, orientationData: OrientationData) -> Unit = kalmanFilterStrategies.notConfigured

    override fun configure(initialLocationData: LocationData){
        stateVector = DMatrixRMaj(9, 1)
        stateTransitionMatrix = DMatrixRMaj(9, 9)
        processNoiseCovarianceMatrix = DMatrixRMaj(9, 9)
        stateNoiseCovarianceMatrix = DMatrixRMaj(9, 9)
        measurementTransitionMatrix = DMatrixRMaj(6, 9)
        measurementNoiseCovarianceMatrix = DMatrixRMaj(6, 6)

        val dimenX = stateTransitionMatrix.numCols
        val dimenZ = measurementTransitionMatrix.numRows

        innovationVector = DMatrixRMaj(dimenZ, 1)
        innovationCovarianceMatrix = DMatrixRMaj(dimenZ, dimenZ)
        innovationCovarianceMatrixInverted = DMatrixRMaj(dimenZ, dimenZ)
        kalmanGainMatrix = DMatrixRMaj(dimenX, dimenZ)
        a = DMatrixRMaj(dimenX, 1)
        b = DMatrixRMaj(dimenX, dimenX)
        c = DMatrixRMaj(dimenZ, dimenX)
        d = DMatrixRMaj(dimenX, dimenZ)

        setStateVector(initialLocationData)
        setStateCovarianceMatrix()
        setStateTransitionMatrix()
        setProcessCovarianceMatrix()
        setMeasurementTransitionMatrix()
        setMeasurementCovarianceMatrix()

        solver = LinearSolverFactory_DDRM.symmPosDef(dimenX)

        // Set appropriate strategies since the filter is now configured
        predictStrategy = kalmanFilterStrategies.predict
        updateStrategy = kalmanFilterStrategies.update
    }

    override fun predict(accelerationData: AccelerationData){
        predictStrategy.invoke(null, accelerationData, null)
    }

    override fun update(locationData: LocationData, accelerationData: AccelerationData, orientationData: OrientationData){
        updateStrategy.invoke(locationData, accelerationData, orientationData)
    }

    private fun setStateVector(initialLocationData: LocationData) {
        // x
        stateVector[0, 0] = initialLocationData.xPos
        stateVector[1, 0] = initialLocationData.yPos
        stateVector[2, 0] = initialLocationData.zPos
        stateVector[3, 0] = 0.0
        stateVector[4, 0] = 0.0
        stateVector[5, 0] = 0.0
        stateVector[6, 0] = 0.0
        stateVector[7, 0] = 0.0
        stateVector[8, 0] = 0.0
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
        stateNoiseCovarianceMatrix[0, 0] =  0.01
        stateNoiseCovarianceMatrix[0, 1] =  0.0
        stateNoiseCovarianceMatrix[0, 2] =  0.0
        stateNoiseCovarianceMatrix[0, 3] =  0.0
        stateNoiseCovarianceMatrix[0, 4] =  0.0
        stateNoiseCovarianceMatrix[0, 5] =  0.0
        stateNoiseCovarianceMatrix[0, 6] =  0.0
        stateNoiseCovarianceMatrix[0, 7] =  0.0
        stateNoiseCovarianceMatrix[0, 8] =  0.0
        stateNoiseCovarianceMatrix[1, 0] =  0.0
        stateNoiseCovarianceMatrix[1, 1] =  0.01
        stateNoiseCovarianceMatrix[1, 2] =  0.0
        stateNoiseCovarianceMatrix[1, 3] =  0.0
        stateNoiseCovarianceMatrix[1, 4] =  0.0
        stateNoiseCovarianceMatrix[1, 5] =  0.0
        stateNoiseCovarianceMatrix[1, 6] =  0.0
        stateNoiseCovarianceMatrix[1, 7] =  0.0
        stateNoiseCovarianceMatrix[1, 8] =  0.0
        stateNoiseCovarianceMatrix[2, 0] =  0.0
        stateNoiseCovarianceMatrix[2, 1] =  0.0
        stateNoiseCovarianceMatrix[2, 2] =  0.001
        stateNoiseCovarianceMatrix[2, 3] =  0.0
        stateNoiseCovarianceMatrix[2, 4] =  0.0
        stateNoiseCovarianceMatrix[2, 5] =  0.0
        stateNoiseCovarianceMatrix[2, 6] =  0.0
        stateNoiseCovarianceMatrix[2, 7] =  0.0
        stateNoiseCovarianceMatrix[2, 8] =  0.0
        stateNoiseCovarianceMatrix[3, 0] =  0.0
        stateNoiseCovarianceMatrix[3, 1] =  0.0
        stateNoiseCovarianceMatrix[3, 2] =  0.0
        stateNoiseCovarianceMatrix[3, 3] =  0.01
        stateNoiseCovarianceMatrix[3, 4] =  0.0
        stateNoiseCovarianceMatrix[3, 5] =  0.0
        stateNoiseCovarianceMatrix[3, 6] =  0.0
        stateNoiseCovarianceMatrix[3, 7] =  0.0
        stateNoiseCovarianceMatrix[3, 8] =  0.0
        stateNoiseCovarianceMatrix[4, 0] =  0.0
        stateNoiseCovarianceMatrix[4, 1] =  0.0
        stateNoiseCovarianceMatrix[4, 2] =  0.0
        stateNoiseCovarianceMatrix[4, 3] =  0.0
        stateNoiseCovarianceMatrix[4, 4] =  0.01
        stateNoiseCovarianceMatrix[4, 5] =  0.0
        stateNoiseCovarianceMatrix[4, 6] =  0.0
        stateNoiseCovarianceMatrix[4, 7] =  0.0
        stateNoiseCovarianceMatrix[4, 8] =  0.0
        stateNoiseCovarianceMatrix[5, 0] =  0.0
        stateNoiseCovarianceMatrix[5, 1] =  0.0
        stateNoiseCovarianceMatrix[5, 2] =  0.0
        stateNoiseCovarianceMatrix[5, 3] =  0.0
        stateNoiseCovarianceMatrix[5, 4] =  0.0
        stateNoiseCovarianceMatrix[5, 5] =  0.001
        stateNoiseCovarianceMatrix[5, 6] =  0.0
        stateNoiseCovarianceMatrix[5, 7] =  0.0
        stateNoiseCovarianceMatrix[5, 8] =  0.0
        stateNoiseCovarianceMatrix[6, 0] =  0.0
        stateNoiseCovarianceMatrix[6, 1] =  0.0
        stateNoiseCovarianceMatrix[6, 2] =  0.0
        stateNoiseCovarianceMatrix[6, 3] =  0.0
        stateNoiseCovarianceMatrix[6, 4] =  0.0
        stateNoiseCovarianceMatrix[6, 5] =  0.0
        stateNoiseCovarianceMatrix[6, 6] =  0.01
        stateNoiseCovarianceMatrix[6, 7] =  0.0
        stateNoiseCovarianceMatrix[6, 8] =  0.0
        stateNoiseCovarianceMatrix[7, 0] =  0.0
        stateNoiseCovarianceMatrix[7, 1] =  0.0
        stateNoiseCovarianceMatrix[7, 2] =  0.0
        stateNoiseCovarianceMatrix[7, 3] =  0.0
        stateNoiseCovarianceMatrix[7, 4] =  0.0
        stateNoiseCovarianceMatrix[7, 5] =  0.0
        stateNoiseCovarianceMatrix[7, 6] =  0.0
        stateNoiseCovarianceMatrix[7, 7] =  0.01
        stateNoiseCovarianceMatrix[7, 8] =  0.0
        stateNoiseCovarianceMatrix[8, 0] =  0.0
        stateNoiseCovarianceMatrix[8, 1] =  0.0
        stateNoiseCovarianceMatrix[8, 2] =  0.0
        stateNoiseCovarianceMatrix[8, 3] =  0.0
        stateNoiseCovarianceMatrix[8, 4] =  0.0
        stateNoiseCovarianceMatrix[8, 5] =  0.0
        stateNoiseCovarianceMatrix[8, 6] =  0.0
        stateNoiseCovarianceMatrix[8, 7] =  0.0
        stateNoiseCovarianceMatrix[8, 8] =  0.01
    }

    private fun setProcessCovarianceMatrix() {
        // Simple Experimental Q
        processNoiseCovarianceMatrix[0, 0] =  0.02
        processNoiseCovarianceMatrix[0, 1] =  0.0
        processNoiseCovarianceMatrix[0, 2] =  0.0
        processNoiseCovarianceMatrix[0, 3] =  0.0
        processNoiseCovarianceMatrix[0, 4] =  0.0
        processNoiseCovarianceMatrix[0, 5] =  0.0
        processNoiseCovarianceMatrix[0, 6] =  0.0
        processNoiseCovarianceMatrix[0, 7] =  0.0
        processNoiseCovarianceMatrix[0, 8] =  0.0
        processNoiseCovarianceMatrix[1, 0] =  0.0
        processNoiseCovarianceMatrix[1, 1] =  0.02
        processNoiseCovarianceMatrix[1, 2] =  0.0
        processNoiseCovarianceMatrix[1, 3] =  0.0
        processNoiseCovarianceMatrix[1, 4] =  0.0
        processNoiseCovarianceMatrix[1, 5] =  0.0
        processNoiseCovarianceMatrix[1, 6] =  0.0
        processNoiseCovarianceMatrix[1, 7] =  0.0
        processNoiseCovarianceMatrix[1, 8] =  0.0
        processNoiseCovarianceMatrix[2, 0] =  0.0
        processNoiseCovarianceMatrix[2, 1] =  0.0
        processNoiseCovarianceMatrix[2, 2] =  0.000001
        processNoiseCovarianceMatrix[2, 3] =  0.0
        processNoiseCovarianceMatrix[2, 4] =  0.0
        processNoiseCovarianceMatrix[2, 5] =  0.0
        processNoiseCovarianceMatrix[2, 6] =  0.0
        processNoiseCovarianceMatrix[2, 7] =  0.0
        processNoiseCovarianceMatrix[2, 8] =  0.0
        processNoiseCovarianceMatrix[3, 0] =  0.0
        processNoiseCovarianceMatrix[3, 1] =  0.0
        processNoiseCovarianceMatrix[3, 2] =  0.0
        processNoiseCovarianceMatrix[3, 3] =  0.04
        processNoiseCovarianceMatrix[3, 4] =  0.0
        processNoiseCovarianceMatrix[3, 5] =  0.0
        processNoiseCovarianceMatrix[3, 6] =  0.0
        processNoiseCovarianceMatrix[3, 7] =  0.0
        processNoiseCovarianceMatrix[3, 8] =  0.0
        processNoiseCovarianceMatrix[4, 0] =  0.0
        processNoiseCovarianceMatrix[4, 1] =  0.0
        processNoiseCovarianceMatrix[4, 2] =  0.0
        processNoiseCovarianceMatrix[4, 3] =  0.0
        processNoiseCovarianceMatrix[4, 4] =  0.04
        processNoiseCovarianceMatrix[4, 5] =  0.0
        processNoiseCovarianceMatrix[4, 6] =  0.0
        processNoiseCovarianceMatrix[4, 7] =  0.0
        processNoiseCovarianceMatrix[4, 8] =  0.0
        processNoiseCovarianceMatrix[5, 0] =  0.0
        processNoiseCovarianceMatrix[5, 1] =  0.0
        processNoiseCovarianceMatrix[5, 2] =  0.0
        processNoiseCovarianceMatrix[5, 3] =  0.0
        processNoiseCovarianceMatrix[5, 4] =  0.0
        processNoiseCovarianceMatrix[5, 5] =  0.000001
        processNoiseCovarianceMatrix[5, 6] =  0.0
        processNoiseCovarianceMatrix[5, 7] =  0.0
        processNoiseCovarianceMatrix[5, 8] =  0.0
        processNoiseCovarianceMatrix[6, 0] =  0.0
        processNoiseCovarianceMatrix[6, 1] =  0.0
        processNoiseCovarianceMatrix[6, 2] =  0.0
        processNoiseCovarianceMatrix[6, 3] =  0.0
        processNoiseCovarianceMatrix[6, 4] =  0.0
        processNoiseCovarianceMatrix[6, 5] =  0.0
        processNoiseCovarianceMatrix[6, 6] =  0.1
        processNoiseCovarianceMatrix[6, 7] =  0.0
        processNoiseCovarianceMatrix[6, 8] =  0.0
        processNoiseCovarianceMatrix[7, 0] =  0.0
        processNoiseCovarianceMatrix[7, 1] =  0.0
        processNoiseCovarianceMatrix[7, 2] =  0.0
        processNoiseCovarianceMatrix[7, 3] =  0.0
        processNoiseCovarianceMatrix[7, 4] =  0.0
        processNoiseCovarianceMatrix[7, 5] =  0.0
        processNoiseCovarianceMatrix[7, 6] =  0.0
        processNoiseCovarianceMatrix[7, 7] =  0.1
        processNoiseCovarianceMatrix[7, 8] =  0.0
        processNoiseCovarianceMatrix[8, 0] =  0.0
        processNoiseCovarianceMatrix[8, 1] =  0.0
        processNoiseCovarianceMatrix[8, 2] =  0.0
        processNoiseCovarianceMatrix[8, 3] =  0.0
        processNoiseCovarianceMatrix[8, 4] =  0.0
        processNoiseCovarianceMatrix[8, 5] =  0.0
        processNoiseCovarianceMatrix[8, 6] =  0.0
        processNoiseCovarianceMatrix[8, 7] =  0.0
        processNoiseCovarianceMatrix[8, 8] =  0.000001
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
        // Simple R
        measurementNoiseCovarianceMatrix[0, 0] =  0.04
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
        measurementNoiseCovarianceMatrix[2, 2] =  10.0
        measurementNoiseCovarianceMatrix[2, 3] =  0.0
        measurementNoiseCovarianceMatrix[2, 4] =  0.0
        measurementNoiseCovarianceMatrix[2, 5] =  0.0
        measurementNoiseCovarianceMatrix[3, 0] =  0.0
        measurementNoiseCovarianceMatrix[3, 1] =  0.0
        measurementNoiseCovarianceMatrix[3, 2] =  0.0
        measurementNoiseCovarianceMatrix[3, 3] =  0.002
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
        measurementNoiseCovarianceMatrix[5, 5] =  MEASUREMENT_NOISE_RELIABLE_Z_ACCELERATION
    }

    private inner class KalmanFilterStrategies {
        val predict: (locationData: LocationData?, accelerationData: AccelerationData?, orientationData: OrientationData?) -> Unit  = { _, accelerationData, _ ->
            // With each prediction, update the process noise covariance matrix with the current overall acceleration to make the filter more adaptive
            val currentProcessNoiseCovarianceMatrix = applyCurrentOverallAccelerationToProcessNoiseCovarianceMatrix(accelerationData!!)

            // x = F * x
            mult(stateTransitionMatrix, stateVector, a)
            stateVector.set(a)

            // P = F * P * F' + Q
            mult(stateTransitionMatrix, stateNoiseCovarianceMatrix, b)
            multTransB(b, stateTransitionMatrix, stateNoiseCovarianceMatrix)
            addEquals(stateNoiseCovarianceMatrix, currentProcessNoiseCovarianceMatrix)
        }

        val update: (uwbLocationData: LocationData, accelerationData: AccelerationData, orientationData: OrientationData) -> Unit = update@ { uwbLocationData, accelerationData, orientationData ->
            // y = z - H * x
            measurementVector = DMatrixRMaj(doubleArrayOf(uwbLocationData.xPos, uwbLocationData.yPos, uwbLocationData.zPos, accelerationData.xAcc, accelerationData.yAcc, accelerationData.zAcc))
            mult(measurementTransitionMatrix, stateVector, innovationVector)
            subtract(measurementVector, innovationVector, innovationVector)

            // S = H * P * H' + R
            adjustMeasurementNoise(orientationData)
            mult(measurementTransitionMatrix, stateNoiseCovarianceMatrix, c)
            multTransB(c, measurementTransitionMatrix, innovationCovarianceMatrix)
            addEquals(innovationCovarianceMatrix, measurementNoiseCovarianceMatrix)

            // K = P * H' * S^(-1)
            if(!solver.setA(innovationCovarianceMatrix)){
                println("RESET")
                // Reset P to avoid diverging Kalman Filter
                setStateCovarianceMatrix()
                return@update
            }
            solver.invert(innovationCovarianceMatrixInverted)
            multTransA(measurementTransitionMatrix, innovationCovarianceMatrixInverted, d)
            mult(stateNoiseCovarianceMatrix, d, kalmanGainMatrix)

            // x = x + K * y
            mult(kalmanGainMatrix, innovationVector, a)
            addEquals(stateVector, a)

            // P = (I - k * H) * P = P - (K * H) * P = P - K * (H * P)
            mult(measurementTransitionMatrix, stateNoiseCovarianceMatrix, c)
            mult(kalmanGainMatrix, c, b)
            subtractEquals(stateNoiseCovarianceMatrix, b)

            kalmanFilterOutputListener.onNewEstimate(uwbLocationData, LocationData(stateVector[0, 0], stateVector[1, 0], stateVector[2, 0]))
        }

        val notConfigured: (p0: Any?, p1: Any?, p2: Any?) -> Unit = {_, _, _ -> throw IllegalAccessError("You need to call KalmanFilterImpl().configure() before making use of it.")}

        private fun applyCurrentOverallAccelerationToProcessNoiseCovarianceMatrix(accelerationData: AccelerationData): DMatrixRMaj {
            val size = processNoiseCovarianceMatrix.numElements
            val data = DoubleArray(size)
            System.arraycopy(processNoiseCovarianceMatrix.data, 0, data, 0, size)
            val q = DMatrixRMaj.wrap(processNoiseCovarianceMatrix.numRows, processNoiseCovarianceMatrix.numCols, data)
            val overallAcceleration = calculateOverallAcceleration(accelerationData)
            for (i in q.data.indices){
                q[i] *= overallAcceleration.pow(2)
            }
            return q
        }

        private fun calculateOverallAcceleration(accelerationData: AccelerationData): Double {
            return sqrt(accelerationData.xAcc.pow(2) + accelerationData.yAcc.pow(2) + accelerationData.zAcc.pow(2))
        }

        // Because the accelerometer of this projects' phone is noisy when lying on its back,
        // we have to tell the filter whether the z acceleration is reliable or not.
        private fun adjustMeasurementNoise(orientationData: OrientationData) {
            if (isZAccelerationReliable(orientationData.roll)) {
                measurementNoiseCovarianceMatrix[5, 5] = MEASUREMENT_NOISE_RELIABLE_Z_ACCELERATION
            }
            else {
                measurementNoiseCovarianceMatrix[5, 5] = MEASUREMENT_NOISE_UNRELIABLE_Z_ACCELERATION
            }
        }

        private fun isZAccelerationReliable(roll: Double): Boolean {
            return if (kotlin.math.abs(roll) > ORIENTATION_ROLL_THRESHOLD){
                println("RELIABLE")
                // Reliable z acceleration
                true
            }
            else {
                println("UNRELIABLE")
                // Unreliable z acceleration
                false
            }
        }
    }
}