package com.hz_apps.filetimelock.ui.add_files

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hz_apps.filetimelock.adapters.FileViewAdapter
import com.hz_apps.filetimelock.databinding.ActivityAddFileBinding
import java.io.File

class AddFileActivity : AppCompatActivity() {

    private lateinit var adapter : FileViewAdapter
    private val path = "/storage/emulated/0"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bindings = ActivityAddFileBinding.inflate(layoutInflater)
        setContentView(bindings.root)

        // ask user for storage permission

        //val files = getFilesAndFolders(path);

        val file = File(path)

        val files = file.listFiles()
        adapter = FileViewAdapter(file)

        bindings.recyclerFileView.adapter = adapter

    }

    fun getFilesAndFolders(path: String): List<File> {
        val files = mutableListOf<File>()
        val folders = mutableListOf<File>()

        val file = File(path)
        if (file.exists()) {
            for (child in file.listFiles()!!) {
                if (child.isFile) {
                    files.add(child)
                } else if (child.isDirectory) {
                    folders.add(child)
                }
            }
        }

        return files + folders
    }

    override fun onBackPressed() {
        if (adapter.getCurrentPath() == path) {
            super.onBackPressed()
        } else {
            adapter.onBackPressed()
        }
    }

}