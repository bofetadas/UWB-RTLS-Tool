package bachelor.test.locationapp.presenter.recording

enum class RecordingModes {
    P,  // Positions - regular mode with merged uwb and imu data
    D   // Displacements - debug mode for comparing uwb with imu displacements
}