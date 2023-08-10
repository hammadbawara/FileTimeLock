package com.hz_apps.filetimelock.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.hz_apps.filetimelock.database.AppDB
import com.hz_apps.filetimelock.database.DBRepository
import com.hz_apps.filetimelock.database.LockFile
import com.hz_apps.filetimelock.databinding.DialogCopyFileBinding
import com.hz_apps.filetimelock.utils.copyFile
import com.hz_apps.filetimelock.utils.createFolder
import com.hz_apps.filetimelock.utils.getFileExtension
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDateTime

class LockFileDialog(
    private val lockFile : File,
    private val unlockTime : LocalDateTime,
    private val onFileLockedDialogListener: OnFileLockedDialogListener
) : DialogFragment() {
    private lateinit var bindings : DialogCopyFileBinding
    private lateinit var db : AppDB
    private lateinit var repository : DBRepository
    private lateinit var destination : String
    private var id : Int = 0

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        bindings = DialogCopyFileBinding.inflate(layoutInflater)

        // Preparing database
        db = AppDB.getInstance(requireContext())
        repository = DBRepository(db.lockFileDao())

        val dialog = AlertDialog.Builder(requireContext())
        dialog.setTitle("Loading")
        dialog.setView(bindings.root)

        dialog.setNegativeButton("Cancel") { _, _ ->
            dismiss()
        }

        lifecycleScope.launch(Dispatchers.IO) {
            copyAndSaveIntoDB(dialog)
        }


        dialog.setCancelable(false)

        return dialog.create()
    }

    private suspend fun copyAndSaveIntoDB(dialog : AlertDialog.Builder) {
        withContext(Dispatchers.Main) {
            dialog.setMessage("Preparing file")
        }
        createDestinationFilePath()

        withContext(Dispatchers.Main) {
            dialog.setMessage("Copying file")
        }
        copyFile()

        withContext(Dispatchers.Main) {
            dialog.setMessage("Finalizing")
        }
        lockFile()
        onFileLockedDialogListener.onFileLocked()
    }

    private suspend fun createDestinationFilePath() {
        id = try { repository.getLastId() + 1 }
        catch (e: Exception) {0}
        createFolder(requireContext(), "data")
        destination = "data/data/${requireContext().packageName}/data/$id"
    }

    private suspend fun lockFile() {
        val file = LockFile(
            id,
            lockFile.name,
            LocalDateTime.now(),
            unlockTime,
            destination,
            lockFile.length().toString(),
            getFileExtension(lockFile)
        )

        repository.insertLockFile(file)
    }

    private suspend fun copyFile() {
        copyFile(lockFile, File(destination), bindings.copyFileProgressBar)
    }

    interface OnFileLockedDialogListener {
        fun onFileLocked()
        fun onFileLockedError()
    }
}