package com.hz_apps.filetimelock.ui.file_transfer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hz_apps.filetimelock.databinding.ActivityFileTransferBinding

class FileTransferActivity : AppCompatActivity() {
    private lateinit var bindings : ActivityFileTransferBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindings = ActivityFileTransferBinding.inflate(layoutInflater)
        setContentView(bindings.root)


    }
}