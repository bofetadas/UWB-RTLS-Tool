package bachelor.test.uwbrtlstool.presenter.recording

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator

class VibratorFeedback(private val context: Context) {

    private val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    fun vibrateOnRecordStart(){
        vibrator.vibrate(VibrationEffect.createOneShot(100L, 255))
    }

    fun vibrateOnRecordStop(){
        vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(100L, 100L, 100L, 100L), -1))
    }
}