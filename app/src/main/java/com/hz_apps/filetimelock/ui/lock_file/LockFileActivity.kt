package com.hz_apps.filetimelock.ui.lock_file

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hz_apps.filetimelock.R
import com.hz_apps.filetimelock.ui.file_picker.FilePickerActivity

class LockFileActivity : AppCompatActivity() {

    val requestCode = 1;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lock_file)

        val intent = Intent(this, FilePickerActivity::class.java)

        startActivity(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        print(requestCode)
    }

}