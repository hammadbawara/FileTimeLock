package com.hz_apps.filetimelock.ui.lock_file

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.hz_apps.filetimelock.R
import com.hz_apps.filetimelock.ui.file_picker.FilePickerActivity

class LockFileActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lock_file)

        val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intent = result.data
            }else{
                finish()
            }
        }

        startForResult.launch(Intent(this, FilePickerActivity::class.java))

    }

}