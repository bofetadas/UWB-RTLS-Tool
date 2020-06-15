package bachelor.test.locationapp.view

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.InputFilter
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import bachelor.test.locationapp.R
import bachelor.test.locationapp.model.Directions

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
        val directions = arrayOf(Directions.N, Directions.E, Directions.S, Directions.W)
        val spinnerAdapter = ArrayAdapter(activity!!.applicationContext, android.R.layout.simple_spinner_dropdown_item, directions)
        directionSpinner.adapter = spinnerAdapter

        setInputFilters(xEditText, yEdiText, zEditText)

        builder.setView(view)
            .setTitle("Enter X, Y, Z and direction you are facing to")
            .setNegativeButton("CANCEL"){_, _ ->}
            .setPositiveButton("RECORD"){_, _ ->
                val xInput = xEditText?.text.toString()
                val yInput = yEdiText?.text.toString()
                val zInput = zEditText?.text.toString()
                val directionInput = directionSpinner?.selectedItem.toString()
                if (isValid(xInput, yInput, zInput, directionInput)) {
                    fileDialogListener?.onFileDataEntered(xInput, yInput, zInput, directionInput)
                }
                else{
                    Toast.makeText(context, "Please specify valid coordinates and a direction. Recording not started", Toast.LENGTH_SHORT).show()
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

    private fun isValid(x: String, y: String, z: String, direction: String): Boolean{
        return  x.isNotBlank()
                && y.isNotBlank()
                && z.isNotBlank()
                && direction.isNotBlank()

    }

    // Set input filter so that only digits & commas can be input into the editText
    private fun setInputFilters(xEditText: EditText, yEditText: EditText, zEditText: EditText) {
        val filter = InputFilter { source, start, end, _, _, _ ->
            for (c in start until end) {
                // Accept only digits & commas (for file name creation)
                if (!Character.isDigit(source[c]) && source[c] != ',') {
                    Toast.makeText(activity, "Only digits and commas are valid", Toast.LENGTH_SHORT).show()
                    return@InputFilter ""
                }
            }
            null
        }
        xEditText.filters = arrayOf(filter)
        yEditText.filters = arrayOf(filter)
        zEditText.filters = arrayOf(filter)
    }
}