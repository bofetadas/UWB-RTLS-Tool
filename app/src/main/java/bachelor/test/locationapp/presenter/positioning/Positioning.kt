package bachelor.test.locationapp.presenter.positioning

import bachelor.test.locationapp.presenter.recording.RecordingModes

interface Positioning {
    fun startIMU()
    fun stopIMU()
    fun getLocalizationData(mode: RecordingModes, byteArray: ByteArray)
}