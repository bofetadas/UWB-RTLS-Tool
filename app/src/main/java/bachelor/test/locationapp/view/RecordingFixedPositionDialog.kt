package bachelor.test.locationapp.view

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import bachelor.test.locationapp.R
import bachelor.test.locationapp.presenter.recording.Directions

class RecordingFixedPositionDialog : DialogFragment(){

    private var recordingFixedPositionDialogListener : RecordingFixedPositionDialogListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        val inflater = activity!!.layoutInflater

        val view = inflater.inflate(R.layout.recording_fixed_position_dialog, null)
        val xEditText = view.findViewById<EditText>(R.id.x_value_edittext)
        val yEdiText = view.findViewById<EditText>(R.id.y_value_edittext)
        val zEditText = view.findViewById<EditText>(R.id.z_value_edittext)
        val directionSpinner = view.findViewById<Spinner>(R.id.directions_spinner)
        val timePeriodEditText = view.findViewById<EditText>(R.id.time_period_edittext)
        val directions = arrayOf(Directions.N, Directions.E, Directions.S, Directions.W)
        val spinnerAdapter = ArrayAdapter(activity!!.applicationContext, android.R.layout.simple_spinner_dropdown_item, directions)
        directionSpinner.adapter = spinnerAdapter

        builder.setView(view)
            .setTitle("Fixed position recording")
            .setMessage("Please provide:\n\n" +
                    "1) Coordinates of the position you would like to record data at\n\n" +
                    "2) Direction you will be looking into\n\n" +
                    "3) Recording Time Period\n" +
                    "(Enter negative number for manual stop of recording)")
            .setNegativeButton("CANCEL"){_, _ ->}
            .setPositiveButton("RECORD"){_, _ ->
                val xInput = xEditText.text.toString().replace('.', ',')
                val yInput = yEdiText.text.toString().replace('.', ',')
                val zInput = zEditText.text.toString().replace('.', ',')
                val directionInput = directionSpinner.selectedItem.toString()
                val timePeriodInput = timePeriodEditText.text.toString().toLong()
                if (isValid(xInput, yInput, zInput, directionInput, timePeriodInput)) {
                    recordingFixedPositionDialogListener?.onFileDataEntered(xInput, yInput, zInput, directionInput, timePeriodInput)
                }
                else{
                    Toast.makeText(context, "Please specify valid coordinates, direction and time period. Recording not started", Toast.LENGTH_LONG).show()
                }
            }
        return builder.create()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        try{
            recordingFixedPositionDialogListener = context as RecordingFixedPositionDialogListener
        } catch (c: ClassCastException){
            throw ClassCastException("${context.toString()} must implement RecordingFixedPositionDialogListener.")
        }
    }

    private fun isValid(x: String, y: String, z: String, direction: String, timePeriod: Long): Boolean{
        return  x.isNotBlank()
                && y.isNotBlank()
                && z.isNotBlank()
                && direction.isNotBlank()
                && timePeriod != 0L
    }
}