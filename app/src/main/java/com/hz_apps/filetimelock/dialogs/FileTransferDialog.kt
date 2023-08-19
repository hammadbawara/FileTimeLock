package com.hz_apps.filetimelock.dialogs

import android.app.Dialog
import android.content.ContentResolver
import android.content.ContentValues
import android.content.DialogInterface
import android.content.pm.ActivityInfo
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
import com.hz_apps.filetimelock.database.AppDB
import com.hz_apps.filetimelock.database.DBRepository
import com.hz_apps.filetimelock.database.LockFile
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
    private var timeLockFolder : File? = null
    private lateinit var mainDialog : Dialog

    private lateinit var  lockFile : LockFile
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = MaterialAlertDialogBuilder(requireContext())
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
        requireActivity().window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        builder.setView(bindings.root)
        builder.setCancelable(false)
        builder.setNegativeButton("Cancel"
        ) { dialog, which ->
            dismiss()
        }



        lockFile = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getSerializable("LOCK_FILE", LockFile::class.java) ?: throw Exception("Source or destination is null.")
        } else {
            arguments?.getSerializable("LOCK_FILE") as LockFile
        }

        CoroutineScope(Dispatchers.IO).launch {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                transferFile(File(lockFile.path), requireContext().contentResolver)
            }else {
                transferFileBelowAndroid10(File(lockFile.path), requireContext().contentResolver)
            }

            deleteFileFromDB()
            dismissDialog()
        }

        mainDialog = builder.create()
        return mainDialog
    }

    private suspend fun deleteFileFromDB() {
        val appDB = AppDB.getInstance(requireContext().applicationContext)
        val repository = DBRepository(appDB.lockFileDao())
        repository.delete(lockFile)
        if (File(lockFile.path).delete()) {
            onCopyError("File is copied to ${lockFile.path} but unable to delete from database")
        }
    }

    private fun dismissDialog() {
        CoroutineScope(Dispatchers.Main).launch {
            if (timeLockFolder != null) {
                Toast.makeText(requireActivity(), "File moved to ${timeLockFolder!!.path}", Toast.LENGTH_LONG).show()
            }
            mainDialog.dismiss()
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        requireActivity().window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        super.onDismiss(dialog)
    }

    private fun onCopyError(message: String) {
        CoroutineScope(Dispatchers.Main).launch {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            mainDialog.dismiss()
        }
    }

    private fun checkSourceAndDirectory(source: File){
        if (!source.exists()) {
            onCopyError("Source File doesn't exists")
        }
        val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        timeLockFolder = File(downloadDir, "File Time Lock")
        if (!timeLockFolder!!.exists()) {
            timeLockFolder!!.mkdirs()
            if (!timeLockFolder!!.mkdirs()) {
                onCopyError("Unable to make directory")
            }

        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private suspend fun transferFile(source: File, contentResolver: ContentResolver) {

        checkSourceAndDirectory(source)

        val destinationFileName = findUniqueFileName(source)

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

    private fun findUniqueFileName(file : File): String {
        var newFile = file
        var counter = 1

        while (newFile.exists()) {
            newFile = File("${file.nameWithoutExtension}(${counter++}).${file.extension}")
        }

        return newFile.name
    }

    private suspend fun transferFileBelowAndroid10(source: File, contentResolver: ContentResolver) {

        checkSourceAndDirectory(source)

        // Determine the destination file's name
        val destinationFileName = findUniqueFileName(source)

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
            e.printStackTrace()
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