package bachelor.test.uwbrtlstool.presenter.recording

interface Recording {
    fun createRecordingMovementFile(): Boolean
    fun createRecordingFixedPositionFile(xInput: String, yInput: String, zInput:String, direction: String): Boolean
    fun writeToFile(line: String)
    fun startTimer(timePeriod: Long?)
    fun stopTimer()
    fun vibrateOnRecordStart()
    fun vibrateOnRecordStop()
}