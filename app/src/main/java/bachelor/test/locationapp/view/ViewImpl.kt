package bachelor.test.locationapp.view

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import bachelor.test.locationapp.R
import bachelor.test.locationapp.presenter.LocationData
import bachelor.test.locationapp.presenter.PresenterImpl
import kotlinx.android.synthetic.main.view.*

class ViewImpl : AppCompatActivity(), MainScreenContract.View {

    private lateinit var presenter: MainScreenContract.Presenter

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

    override fun showPosition(locationData: LocationData) {
        this.runOnUiThread {
            x_position.text = "X: " + locationData.xPos + " m"
            y_position.text = "Y: " + locationData.yPos + " m"
            z_position.text = "Z: " + locationData.zPos + " m"
            quality_factor.text = "Quality Factor: " + locationData.qualityFactor
        }
    }

    override fun enableConnectButton(enabled: Boolean) {
        this.runOnUiThread {
            if (enabled) {
                connect_button.visibility = View.VISIBLE
                start_button.visibility = View.GONE
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
            } else {
                start_button.visibility = View.GONE
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
    }
}