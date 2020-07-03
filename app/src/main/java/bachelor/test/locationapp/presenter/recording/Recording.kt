package bachelor.test.locationapp.presenter.recording

interface Recording {
    fun createFile(xInput: String, yInput: String, zInput:String, direction: String): Boolean
    fun writeToFile(line: String)
    fun startTimer(timePeriod: Long)
    fun stopTimer()
    fun vibrateOnRecordStart()
    fun vibrateOnRecordStop()
}