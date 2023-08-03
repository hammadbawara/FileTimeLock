package com.hz_apps.filetimelock.ui.files

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hz_apps.filetimelock.adapters.LockedFileViewAdapter
import com.hz_apps.filetimelock.database.AppDB
import com.hz_apps.filetimelock.database.DBRepository
import com.hz_apps.filetimelock.databinding.ActivityFilesBinding
import com.hz_apps.filetimelock.ui.lock_file.LockFileActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FilesActivity : AppCompatActivity() {

    private val viewModel = FilesViewModel()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bindings = ActivityFilesBinding.inflate(layoutInflater)
        setContentView(bindings.root)

        val appDB = AppDB.getInstance(applicationContext)
        val repository = DBRepository(appDB.lockFileDao())

        bindings.floatingBtnFilesActivity.setOnClickListener {
            val intent = Intent(this, LockFileActivity::class.java)
            startActivity(intent)
        }


        CoroutineScope(Dispatchers.IO).launch {
            val lockedFiles = viewModel.getLockedFiles(repository)
            launch(Dispatchers.Main) {
                lockedFiles.observe(this@FilesActivity) {
                val adapter = LockedFileViewAdapter(it)
                    bindings.lockedFilesRecyclerview.adapter = adapter
                }
            }
        }




    }
}