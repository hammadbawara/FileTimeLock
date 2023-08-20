package com.hz_apps.filetimelock.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class InformationDialog: DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = MaterialAlertDialogBuilder(requireContext())

        val message = arguments?.getString("MESSAGE")

        dialog.setTitle("Info")
        dialog.setMessage(message)
        dialog.setPositiveButton("OK") { _, _ ->
            dismiss()
        }
        return dialog.create()
    }
}