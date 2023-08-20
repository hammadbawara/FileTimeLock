package com.hz_apps.filetimelock.ui.dialogs

import android.app.Dialog
import android.content.DialogInterface
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.hz_apps.filetimelock.databinding.DialogCopyFileBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

class FileCopyDialog(private val listeners : OnFileCopyListeners) : DialogFragment() {
    private lateinit var bindings : DialogCopyFileBinding
    private lateinit var mainDialog : Dialog
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
        requireActivity().window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        bindings = DialogCopyFileBinding.inflate(layoutInflater)

        val dialog = MaterialAlertDialogBuilder(requireContext())
        dialog.setView(bindings.root)

        dialog.setNegativeButton("Cancel") { _, _ ->
        }

        dialog.setCancelable(false)

        lifecycleScope.launch(Dispatchers.IO) {
            val sourcePath = arguments?.getString("source")
            val destinationPath = arguments?.getString("destination")

            if (sourcePath == null || destinationPath == null) {
                throw Exception("Source and destination arguments should not be null")
            }
            copyFile(File(sourcePath), File(destinationPath))
            listeners.onFileCopied()
        }

        mainDialog = dialog.create()
        return mainDialog
    }


    interface OnFileCopyListeners {
        fun onFileCopied()
        fun onFileCopyError()
    }

    override fun onDismiss(dialog: DialogInterface) {
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        requireActivity().window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
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

        outputStream.flush()

        inputStream.close()
        outputStream.close()
    }
}