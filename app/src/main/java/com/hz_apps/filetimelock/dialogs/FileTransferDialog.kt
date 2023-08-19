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
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.hz_apps.filetimelock.databinding.DialogCopyFileBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException

class FileTransferDialog: DialogFragment() {

    private val bindings : DialogCopyFileBinding by lazy {
        DialogCopyFileBinding.inflate(layoutInflater)
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = MaterialAlertDialogBuilder(requireContext())

        builder.setView(bindings.root)
        builder.setCancelable(false)

        val source = arguments?.getString("source")
        val destination = arguments?.getString("destination")

        if (source == null || destination == null) {
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

        return builder.create()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private suspend fun transferFile(source: File, contentResolver: ContentResolver) {
        if (!source.exists()) {
            throw FileNotFoundException("Source file does not exist.")
        }

        val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val timeLockFolder = File(downloadDir, "File Time Lock")
        if (!timeLockFolder.exists()) {
            timeLockFolder.mkdirs()
        }

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
            e.printStackTrace()
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
        if (!source.exists()) {
            throw FileNotFoundException("Source file does not exist.")
        }

        val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val timeLockFolder = File(downloadDir, "File Time Lock")
        if (!timeLockFolder.exists()) {
            timeLockFolder.mkdirs()
        }

        val destinationFileName = source.name

        val existingFile = File(timeLockFolder, source.name)
        if (existingFile.exists()) {
            val newName = findUniqueFileName(timeLockFolder, destinationFileName)
            existingFile.renameTo(File(timeLockFolder, newName))
        }

        val newFile = File(timeLockFolder, destinationFileName)
        newFile.outputStream().use { outputStream ->
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
    }


}