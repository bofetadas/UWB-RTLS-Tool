package bachelor.test.locationapp.model

import android.content.Context
import android.os.Environment.DIRECTORY_DOCUMENTS
import java.io.File

class FileWriter(private val context: Context) {

    lateinit var file: File

    fun createFile(filename: String): Boolean{
        file = File(context.getExternalFilesDir(DIRECTORY_DOCUMENTS), "$filename.txt")
        return file.createNewFile()
    }

    fun writeToFile(message: String){
        file.appendText("$message\n")
    }
}