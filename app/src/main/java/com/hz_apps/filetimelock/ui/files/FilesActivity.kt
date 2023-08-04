package com.hz_apps.filetimelock.ui.files

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.distinctUntilChanged
import com.hz_apps.filetimelock.adapters.LockedFileViewAdapter
import com.hz_apps.filetimelock.database.AppDB
import com.hz_apps.filetimelock.database.DBRepository
import com.hz_apps.filetimelock.databinding.ActivityFilesBinding
import com.hz_apps.filetimelock.ui.lock_file.LockFileActivity
import com.hz_apps.filetimelock.ui.permissions.PermissionsActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FilesActivity : AppCompatActivity() {

    private val viewModel = FilesViewModel()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bindings = ActivityFilesBinding.inflate(layoutInflater)
        bindings.toolbarFilesActivity.title = "File Time Lock"
        setContentView(bindings.root)
        setSupportActionBar(bindings.toolbarFilesActivity)

        val appDB = AppDB.getInstance(applicationContext)
        val repository = DBRepository(appDB.lockFileDao())

        bindings.floatingBtnFilesActivity.setOnClickListener {
            if(ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED) {
                val intent = Intent(this, LockFileActivity::class.java)
                startActivity(intent)
            }else{
                val intent = Intent(this, PermissionsActivity::class.java)
                startActivity(intent)
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            val lockedFiles = viewModel.getLockedFiles(repository).distinctUntilChanged()
            launch(Dispatchers.Main) {
                lockedFiles.observe(this@FilesActivity) {
                    if (it.size == 0) {
                        bindings.emptyLockedFiles.visibility = View.VISIBLE
                    }else{
                        if (bindings.emptyLockedFiles.isVisible) bindings.emptyLockedFiles.visibility = View.GONE
                        val adapter = LockedFileViewAdapter(it)
                        bindings.lockedFilesRecyclerview.adapter = adapter
                    }

                }
            }
        }


    }
}