package bachelor.test.locationapp.positioning

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

/*  COORDINATES FOR EVERY ANCHOR IN MM   */

// Z coordinate for every anchor
private const val Z = 2.35f

// Initiator aka DWCDA5
private const val INIT_X = 0.02f
private const val INIT_Y = 0.3f
private const val INIT_Z = Z

// Anchor 1 aka DW5512
private const val AN1_X = 0.02f
private const val AN1_Y = 3.41f
private const val AN1_Z = Z

// Anchor 2 aka DW12A2
private const val AN2_X = 3.75f
private const val AN2_Y = 3.41f
private const val AN2_Z = Z

// Anchor 3 aka DW1AA0
private const val AN3_X = 3.75f
private const val AN3_Y = 0.1f
private const val AN3_Z = Z

class GaussNewtonMethod {

    private val maxIteration = 100
    private val precision = 0.5
    private val rows = 4
    private val cols = 3

    private val anchorCoordinates = Array(4){Array(3){0f}}
    private val guess = Array(3){0f}
    private val lastSuccessfulGuess = Array(3){0f}

    private val residualsVector = Array(4){Array(1){0f}}
    private val jacobiMatrix = Array(4){Array(3) {0f} }
    private val jacobiMatrixTransposed = Array(3){Array(4) {0f} }
    private val JTWJ = Array(3){Array(4) {0f} }
    private val JTWr = Array(3){Array(1){0f}}

    init {
        // Init anchorCoordinates with constants
        anchorCoordinates[0][0] = INIT_X
        anchorCoordinates[0][1] = INIT_Y
        anchorCoordinates[0][2] = INIT_Z
        anchorCoordinates[1][0] = AN1_X
        anchorCoordinates[1][1] = AN1_Y
        anchorCoordinates[1][2] = AN1_Z
        anchorCoordinates[2][0] = AN2_X
        anchorCoordinates[2][1] = AN2_Y
        anchorCoordinates[2][2] = AN2_Z
        anchorCoordinates[3][0] = AN3_X
        anchorCoordinates[3][1] = AN3_Y
        anchorCoordinates[3][2] = AN3_Z

        // Populate guess array with initial guess X, Y, Z (center of my room)
        guess[0] = 1f
        guess[1] = 1f
        guess[2] = 1f
    }

    //@Synchronized
    fun solve(distanceData: DistanceData): LocationData {
        val measurements = transformDistanceDataToArray(distanceData)
        val w = Array(4){0f}
        w[0] = (1/measurements[0].toDouble().pow(2)).toFloat()
        w[1] = (1/measurements[1].toDouble().pow(2)).toFloat()
        w[2] = (1/measurements[2].toDouble().pow(2)).toFloat()
        w[3] = (1/measurements[3].toDouble().pow(2)).toFloat()

        for(i in 0 until maxIteration){
            calculateResiduals(measurements)
            val errorDistance = calculateErrorChi(w)
            if (abs(errorDistance) < precision){
                lastSuccessfulGuess[0] = guess[0]
                lastSuccessfulGuess[1] = guess[1]
                lastSuccessfulGuess[2] = guess[2]
                return LocationData(
                    guess[0],
                    guess[1],
                    guess[2],
                    maxIteration - i
                )
            }
            jacob()
            transpose()
            multiplyMatrices(jacobiMatrixTransposed, jacobiMatrix, JTWJ, w)
            multiplyMatrices(jacobiMatrixTransposed, residualsVector, JTWr, w)
            JTWJ[0][3] = JTWr[0][0]
            JTWJ[1][3] = JTWr[1][0]
            JTWJ[2][3] = JTWr[2][0]

            //gauss()

            // X Pos
            guess[0] = guess[0] - JTWJ[0][3] / JTWJ[0][0]
            if (guess[0].isNaN()) {
                guess[0] = 1f
            }
            // Y Pos
            guess[1] = guess[1] - JTWJ[1][3] / JTWJ[1][1]
            if (guess[1].isNaN()) {
                guess[1] = 1f
            }
            // Z Pos
            guess[2] = guess[2] - JTWJ[2][3] / JTWJ[2][2]
            if (guess[2].isNaN()) {
                guess[2] = 1f
            }
        }
        resetMatrices()
        return LocationData(
            lastSuccessfulGuess[0],
            lastSuccessfulGuess[1],
            lastSuccessfulGuess[2],
            -1
        )
    }

    private fun calculateResiduals(measurements: Array<Float>){
        for (i in 0 until rows){
            val deltaX = guess[0] - anchorCoordinates[i][0]
            val deltaY = guess[1] - anchorCoordinates[i][1]
            val deltaZ = guess[2] - anchorCoordinates[i][2]
            residualsVector[i][0] = (deltaX*deltaX)+(deltaY*deltaY)+(deltaZ*deltaZ)-(measurements[i]*measurements[i])
        }
    }

    private fun calculateErrorChi(w: Array<Float>): Double{
        var sum = 0.0
        for (i in 0 until rows){
            sum += residualsVector[i][0] * residualsVector[i][0] * w[i]
        }
        return sqrt(sum)
    }

    private fun jacob(){
        for (i in 0 until rows){
            jacobiMatrix[i][0] = 2 * guess[0] - 2 * anchorCoordinates[i][0]
            jacobiMatrix[i][1] = 2 * guess[1] - 2 * anchorCoordinates[i][1]
            jacobiMatrix[i][2] = 2 * guess[2] - 2 * anchorCoordinates[i][2]
        }
    }

