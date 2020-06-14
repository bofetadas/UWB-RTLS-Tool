package bachelor.test.locationapp.view

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.InputFilter
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import bachelor.test.locationapp.R

class FilenameDialog : DialogFragment(){

    private var filenameDialogListener : FilenameDialogListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        val inflater = activity?.layoutInflater

        val view = inflater?.inflate(R.layout.filename_dialog, null)
        val filenameTextField = view?.findViewById<EditText>(R.id.filename_text_field)
        setInputFilters(filenameTextField)

        builder.setView(view)
            .setTitle("Enter filename")
            .setNegativeButton("CANCEL"){_, _ ->}
            .setPositiveButton("RECORD"){_, _ ->
                val input = filenameTextField?.text.toString()
                if (input.isNotEmpty()) {
                    filenameDialogListener?.onFilenameEntered(input)
                }
                else{
                    Toast.makeText(context, "Please specify a valid file name. Recording not started", Toast.LENGTH_SHORT).show()
                }
            }
        return builder.create()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        try{
            filenameDialogListener = context as FilenameDialogListener
        } catch (c: ClassCastException){
            throw ClassCastException("${context.toString()} must implement FilenameDialogListener.")
        }
    }

    // Set input filter so that only letters and digits can be input into the text field
    private fun setInputFilters(filenameTextField: EditText?) {
        val filter = InputFilter { source, start, end, _, _, _ ->
            for (c in start until end) {
                // Accept only letter & digits; otherwise just return
                if (!Character.isLetterOrDigit(source[c])) {
                    Toast.makeText(activity, "Only letters and digits are valid", Toast.LENGTH_SHORT).show()
                    return@InputFilter ""
                }
            }
            null
        }
        filenameTextField?.filters = arrayOf(filter)
    }
}