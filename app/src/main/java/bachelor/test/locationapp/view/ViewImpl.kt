package bachelor.test.locationapp.view

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import bachelor.test.locationapp.R
import bachelor.test.locationapp.presenter.PresenterImpl
import kotlinx.android.synthetic.main.activity_main.*

class ViewImpl : AppCompatActivity(), MainScreenContract.View {

    private lateinit var presenter: MainScreenContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 1)
        presenter = PresenterImpl(applicationContext, this)
        presenter.start()

        setOnClickListeners()
    }

    override fun showPosition(locationData: LocationData) {
        x_position.text     =   "X: " + locationData.xPos
        y_position.text     =   "Y: " + locationData.yPos
        z_position.text     =   "Z: " + locationData.zPos
        quality_factor.text =   "Quality Factor: " + locationData.qualityFactor
    }

    override fun showMessage(message: String?) {
        this.runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun swapButton() {
        if (start_button.visibility == View.VISIBLE)
        {
            start_button.visibility = View.GONE
            stop_button.visibility = View.VISIBLE
        }
        else
        {
            stop_button.visibility = View.GONE
            start_button.visibility = View.VISIBLE
        }
    }

    override fun setPresenter(presenter: MainScreenContract.Presenter) {
        this.presenter = presenter
    }

    private fun setOnClickListeners() {
        start_button.setOnClickListener {
            presenter.onStartClicked()
        }

        stop_button.setOnClickListener {
            presenter.onStopClicked()
        }
    }
}
