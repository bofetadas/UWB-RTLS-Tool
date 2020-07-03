package bachelor.test.locationapp.presenter.recording

import android.content.Context
import android.os.Environment.DIRECTORY_DOCUMENTS
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class FileWriter(private val context: Context) {

    lateinit var file: File

    fun createFile(x: String, y: String, z: String, direction: String): Boolean{
        val sdf = SimpleDateFormat("dd-MM-yyyy-HH-mm-ss", Locale.GERMANY)
        val date = Date()

        file = File(context.getExternalFilesDir(DIRECTORY_DOCUMENTS), "$direction(${x}_${y}_${z})_${sdf.format(date)}.txt")
        return file.createNewFile()
    }

    fun writeToFile(message: String){
        file.appendText("$message, ${System.currentTimeMillis()}\n")
    }
}