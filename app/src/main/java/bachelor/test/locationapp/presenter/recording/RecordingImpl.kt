package bachelor.test.locationapp.presenter.recording

import android.content.Context
import bachelor.test.locationapp.view.MainScreenContract

// Entry class for handling recording logic
class RecordingImpl(context: Context, private val presenter: MainScreenContract.Presenter): Recording, TimerCallbacks {
    private val fileWriter = FileWriter(context)
    private val vibratorFeedback = VibratorFeedback(context)
    private val timer = Timer(this)

    override fun createFile(
        xInput: String,
        yInput: String,
        zInput: String,
        direction: String
    ): Boolean {
        return fileWriter.createFile(xInput, yInput, zInput, direction)
    }

    override fun writeToFile(line: String) {
        fileWriter.writeToFile(line)
    }

    override fun startTimer(timePeriod: Long) {
        timer.startTimer(timePeriod)
    }

    override fun stopTimer() {
        timer.stopTimer()
    }

    override fun onTimerDone() {
        vibratorFeedback.vibrateOnRecordStop()
        presenter.onTimerDone()
    }

    override fun vibrateOnRecordStart() {
        vibratorFeedback.vibrateOnRecordStart()
    }

    override fun vibrateOnRecordStop() {
        vibratorFeedback.vibrateOnRecordStop()
    }
}