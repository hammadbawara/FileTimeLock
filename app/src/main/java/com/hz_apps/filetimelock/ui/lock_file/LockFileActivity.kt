package com.hz_apps.filetimelock.ui.lock_file

import android.app.Activity
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
import com.hz_apps.filetimelock.ui.file_picker.FilePickerActivity
import com.hz_apps.filetimelock.utils.getDateInFormat
import com.hz_apps.filetimelock.utils.getTimeIn12HourFormat
import com.hz_apps.filetimelock.utils.setFileIcon
import java.io.File
import java.time.LocalDateTime

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

    private fun setDateTimeInTextView() {
        bindings.timeLockFile.text = getTimeIn12HourFormat(viewModel.getDateTime())
        bindings.dateLockFile.text = getDateInFormat(viewModel.getDateTime())
    }
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


        bindings.dateLockFile.setOnClickListener {

            val datePickerBuilder = datePicker.build()

            datePickerBuilder.addOnPositiveButtonClickListener {
                val dateTime = viewModel.getDateTime()

                val selection = datePickerBuilder.selection

                TODO("Implement datelockfile picker")
            }
            datePickerBuilder.show(supportFragmentManager, "DATE_PICKER")
        }

        bindings.timeLockFile.setOnClickListener {
            timePicker.setHour(viewModel.getDateTime().hour)
            timePicker.setMinute(viewModel.getDateTime().minute)

            val timePickerBuilder = timePicker.build()

            timePickerBuilder.addOnPositiveButtonClickListener{
                val dateTime = viewModel.getDateTime()
                viewModel.setDateTime(LocalDateTime.of(
                    dateTime.year,
                    dateTime.monthValue,
                    dateTime.dayOfMonth,
                    timePickerBuilder.hour,
                    timePickerBuilder.minute
                ))
                setDateTimeInTextView()
            }

            timePickerBuilder.show(supportFragmentManager, "TIME_PICKER")
        }

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