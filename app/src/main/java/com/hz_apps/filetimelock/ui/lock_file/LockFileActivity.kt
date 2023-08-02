package com.hz_apps.filetimelock.ui.lock_file

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.hz_apps.filetimelock.databinding.ActivityLockFileBinding
import com.hz_apps.filetimelock.ui.file_picker.FilePickerActivity
import com.hz_apps.filetimelock.utils.getDateInFormat
import com.hz_apps.filetimelock.utils.getTimeIn12HourFormat
import com.hz_apps.filetimelock.utils.setFileIcon
import java.io.File

class LockFileActivity : AppCompatActivity() {

    private lateinit var lockFile : File
    private val viewModel: LockFileViewModel by viewModels()
    private lateinit var bindings: ActivityLockFileBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindings = ActivityLockFileBinding.inflate(layoutInflater)
        setContentView(bindings.root)

        if (viewModel.lockFile==null) {
            launchFilePicker()
        }else{
            setValues()
        }



    }
    private fun setValues() {
        val fileView = bindings.fileViewLockFile
        fileView.nameFileView.text = viewModel.lockFile?.name ?: "No file selected"
        setFileIcon(this, fileView.iconFileView, viewModel.lockFile!!)

        bindings.timeLockFile.text = getTimeIn12HourFormat(viewModel.getDateTime())
        bindings.dateLockFile.text = getDateInFormat(viewModel.getDateTime())

    }

    private fun launchFilePicker() {

        val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intent = result.data
                viewModel.lockFile = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent!!.getSerializableExtra("result", File::class.java)!!
                }else{
                    intent!!.getSerializableExtra("result") as File
                }
                setValues()

            }else{
                finish()
            }
        }

        startForResult.launch(Intent(this, FilePickerActivity::class.java))

    }

}