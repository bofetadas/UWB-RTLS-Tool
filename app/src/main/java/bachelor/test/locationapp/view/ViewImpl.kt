package bachelor.test.locationapp.view

import android.Manifest
import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import bachelor.test.locationapp.BuildConfig
import bachelor.test.locationapp.R
import bachelor.test.locationapp.presenter.PresenterImpl
import bachelor.test.locationapp.presenter.positioning.AccelerationData
import bachelor.test.locationapp.presenter.positioning.LocationData
import bachelor.test.locationapp.presenter.recording.InputData
import kotlinx.android.synthetic.main.view.*

class ViewImpl : AppCompatActivity(), MainScreenContract.View, FileDialogListener {

    private lateinit var presenter: MainScreenContract.Presenter

    private lateinit var inputData: InputData

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

    override fun showRecordingDialog() {
        AlertDialog.Builder(this)
            .setTitle("Do you want to save the received Location Data?")
            .setMessage("Data will be saved to /Android/data/${BuildConfig.APPLICATION_ID}/files/Documents directory on device")
            .setPositiveButton("YES"){_, _ ->
                val filenameDialog = FileDialog()
                filenameDialog.show(supportFragmentManager, "View")
            }
            .setNegativeButton("NO"){_, _ ->
                presenter.onRegularDataTransferStart()
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
        start_button.visibility = View.GONE
        disconnect_button.visibility = View.GONE
        record_start_button.visibility = View.VISIBLE

        inputData = InputData(x, y, z, direction, timePeriod)
    }

    override fun showPosition(locationData: LocationData) {
        this.runOnUiThread {
            x_position.text = "X: ${locationData.xPos} m"
            y_position.text = "Y: ${locationData.yPos} m"
            z_position.text = "Z: ${locationData.zPos} m"
            quality_factor.text = "Quality Factor: ${locationData.qualityFactor}"
        }
    }

    override fun showAcceleration(accData: AccelerationData) {
        this.runOnUiThread {
            x_acc.text = "X Acc: ${accData.xAcc}"
            y_acc.text = "Y Acc: ${accData.yAcc}"
            z_acc.text = "Z Acc: ${accData.zAcc}"
            if (accData.xAcc > 0) x_acc.setTextColor(Color.GREEN) else if (accData.xAcc < 0) x_acc.setTextColor(Color.RED) else x_acc.setTextColor(Color.GRAY)
            if (accData.yAcc > 0) y_acc.setTextColor(Color.GREEN) else if (accData.yAcc < 0) y_acc.setTextColor(Color.RED) else y_acc.setTextColor(Color.GRAY)
            if (accData.zAcc > 0) z_acc.setTextColor(Color.GREEN) else if (accData.zAcc < 0) z_acc.setTextColor(Color.RED) else z_acc.setTextColor(Color.GRAY)
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
            presenter.onRecordingDataTransferStart(inputData)
        }

        record_stop_button.setOnClickListener {
            presenter.onRecordStopClicked()
        }
    }
}