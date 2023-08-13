package com.hz_apps.filetimelock.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.hz_apps.filetimelock.database.LockFile
import com.hz_apps.filetimelock.databinding.DialogLockFileViewBinding

class LockFileViewDialog (val lockFile : LockFile): DialogFragment() {
    private lateinit var bindings : DialogLockFileViewBinding
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        bindings = DialogLockFileViewBinding.inflate(layoutInflater)
        val dialogBuilder = AlertDialog.Builder(requireContext())
        dialogBuilder.setView(bindings.root)

        return dialogBuilder.create()
    }
}