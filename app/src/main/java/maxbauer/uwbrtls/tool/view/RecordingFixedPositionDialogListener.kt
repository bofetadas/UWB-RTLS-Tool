package maxbauer.uwbrtls.tool.view

interface RecordingFixedPositionDialogListener {

    fun onFileDataEntered(x: String, y: String, z: String, direction: String, timePeriod: Long)
}