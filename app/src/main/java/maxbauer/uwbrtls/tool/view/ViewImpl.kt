package maxbauer.uwbrtls.tool.view

import android.Manifest
import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.view.*
import maxbauer.uwbrtls.tool.BuildConfig
import maxbauer.uwbrtls.tool.R
import maxbauer.uwbrtls.tool.presenter.PresenterImpl
import maxbauer.uwbrtls.tool.presenter.positioning.AccelerationData
import maxbauer.uwbrtls.tool.presenter.positioning.LocationData
import maxbauer.uwbrtls.tool.presenter.positioning.OrientationData
import maxbauer.uwbrtls.tool.presenter.recording.InputData
import maxbauer.uwbrtls.tool.utils.StringUtil

class ViewImpl : AppCompatActivity(), MainScreenContract.View,
    RecordingFixedPositionDialogListener {

    private lateinit var presenter: MainScreenContract.Presenter

    private var recordingDetailsData: InputData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.view)
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 1)
        presenter = PresenterImpl(applicationContext, this)

        setOnClickListeners()
    }

    override fun onStart() {
        super.onStart()
        presenter.start()
    }

    override fun onPause() {
        super.onPause()
        presenter.stop()
        enableConnectButton(true)
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.stop()
        enableConnectButton(true)
    }

    override fun showRecordingOptionsDialog() {
        AlertDialog.Builder(this)
            .setTitle("Do you want to record position data?")
            .setMessage("Data will be saved to the following device directory:\n\n" +
                    "/Android/data/${BuildConfig.APPLICATION_ID}/files/Documents/")
            .setPositiveButton("YES"){_, _ ->
                showRecordingDetailsDialog()
            }
            .setNegativeButton("NO"){_, _ ->
                presenter.onRegularDataTransferStart()
            }
            .create()
            .show()
    }

    override fun showRecordingDetailsDialog() {
        AlertDialog.Builder(this)
            .setTitle("What kind of recording would you like to start?")
            .setMessage("Do you want to record data at a fixed position or during a movement?")
            .setPositiveButton("Movement"){_, _ ->
                // By setting 'recordingDetailsData' to null, we signalize the presenter that a recording of a movement is about to start
                // since for a movement no more necessary information is needed.
                recordingDetailsData = null
                prepareViewForRecording()
            }
            .setNegativeButton("Fixed Position"){_, _ ->
                val recordingFixedPositionDialog = RecordingFixedPositionDialog()
                recordingFixedPositionDialog.show(supportFragmentManager, "View")
            }
            .create()
            .show()
    }

    override fun showRecordStopScreen() {
        record_start_button.visibility = View.GONE
        record_stop_button.visibility = View.VISIBLE
    }

    override fun dismissRecordStopScreen() {
        record_stop_button.visibility = View.GONE
        start_button.visibility = View.VISIBLE
        disconnect_button.visibility = View.VISIBLE
    }

    override fun onFileDataEntered(x: String, y: String, z: String, direction: String, timePeriod: Long) {
        /*start_button.visibility = View.GONE
        disconnect_button.visibility = View.GONE
        record_start_button.visibility = View.VISIBLE*/
        // By setting 'recordingDetailsData' to actual recording details data, we signalize the presenter that a recording of a fixed position is about to start.
        recordingDetailsData = InputData(x, y, z, direction, timePeriod)
        prepareViewForRecording()
    }

    override fun showUWBPosition(uwbLocationData: LocationData) {
        this.runOnUiThread {
            uwb_x_position.text = "UWB X: ${StringUtil.inEuropeanNotation(uwbLocationData.xPos)} m"
            uwb_y_position.text = "UWB Y: ${StringUtil.inEuropeanNotation(uwbLocationData.yPos)} m"
            uwb_z_position.text = "UWB Z: ${StringUtil.inEuropeanNotation(uwbLocationData.zPos)} m"
        }
    }

    override fun showFilteredPosition(filteredLocationData: LocationData) {
        this.runOnUiThread {
            filtered_x_position.text = "Filter X: ${StringUtil.inEuropeanNotation(filteredLocationData.xPos)} m"
            filtered_y_position.text = "Filter Y: ${StringUtil.inEuropeanNotation(filteredLocationData.yPos)} m"
            filtered_z_position.text = "Filter Z: ${StringUtil.inEuropeanNotation(filteredLocationData.zPos)} m"
        }
    }

    override fun showAcceleration(accData: AccelerationData) {
        this.runOnUiThread {
            x_acc.text = "X Acc: ${StringUtil.inEuropeanNotation(accData.xAcc)}"
            y_acc.text = "Y Acc: ${StringUtil.inEuropeanNotation(accData.yAcc)}"
            z_acc.text = "Z Acc: ${StringUtil.inEuropeanNotation(accData.zAcc)}"
            if (accData.xAcc > 0) x_acc.setTextColor(Color.GREEN) else if (accData.xAcc < 0) x_acc.setTextColor(Color.RED) else x_acc.setTextColor(Color.GRAY)
            if (accData.yAcc > 0) y_acc.setTextColor(Color.GREEN) else if (accData.yAcc < 0) y_acc.setTextColor(Color.RED) else y_acc.setTextColor(Color.GRAY)
            if (accData.zAcc > 0) z_acc.setTextColor(Color.GREEN) else if (accData.zAcc < 0) z_acc.setTextColor(Color.RED) else z_acc.setTextColor(Color.GRAY)
        }
    }

    override fun showOrientation(orientationData: OrientationData) {
        this.runOnUiThread {
            yaw.text = "Yaw: ${StringUtil.inEuropeanNotation(orientationData.yaw)}"
            pitch.text = "Pitch: ${StringUtil.inEuropeanNotation(orientationData.pitch)}"
            roll.text = "Roll: ${StringUtil.inEuropeanNotation(orientationData.roll)}"
        }
    }

    override fun showCompassDirection(direction: String) {
        this.runOnUiThread {
            compass_direction.text = "Direction: $direction"
        }
    }

    override fun enableConnectButton(enabled: Boolean) {
        this.runOnUiThread {
            if (enabled) {
                connect_button.visibility = View.VISIBLE
                start_button.visibility = View.GONE
                disconnect_button.visibility = View.GONE
                stop_button.visibility = View.GONE
            } else {
                connect_button.visibility = View.GONE
            }
        }
    }

    override fun swapStartButton(start: Boolean) {
        this.runOnUiThread {
            if (start) {
                stop_button.visibility = View.GONE
                start_button.visibility = View.VISIBLE
                disconnect_button.visibility = View.VISIBLE
            } else {
                start_button.visibility = View.GONE
                disconnect_button.visibility = View.GONE
                stop_button.visibility = View.VISIBLE
            }
        }
    }

    override fun showMessage(message: String?) {
        this.runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun setPresenter(presenter: MainScreenContract.Presenter) {
        this.presenter = presenter
    }

    private fun prepareViewForRecording(){
        start_button.visibility = View.GONE
        disconnect_button.visibility = View.GONE
        record_start_button.visibility = View.VISIBLE
    }

    private fun setOnClickListeners() {
        connect_button.setOnClickListener {
            presenter.onConnectClicked()
        }

        start_button.setOnClickListener {
            presenter.onStartClicked()
        }

        stop_button.setOnClickListener {
            presenter.onStopClicked()
        }

        disconnect_button.setOnClickListener {
            presenter.onDisconnectClicked()
        }

        record_start_button.setOnClickListener {
            presenter.onRecordingDataTransferStart(recordingDetailsData)
        }

        record_stop_button.setOnClickListener {
            presenter.onRecordStopClicked()
        }
    }
}