package com.hz_apps.filetimelock.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.hz_apps.filetimelock.database.LockFile
import com.hz_apps.filetimelock.databinding.DialogLockFileViewBinding
import com.hz_apps.filetimelock.utils.calculateTimeDifferenceTillEnd
import com.hz_apps.filetimelock.utils.formatFileSize
import com.hz_apps.filetimelock.utils.openLockFile
import com.hz_apps.filetimelock.utils.setFileIcon
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class LockFileViewDialog(): DialogFragment() {
    private lateinit var bindings : DialogLockFileViewBinding
    private val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy  hh:mm a")
    private lateinit var lockFile : LockFile

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        bindings = DialogLockFileViewBinding.inflate(layoutInflater)
        val dialogBuilder = MaterialAlertDialogBuilder(requireContext())

        lockFile = arguments?.getSerializable("LOCK_FILE") as LockFile
        val timeNow = arguments?.getSerializable("TIME_NOW") as LocalDateTime

        bindings.nameLockFileView.text = lockFile.name
        bindings.dateAddedLockFileView.text = lockFile.dateAdded.format(dateFormatter)
        bindings.unlockDateLockFileView.text = lockFile.dateUnlock.format(dateFormatter)
        bindings.timeRemainingLockFileView.text = calculateTimeDifferenceTillEnd(lockFile.dateUnlock, timeNow)
        bindings.sizeLockFileView.text = formatFileSize(lockFile.size)

        dialogBuilder.setNegativeButton("Cancel"
        ) { dialog, which ->

        }
        if (lockFile.isUnlocked) {
            dialogBuilder.setPositiveButton("Open") { dialog, which ->
                openLockFile(requireContext(), lockFile)
            }
            setFileIcon(requireContext(), bindings.iconLockFileView, File(lockFile.path), lockFile.extension)

        }else{
            setFileIcon(bindings.iconLockFileView, lockFile.extension)
        }
        dialogBuilder.setView(bindings.root)

        return dialogBuilder.create()
    }


}