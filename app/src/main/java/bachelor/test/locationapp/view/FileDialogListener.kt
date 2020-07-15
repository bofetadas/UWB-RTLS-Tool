package bachelor.test.locationapp.view

import bachelor.test.locationapp.presenter.recording.RecordingModes

interface FileDialogListener {

    fun onFileDataEntered(mode: RecordingModes, x: String, y: String, z: String, direction: String, timePeriod: Long)
}