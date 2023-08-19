package com.hz_apps.filetimelock.ui.lock_file

import android.app.Dialog
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.hz_apps.filetimelock.database.AppDB
import com.hz_apps.filetimelock.database.DBRepository
import com.hz_apps.filetimelock.database.LockFile
import com.hz_apps.filetimelock.databinding.ActivityLockFileBinding
import com.hz_apps.filetimelock.ui.dialogs.FileCopyDialog
import com.hz_apps.filetimelock.utils.createFolder
import com.hz_apps.filetimelock.utils.getDateInFormat
import com.hz_apps.filetimelock.utils.getFileExtension
import com.hz_apps.filetimelock.utils.getTimeIn12HourFormat
import com.hz_apps.filetimelock.utils.setFileIcon
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import java.time.LocalDateTime
import java.time.ZoneOffset

class LockFileActivity : AppCompatActivity(), FileCopyDialog.OnFileCopyListeners {

    private val viewModel: LockFileViewModel by viewModels()
    private lateinit var bindings: ActivityLockFileBinding
    private val appDB : AppDB by lazy { AppDB.getInstance(this) }
    private val repository : DBRepository by lazy { DBRepository(appDB.lockFileDao())}
    private var id = 0
    private lateinit var destination : String
    private var exitDialog : Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindings = ActivityLockFileBinding.inflate(layoutInflater)
        setContentView(bindings.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Select Unlock Time"

        // Check if the selected file is already available or launch the file picker
        viewModel.lockFile = File(intent.getStringExtra("file_path")!!)
        setValues()

        bindings.okLockFile.setOnClickListener {
            createDestinationFilePath()
            val fileCopyDialog = FileCopyDialog(this)
            fileCopyDialog.arguments = Bundle().apply {
                putString("source", viewModel.lockFile.path)
                putString("destination", destination)
            }
            fileCopyDialog.show(supportFragmentManager, "copyFile")
        }
    }

    private fun createDestinationFilePath() {
        runBlocking {
            CoroutineScope(Dispatchers.IO).launch {
                id = try { repository.getLastId() + 1 }
                catch (e: Exception) {0}
                createFolder(this@LockFileActivity, "data")
                destination = "data/data/${this@LockFileActivity.packageName}/data/$id"
            }.join()
        }
    }

    private suspend fun insertFileIntoDB() {
        val file = LockFile(
            id,
            viewModel.lockFile.name,
            LocalDateTime.now(),
            viewModel.unlockDateTime,
            destination,
            viewModel.lockFile.length(),
            getFileExtension(viewModel.lockFile),
            false,
        )

        repository.insertLockFile(file)
    }

    // Set date and time in the TextView based on the ViewModel's date and time
    private fun setDateTimeInTextView() {
        bindings.timeLockFile.text = getTimeIn12HourFormat(viewModel.unlockDateTime)
        bindings.dateLockFile.text = getDateInFormat(viewModel.unlockDateTime)
    }

    // Set file information and date-time listeners
    private fun setValues() {
        val fileView = bindings.fileViewLockFile
        val lockFile = viewModel.lockFile
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
                val date = LocalDateTime.ofEpochSecond(it / 1000, 0, ZoneOffset.UTC)
                viewModel.unlockDateTime = LocalDateTime.of(
                    date.year,
                    date.month,
                    date.dayOfMonth,
                    viewModel.unlockDateTime.hour,
                    viewModel.unlockDateTime.minute)
                setDateTimeInTextView()
            }
            datePickerBuilder.show(supportFragmentManager, "DATE_PICKER")
        }

        // Time picker click listener
        bindings.timeLockFile.setOnClickListener {
            timePicker.setHour(viewModel.unlockDateTime.hour)
            timePicker.setMinute(viewModel.unlockDateTime.minute)

            val timePickerBuilder = timePicker.build()

            timePickerBuilder.addOnPositiveButtonClickListener {
                viewModel.unlockDateTime = LocalDateTime.of(
                    viewModel.unlockDateTime.year,
                    viewModel.unlockDateTime.month,
                    viewModel.unlockDateTime.dayOfMonth,
                    timePickerBuilder.hour,
                    timePickerBuilder.minute
                )
                setDateTimeInTextView()
            }

            timePickerBuilder.show(supportFragmentManager, "TIME_PICKER")
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        if (exitDialog == null) {
            val dialogBuilder = MaterialAlertDialogBuilder(this)
            dialogBuilder.setMessage("Do you really want to go back? If you go back file will not be locked.")
            dialogBuilder.setNegativeButton("No"
            ) { dialog, which ->

            }
            dialogBuilder.setPositiveButton("Yes") {
                dialog, which ->
                onBackPressed()
            }
            exitDialog = dialogBuilder.create()
        }
        exitDialog?.show()
        return super.onSupportNavigateUp()
    }

    override fun onFileCopied() {
        runBlocking {
            CoroutineScope(Dispatchers.IO).launch {
                insertFileIntoDB()
            }.join()
        }
        finish()
    }

    override fun onFileCopyError() {
        TODO("Not yet implemented")
    }
}
