package com.hz_apps.filetimelock.ui.lock_file

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.hz_apps.filetimelock.databinding.ActivityLockFileBinding
import com.hz_apps.filetimelock.ui.dialogs.LockFileDialog
import com.hz_apps.filetimelock.ui.file_picker.FilePickerActivity
import com.hz_apps.filetimelock.utils.getDateInFormat
import com.hz_apps.filetimelock.utils.getTimeIn12HourFormat
import com.hz_apps.filetimelock.utils.setFileIcon
import java.io.File
import java.time.LocalDateTime

class LockFileActivity : AppCompatActivity() {

    private lateinit var selectedFile: File
    private val viewModel: LockFileViewModel by viewModels()
    private lateinit var bindings: ActivityLockFileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindings = ActivityLockFileBinding.inflate(layoutInflater)
        setContentView(bindings.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Check if the selected file is already available or launch the file picker
        if (viewModel.lockFile == null) {
            launchFilePicker()
        } else {
            setValues()
        }

        bindings.okLockFile.setOnClickListener {
            val lockFileDialog = viewModel.lockFile?.let { LockFileDialog(it, viewModel.getUnlockTime()) }
            lockFileDialog?.show(supportFragmentManager, "copyFile")
        }
    }

// Set date and time in the TextView based on the ViewModel's date and time
private fun setDateTimeInTextView() {
    bindings.timeLockFile.text = getTimeIn12HourFormat(viewModel.getUnlockTime())
    bindings.dateLockFile.text = getDateInFormat(viewModel.getUnlockTime())
}

// Set file information and date-time listeners
private fun setValues() {
    val fileView = bindings.fileViewLockFile
    fileView.nameFileView.text = viewModel.lockFile?.name ?: "No file selected"
    setFileIcon(this, fileView.iconFileView, viewModel.lockFile!!)

    setDateTimeInTextView()

    val datePicker = MaterialDatePicker.Builder.datePicker()
        .setTitleText("Select Unlock Date")

    val timePicker = MaterialTimePicker.Builder()
        .setTimeFormat(TimeFormat.CLOCK_12H)
        .setTitleText("Select Unlock Time")

    // Date picker click listener
    bindings.dateLockFile.setOnClickListener {
        val datePickerBuilder = datePicker.build()
        datePickerBuilder.addOnPositiveButtonClickListener {
            val dateTime = viewModel.getUnlockTime()
            val selection = datePickerBuilder.selection

            // TODO: Implement date lock file picker based on 'selection'
        }
        datePickerBuilder.show(supportFragmentManager, "DATE_PICKER")
    }

    // Time picker click listener
    bindings.timeLockFile.setOnClickListener {
        timePicker.setHour(viewModel.getUnlockTime().hour)
        timePicker.setMinute(viewModel.getUnlockTime().minute)

        val timePickerBuilder = timePicker.build()

        timePickerBuilder.addOnPositiveButtonClickListener {
            val dateTime = viewModel.getUnlockTime()
            viewModel.setDateTime(
                LocalDateTime.of(
                    dateTime.year,
                    dateTime.monthValue,
                    dateTime.dayOfMonth,
                    timePickerBuilder.hour,
                    timePickerBuilder.minute
                )
            )
            setDateTimeInTextView()
        }

        timePickerBuilder.show(supportFragmentManager, "TIME_PICKER")
    }
}

// Launch the file picker activity using Activity Result API
private fun launchFilePicker() {
    val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                val intent = result.data
                selectedFile =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent!!.getSerializableExtra("result", File::class.java)!!
                    } else {
                        intent!!.getSerializableExtra("result") as File
                    }
                viewModel.lockFile = selectedFile
                setValues()
            } else {
                finish()
            }
        }

    startForResult.launch(Intent(this, FilePickerActivity::class.java))
}
}
