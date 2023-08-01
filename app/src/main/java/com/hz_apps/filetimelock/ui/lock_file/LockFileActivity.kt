package com.hz_apps.filetimelock.ui.lock_file

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.hz_apps.filetimelock.databinding.ActivityLockFileBinding
import com.hz_apps.filetimelock.ui.file_picker.FilePickerActivity
import java.io.File

class LockFileActivity : AppCompatActivity() {

    private lateinit var lockFile : File
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bindings = ActivityLockFileBinding.inflate(layoutInflater)
        setContentView(bindings.root)

        val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intent = result.data
                lockFile = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent!!.getSerializableExtra("result", File::class.java)!!
                }else{
                    intent!!.getSerializableExtra("result") as File
                }

                val fileView = bindings.fileViewLockFile

                fileView.nameFileView.text = lockFile.name

            }else{
                finish()
            }
        }

        startForResult.launch(Intent(this, FilePickerActivity::class.java))

    }

}