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

class FileDialog : DialogFragment(){

    private var fileDialogListener : FileDialogListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        val inflater = activity!!.layoutInflater

        val view = inflater.inflate(R.layout.file_dialog, null)
        val xEditText = view.findViewById<EditText>(R.id.x_value_edittext)
        val yEdiText = view.findViewById<EditText>(R.id.y_value_edittext)
        val zEditText = view.findViewById<EditText>(R.id.z_value_edittext)
        val directionSpinner = view.findViewById<Spinner>(R.id.directions_spinner)
        val timePeriodEditText = view.findViewById<EditText>(R.id.time_period_edittext)
        val directions = arrayOf(Directions.N, Directions.E, Directions.S, Directions.W)
        val spinnerAdapter = ArrayAdapter(activity!!.applicationContext, android.R.layout.simple_spinner_dropdown_item, directions)
        directionSpinner.adapter = spinnerAdapter

        builder.setView(view)
            .setTitle("Enter recording details")
            .setNegativeButton("CANCEL"){_, _ ->}
            .setPositiveButton("RECORD"){_, _ ->
                val xInput = xEditText.text.toString().replace('.', ',')
                val yInput = yEdiText.text.toString().replace('.', ',')
                val zInput = zEditText.text.toString().replace('.', ',')
                val directionInput = directionSpinner.selectedItem.toString()
                val timePeriodInput = timePeriodEditText.text.toString().toLong()
                if (isValid(xInput, yInput, zInput, directionInput, timePeriodInput)) {
                    fileDialogListener?.onFileDataEntered(xInput, yInput, zInput, directionInput, timePeriodInput)
                }
                else{
                    Toast.makeText(context, "Please specify valid coordinates, direction and time period. Recording not started", Toast.LENGTH_SHORT).show()
                }
            }
        return builder.create()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        try{
            fileDialogListener = context as FileDialogListener
        } catch (c: ClassCastException){
            throw ClassCastException("${context.toString()} must implement FileDialogListener.")
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