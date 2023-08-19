package com.hz_apps.filetimelock.ui.file_picker

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.hz_apps.filetimelock.databinding.ActivityFilePickerBinding
import com.hz_apps.filetimelock.ui.dialogs.FileCopyDialog
import java.io.File

class FilePickerActivity : AppCompatActivity() {
    private val bindings : ActivityFilePickerBinding by lazy {
        ActivityFilePickerBinding.inflate(layoutInflater)
    }
    private lateinit var  viewModel : FilePickerViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(bindings.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Select a file"

        viewModel = ViewModelProvider(this)[FilePickerViewModel::class.java]

        viewModel.isLaunchedAsFileTransfer = intent.getBooleanExtra("IS_LAUNCHED_AS_FILE_TRANSFER", false)

        if (viewModel.isLaunchedAsFileTransfer) {
            bindings.moveFilePicker.visibility = View.VISIBLE
            bindings.moveFilePicker.setOnClickListener {
                transferFile()
            }
        }

    }

    private fun transferFile() {
        val source = intent.getStringExtra("source")
        val destination = intent.getStringExtra("destination")

        if (source == null ) {
            throw NullPointerException("source should not be null")
        }

        val listener = object : FileCopyDialog.OnFileCopyListeners {
            override fun onFileCopied() {
                finish()
            }

            override fun onFileCopyError() {
                TODO("Not yet implemented")
            }

        }

        val dialog = FileCopyDialog(listener)
        dialog.arguments = Bundle().apply {
            putString("source", source)
            putString("destination", "${viewModel.path}/${File(source).name}")
        }
        dialog.show(supportFragmentManager, "copyFile")
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }
}