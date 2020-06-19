package bachelor.test.locationapp.presenter

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import bachelor.test.locationapp.view.MainScreenContract
import kotlin.math.sqrt

class AccelerometerReader(context: Context, val presenter: MainScreenContract.Presenter):
    SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val gravity = FloatArray(3)
    private val linearAcc = FloatArray(3)

    fun registerListener(){
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL)
    }

    fun unregisterListener(){
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        //println("X ACC: ${event!!.values[0]}")
        //println("Y ACC: ${event!!.values[1]}")
        //println("Z ACC: ${event.values[2]}")

        // alpha is calculated as t / (t + dT)
        // with t, the low-pass filter's time-constant
        // and dT, the event delivery rate
        val alpha = 0.8f

        gravity[0] = alpha * gravity[0] + (1 - alpha) * event!!.values[0]
        gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1]
        gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2]

        linearAcc[0] = event.values[0] - gravity[0]
        linearAcc[1] = event.values[1] - gravity[1]
        linearAcc[2] = event.values[2] - gravity[2]

        val scalarProduct: Float = gravity[0] * linearAcc[0] + gravity[1] * linearAcc[1] + gravity[2] * linearAcc[2]
        val gravityVectorLength = sqrt(gravity[0] * gravity[0] + gravity[1] * gravity[1] + gravity[2] * gravity[2])
        val linearAccVectorLength = sqrt(linearAcc[0] * linearAcc[0] + linearAcc[1] * linearAcc[1] + linearAcc[2] * linearAcc[2])

        val cosVectorAngle = scalarProduct / (gravityVectorLength * linearAccVectorLength)

        //println("ACC: $lianearAccVectorLength")
        //println("ANGLE: $cosVectorAngle")
        if (linearAccVectorLength > 2) { //increase to detect only bigger accelerations, decrease to make detection more sensitive but noisy - original value: 2
            if (cosVectorAngle > 0.5) {
                println("Down")
            } else if (cosVectorAngle < -0.5) {
                println("Up")
            }
        }

        val accData = AccelerometerData(event.values[0], event.values[1], event.values[2], linearAccVectorLength)
        presenter.onAccelerometerUpdate(accData)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int){}
}