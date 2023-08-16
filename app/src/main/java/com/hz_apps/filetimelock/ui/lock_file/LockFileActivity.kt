package com.hz_apps.filetimelock.ui.lock_file

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.hz_apps.filetimelock.databinding.ActivityLockFileBinding
import com.hz_apps.filetimelock.ui.dialogs.LockFileDialog
import com.hz_apps.filetimelock.utils.getDateInFormat
import com.hz_apps.filetimelock.utils.getFileExtension
import com.hz_apps.filetimelock.utils.getTimeIn12HourFormat
import com.hz_apps.filetimelock.utils.setFileIcon
import java.io.File
import java.time.LocalDateTime

class LockFileActivity : AppCompatActivity(), LockFileDialog.OnFileLockedDialogListener {

    private val viewModel: LockFileViewModel by viewModels()
    private lateinit var bindings: ActivityLockFileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindings = ActivityLockFileBinding.inflate(layoutInflater)
        setContentView(bindings.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Check if the selected file is already available or launch the file picker
        viewModel.lockFile = File(intent.getStringExtra("file_path")!!)
        setValues()

        bindings.okLockFile.setOnClickListener {
            val lockFileDialog = viewModel.lockFile?.let { LockFileDialog(it, viewModel.getUnlockTime(), this) }
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
        val lockFile = viewModel.lockFile!!
        fileView.nameFileView.text = lockFile.name
        setFileIcon(this, fileView.iconFileView, lockFile, getFileExtension(lockFile))

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

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    override fun onFileLocked() {
        finish()
    }

    override fun onFileLockedError() {
        TODO("Not yet implemented")
    }
}
