package com.hz_apps.filetimelock.ui.file_picker

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.hz_apps.filetimelock.databinding.ActivityFilePickerBinding

class FilePickerActivity : AppCompatActivity() {
    private val bindings : ActivityFilePickerBinding by lazy {
        ActivityFilePickerBinding.inflate(layoutInflater)
    }
    private lateinit var  viewModel : FilePickerViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(bindings.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        viewModel = ViewModelProvider(this)[FilePickerViewModel::class.java]

        viewModel.isLaunchedAsFileTransfer = intent.getBooleanExtra("IS_LAUNCHED_AS_FILE_TRANSFER", false)

        if (viewModel.isLaunchedAsFileTransfer) {
            bindings.moveFilePicker.visibility = View.VISIBLE
            bindings.moveFilePicker.setOnClickListener {
                Toast.makeText(this, viewModel.path, Toast.LENGTH_SHORT).show()
            }
        }

    }
}