package com.hz_apps.filetimelock.ui.dialogs

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.hz_apps.filetimelock.database.AppDB
import com.hz_apps.filetimelock.database.DBRepository
import com.hz_apps.filetimelock.database.LockFile
import com.hz_apps.filetimelock.databinding.DialogCopyFileBinding
import com.hz_apps.filetimelock.utils.createFolder
import com.hz_apps.filetimelock.utils.getFileExtension
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.time.LocalDateTime

class LockFileDialog(
    private val lockFile : File,
    private val unlockTime : LocalDateTime,
    private val onFileLockedDialogListener: OnFileLockedDialogListener
) : DialogFragment() {
    private lateinit var bindings : DialogCopyFileBinding
    private lateinit var db : AppDB
    private lateinit var repository : DBRepository
    private lateinit var destination : File
    private var id : Int = 0
    private lateinit var mainDialog : Dialog
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        bindings = DialogCopyFileBinding.inflate(layoutInflater)

        val dialog = MaterialAlertDialogBuilder(requireContext())
        dialog.setView(bindings.root)

        dialog.setNegativeButton("Cancel") { _, _ ->
        }

        dialog.setCancelable(false)

        // Preparing database
        db = AppDB.getInstance(requireContext())
        repository = DBRepository(db.lockFileDao())

        lifecycleScope.launch(Dispatchers.IO) {
            copyAndSaveIntoDB()
        }

        mainDialog = dialog.create()
        return mainDialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    private suspend fun copyAndSaveIntoDB() {
        createDestinationFilePath()
        copyFile(lockFile, destination)
        lockFile()
        onFileLockedDialogListener.onFileLocked()
    }

    private suspend fun createDestinationFilePath() {
        id = try { repository.getLastId() + 1 }
        catch (e: Exception) {0}
        createFolder(requireContext(), "data")
        destination = File("data/data/${requireContext().packageName}/data/$id")
    }

    private suspend fun lockFile() {
        val file = LockFile(
            id,
            lockFile.name,
            LocalDateTime.now(),
            unlockTime,
            destination.absolutePath,
            lockFile.length(),
            getFileExtension(lockFile),
            false,
        )

        repository.insertLockFile(file)
    }

    interface OnFileLockedDialogListener {
        fun onFileLocked()
        fun onFileLockedError()
    }

    override fun onDismiss(dialog: DialogInterface) {
        Toast.makeText(requireContext(), "Canceled", Toast.LENGTH_SHORT).show()
        super.onDismiss(dialog)
    }

    private fun copyFile(source: File, destination: File) {
        if (!source.exists()) {
            throw FileNotFoundException("Source file does not exist")
        }

        if (destination.exists()) {
            if (!destination.delete()) {
                throw IOException("Failed to delete destination file")
            }
        }

        destination.createNewFile()

        val inputStream = FileInputStream(source)
        val outputStream = FileOutputStream(destination)

        val buffer = ByteArray(512000)
        var bytesRead: Int
        var totalBytesRead = 0L
        val fileSize = source.length()

        while (true) {
            bytesRead = inputStream.read(buffer)
            if (bytesRead == -1) {
                break
            }
            outputStream.write(buffer, 0, bytesRead)
            totalBytesRead += bytesRead

            // Calculate progress and update progress bar in the main thread
            val progress = (totalBytesRead * 100 / fileSize).toInt()
            CoroutineScope(Dispatchers.Main).launch {
                bindings.copyFileProgressBar.progress = progress
                bindings.percentageCopyFileDialog.text = "$progress%"
            }
        }

        inputStream.close()
        outputStream.close()
    }
}