package bachelor.test.locationapp.presenter

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BluetoothBroadcastReceiver(private val presenter: Observer): BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == BluetoothAdapter.ACTION_STATE_CHANGED){
            val extra = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 10)
            if (extra == BluetoothAdapter.STATE_ON){
                context?.unregisterReceiver(this)
                presenter.onBluetoothEnabled()
            }
        }
    }
}