    private fun transpose(){
        for (i in 0 until rows){
            for (j in 0 until cols){
                jacobiMatrixTransposed[j][i] = jacobiMatrix[i][j]
            }
        }
    }

    private fun multiplyMatrices(matrix1: Array<Array<Float>>, matrix2: Array<Array<Float>>, resultMatrix: Array<Array<Float>>, w: Array<Float>){
        // For in matrix 1 rows
        for (i in 0 until matrix1.indices.count()){
            // For j in matrix 2 cols
            for (j in 0 until matrix2[0].indices.count()){
                var sum = 0.0f
                // For k in matrix 1 cols
                for (k in 0 until matrix1[0].indices.count())
                    sum += matrix1[i][k] * matrix2[k][j] * w[k]
                resultMatrix[i][j] = sum
            }
        }
    }

    private fun gauss(){
        var x = -JTWJ[0][0]
        var y = JTWJ[1][0]
        JTWJ[1][0] = JTWJ[1][0] / y * x + JTWJ[0][0]
        JTWJ[1][1] = JTWJ[1][1] / y * x + JTWJ[0][1]
        JTWJ[1][2] = JTWJ[1][2] / y * x + JTWJ[0][2]
        JTWJ[1][3] = JTWJ[1][3] / y * x + JTWJ[0][3]
        x = -JTWJ[0][0]
        y = JTWJ[2][0]
        JTWJ[2][0] = JTWJ[2][0] / (y * x + JTWJ[0][0])
        JTWJ[2][1] = JTWJ[2][1] / (y * x + JTWJ[0][1])
        JTWJ[2][2] = JTWJ[2][2] / (y * x + JTWJ[0][2])
        JTWJ[2][3] = JTWJ[2][3] / y * x + JTWJ[0][3]
        x = -JTWJ[1][1]
        y = JTWJ[0][1]
        JTWJ[0][0] = JTWJ[0][0] / y * x + JTWJ[1][0]
        JTWJ[0][1] = JTWJ[0][1] / y * x + JTWJ[1][1]
        JTWJ[0][2] = JTWJ[0][2] / y * x + JTWJ[1][2]
        JTWJ[0][3] = JTWJ[0][3] / y * x + JTWJ[1][3]
        x = -JTWJ[1][1]
        y = JTWJ[2][1]
        JTWJ[2][0] = JTWJ[2][0] / y * x + JTWJ[1][0]
        JTWJ[2][1] = JTWJ[2][1] / y * x + JTWJ[1][1]
        JTWJ[2][2] = JTWJ[2][2] / y * x + JTWJ[1][2]
        JTWJ[2][3] = JTWJ[2][3] / y * x + JTWJ[1][3]
        x = -JTWJ[2][2]
        y = JTWJ[0][2]
        JTWJ[0][0] = JTWJ[0][0] / y * x + JTWJ[2][0]
        JTWJ[0][1] = JTWJ[0][1] / y * x + JTWJ[2][1]
        JTWJ[0][2] = JTWJ[0][2] / y * x + JTWJ[2][2]
        JTWJ[0][3] = JTWJ[0][3] / y * x + JTWJ[2][3]
        x = -JTWJ[2][2]
        y = JTWJ[1][2]
        JTWJ[1][0] = JTWJ[1][0] / y * x + JTWJ[2][0]
        JTWJ[1][1] = JTWJ[1][1] / y * x + JTWJ[2][1]
        JTWJ[1][2] = JTWJ[1][2] / y * x + JTWJ[2][2]
        JTWJ[1][3] = JTWJ[1][3] / y * x + JTWJ[2][3]
    }

    private fun resetMatrices() {
        for (i in guess.indices){
            guess[i] = 1f
        }
        for (i in 0 until residualsVector.indices.count()){
            for (j in 0 until residualsVector[0].indices.count()){
                residualsVector[i][j] = 0f
            }
        }
        for (i in 0 until jacobiMatrix.indices.count()){
            for (j in 0 until jacobiMatrix[0].indices.count()){
                jacobiMatrix[i][j] = 0f
            }
        }
        for (i in 0 until jacobiMatrixTransposed.indices.count()){
            for (j in 0 until jacobiMatrixTransposed[0].indices.count()){
                jacobiMatrixTransposed[i][j] = 0f
            }
        }
        for (i in 0 until JTWJ.indices.count()){
            for (j in 0 until JTWJ[0].indices.count()){
                JTWJ[i][j] = 0f
            }
        }
        for (i in 0 until JTWr.indices.count()){
            for (j in 0 until JTWr[0].indices.count()){
                JTWr[i][j] = 0f
            }
        }
    }

    private fun transformDistanceDataToArray(distanceData: DistanceData): Array<Float>{
        val measurements = Array(4){0f}
        measurements[0] = distanceData.first.distance
        measurements[1] = distanceData.second.distance
        measurements[2] = distanceData.third.distance
        measurements[3] = distanceData.fourth.distance

        /*measurements[0] = 2.92f
        measurements[1] = 1.66f
        measurements[2] = 3.35f
        measurements[3] = 4.26f*/
        return measurements
    }
}