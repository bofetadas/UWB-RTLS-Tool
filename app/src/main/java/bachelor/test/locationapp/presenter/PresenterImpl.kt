package bachelor.test.locationapp.presenter

import android.content.Context
import bachelor.test.locationapp.model.Model
import bachelor.test.locationapp.model.ModelImpl
import bachelor.test.locationapp.view.LocationData
import bachelor.test.locationapp.view.MainScreenContract
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val POSITION_LOCATION_BYTE_ARRAY_SIZE = 14
private const val FPS60 = 17L
private const val FPS30 = 34L
private const val FPS2 = 500L
class PresenterImpl(private val context: Context, private val view: MainScreenContract.View): MainScreenContract.Presenter {

    private lateinit var model: Model
    private lateinit var job: Job

    override fun start() {
        model = ModelImpl(context)
        model.initializeBluetooth()
    }

    override fun onStartClicked() {
        job = CoroutineScope(IO).launch { getLocation() }
        view.swapButton()
    }

    override fun onStopClicked() {
        job.cancel()
        view.swapButton()
    }

    private suspend fun getLocation(){
        try {
            val locationByteArray = model.getLocation()
            if (locationByteArray.size == POSITION_LOCATION_BYTE_ARRAY_SIZE){
                val location = getLocationFromByteArray(locationByteArray)
                view.showPosition(location)
            }
        } catch (e: Exception){
            view.showMessage(e.message)
        }
        delay(FPS2)
        getLocation()
    }

    private fun getLocationFromByteArray(locationByteArray: ByteArray): LocationData{

        val xByte = ("${locationByteArray[4]} ${locationByteArray[3]} ${locationByteArray[2]} ${locationByteArray[1]}")
        val yByte = ("${locationByteArray[8]} ${locationByteArray[7]} ${locationByteArray[6]} ${locationByteArray[5]}")
        val zByte = ("${locationByteArray[12]} ${locationByteArray[11]} ${locationByteArray[10]} ${locationByteArray[9]}")
        val qualityFactor = ("${locationByteArray[13]}")
        return LocationData(xByte, yByte, zByte, qualityFactor)
    }
}