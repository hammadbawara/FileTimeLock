package com.hz_apps.filetimelock.ui.files

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hz_apps.filetimelock.databinding.ActivityFilesBinding
import com.hz_apps.filetimelock.ui.lock_file.LockFileActivity

class FilesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bindings = ActivityFilesBinding.inflate(layoutInflater)
        setContentView(bindings.root)

        bindings.floatingBtnFilesActivity.setOnClickListener {
            val intent = Intent(this, LockFileActivity::class.java)
            startActivity(intent)
        }


    }
}