package bachelor.test.locationapp.presenter.recording

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class Timer(private val timerCallbacks: TimerCallbacks) {

    private var timerJob: Job? = null

    fun startTimer(timePeriod: Long){
        // Check for a negative integer - negative integers mean the recording has to be stopped manually
        if (timePeriod > 0) {
            timerJob = CoroutineScope(Main).launch {
                delay(timePeriod * 1000)
                stopTimer()
            }
        }
    }

    fun stopTimer(){
        timerJob?.cancel()
        timerCallbacks.onTimerDone()
    }
}