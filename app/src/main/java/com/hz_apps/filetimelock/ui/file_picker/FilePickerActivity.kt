package com.hz_apps.filetimelock.ui.file_picker

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.hz_apps.filetimelock.adapters.FileViewAdapter
import com.hz_apps.filetimelock.databinding.ActivityAddFileBinding

class FilePickerActivity : AppCompatActivity() {

    private lateinit var adapter: FileViewAdapter
    private val viewModel: FilePickerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bindings = ActivityAddFileBinding.inflate(layoutInflater)
        setContentView(bindings.root)
        setSupportActionBar(bindings.toolbarFilePicker)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        bindings.toolbarFilePicker.setNavigationOnClickListener {
            onBackPressed()
        }

        bindings.cancelFilePicker.setOnClickListener {
            super.onBackPressed()
        }


        // ask user for storage permission

        //val files = getFilesAndFolders(path);


        adapter = FileViewAdapter(viewModel)

        bindings.recyclerFileView.adapter = adapter

    }

    override fun onBackPressed() {
        if (adapter.getCurrentPath() == viewModel.path) {
            super.onBackPressed()
        } else {
            adapter.onBackPressed()
        }
    }

}