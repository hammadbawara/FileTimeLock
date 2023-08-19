package com.hz_apps.filetimelock.dialogs

import android.app.Dialog
import android.content.ContentResolver
import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.hz_apps.filetimelock.databinding.DialogCopyFileBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class FileTransferDialog: DialogFragment() {

    private val bindings : DialogCopyFileBinding by lazy {
        DialogCopyFileBinding.inflate(layoutInflater)
    }
    private lateinit var mainDialog : Dialog
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = MaterialAlertDialogBuilder(requireContext())

        builder.setView(bindings.root)
        builder.setCancelable(false)
        builder.setNegativeButton("Cancel"
        ) { dialog, which ->
            dismiss()
        }

        val source = arguments?.getString("source")

        if (source == null) {
            throw Exception("Source or destination is null.")
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            CoroutineScope(Dispatchers.IO).launch {
                transferFile(File(source), requireContext().contentResolver)
                dialog?.dismiss()
            }
        }
        else {
            CoroutineScope(Dispatchers.IO).launch {
                transferFileBelowAndroid10(File(source), requireContext().contentResolver)
                dialog?.dismiss()
            }
        }

        mainDialog = builder.create()
        return mainDialog
    }

    private fun onCopyError(message: String) {
        CoroutineScope(Dispatchers.Main).launch {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            mainDialog.dismiss()
        }
    }

    private fun checkSourceAndDirectory(source: File) : File{
        if (!source.exists()) {
            onCopyError("Source File doesn't exists")
        }
        val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val timeLockFolder = File(downloadDir, "File Time Lock")
        if (!timeLockFolder.exists()) {
            timeLockFolder.mkdirs()
            if (!timeLockFolder.mkdirs()) {
                onCopyError("Unable to make directory")
            }

        }

        return timeLockFolder
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private suspend fun transferFile(source: File, contentResolver: ContentResolver) {

        val timeLockFolder = checkSourceAndDirectory(source)

        val destinationFileName = source.name

        val existingFile = File(timeLockFolder, source.name)
        if (existingFile.exists()) {
            val newName = findUniqueFileName(timeLockFolder, destinationFileName)
            existingFile.renameTo(File(timeLockFolder, newName))
        }

        val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, destinationFileName)
            put(MediaStore.Downloads.MIME_TYPE, MimeTypeMap.getSingleton().getMimeTypeFromExtension(source.extension))
            put(MediaStore.Downloads.IS_PENDING, 1)
        }

        val uri: Uri? = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

        try {
            contentResolver.openOutputStream(uri!!)?.use { outputStream ->
                source.inputStream().use { inputStream ->
                    val buffer = ByteArray(512000)
                    var bytesRead: Int
                    var totalByteRead = 0L
                    val fileSize = source.length()

                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        totalByteRead += bytesRead
                        withContext(Dispatchers.Main) {
                            bindings.copyFileProgressBar.progress = (totalByteRead * 100 / fileSize).toInt()
                        }
                        outputStream.write(buffer, 0, bytesRead)
                    }
                    outputStream.flush()
                }
            }

            contentValues.clear()
            contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
            contentResolver.update(uri, contentValues, null, null)
        } catch (e: Exception) {
            throw Exception("Something went wrong")
        }
    }

    private fun findUniqueFileName(directory: File, baseName: String): String {
        var newName = baseName
        var counter = 1
        val extension = MimeTypeMap.getFileExtensionFromUrl(baseName)

        while (File(directory, newName).exists()) {
            newName = "${baseName}_${counter++}.$extension"
        }

        return newName
    }

    private suspend fun transferFileBelowAndroid10(source: File, contentResolver: ContentResolver) {

        val timeLockFolder = checkSourceAndDirectory(source)

        // Determine the destination file's name
        val destinationFileName = source.name

        // Handle cases where a file with the same name already exists
        val existingFile = File(timeLockFolder, source.name)
        if (existingFile.exists()) {
            val newName = findUniqueFileName(timeLockFolder, destinationFileName)
            existingFile.renameTo(File(timeLockFolder, newName))
        }

        // Create and write to the new destination file
        val newFile = File(timeLockFolder, destinationFileName)
        var inputStream : InputStream? = null
        var outputStream : OutputStream? = null
        try {
            inputStream = source.inputStream()
            outputStream = newFile.outputStream()

            val buffer = ByteArray(512000)
            var length : Int
            val totalSize = source.length()
            var readBytes :Int = 0

            while (inputStream.read(buffer).also { length = it } > 0) {
                outputStream.write(buffer, 0, length)
                readBytes += length
                val percentage = ((readBytes*100)/totalSize).toInt()
                withContext(Dispatchers.Main){
                    bindings.copyFileProgressBar.progress = percentage
                    bindings.percentageCopyFileDialog.text = "$percentage%"
                }
            }

            outputStream.flush()

        }
        catch (e : Exception) {
            onCopyError("Something went wrong")
        }
        finally {
            if (inputStream != null) {
                try {
                    inputStream.close()
                }catch (e : IOException) {
                    e.printStackTrace()
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close()
                }catch (e: IOException){
                    e.printStackTrace()
                }
            }
        }
    }



}