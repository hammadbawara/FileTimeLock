package com.hz_apps.filetimelock.ui.files

import OnTimeAPIListener
import TimeApiClient
import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.lifecycle.distinctUntilChanged
import com.hz_apps.filetimelock.R
import com.hz_apps.filetimelock.adapters.ClickListenerLockedFile
import com.hz_apps.filetimelock.adapters.LockedFileViewAdapter
import com.hz_apps.filetimelock.database.AppDB
import com.hz_apps.filetimelock.database.DBRepository
import com.hz_apps.filetimelock.database.LockFile
import com.hz_apps.filetimelock.databinding.ActivityFilesBinding
import com.hz_apps.filetimelock.ui.lock_file.LockFileActivityDialog
import com.hz_apps.filetimelock.ui.permissions.PermissionsActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import java.time.LocalDateTime

class FilesActivity : AppCompatActivity(), ClickListenerLockedFile, OnTimeAPIListener{

    private val viewModel = FilesViewModel()
    private lateinit var bindings : ActivityFilesBinding
    private var actionModeCallBack : ActionMode.Callback? = null
    private lateinit var adapter : LockedFileViewAdapter
    private lateinit var repository : DBRepository
    private val timeApiClient by lazy { TimeApiClient(this, this) }
    private var timeGetJob : Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindings = ActivityFilesBinding.inflate(layoutInflater)
        setContentView(bindings.root)

        // Creating database instance
        val appDB = AppDB.getInstance(applicationContext)
        repository = DBRepository(appDB.lockFileDao())

        // floating action button
        bindings.floatingBtnFilesActivity.setOnClickListener {
            if(ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED) {
                val intent = Intent(this, LockFileActivityDialog::class.java)
                startActivity(intent)
            }else{
                val intent = Intent(this, PermissionsActivity::class.java)
                startActivity(intent)
            }
        }

        // Getting data from database and setting in recyclerview
        CoroutineScope(Dispatchers.IO).launch {
            val lockedFiles = viewModel.getLockedFiles(repository).distinctUntilChanged()
            launch(Dispatchers.Main) {
                lockedFiles.observe(this@FilesActivity) {
                    setRecyclerView(it)
                }
            }
        }

        bindings.timeGetBtn.setOnClickListener {
            if (timeGetJob != null && timeGetJob!!.isActive) {
                timeGetJob!!.cancel()
                stopGettingTime()
                Toast.makeText(this, "Time Get Cancelled", Toast.LENGTH_SHORT).show()
            }
            else if (timeGetJob == null) {
                Toast.makeText(this, "Getting Time", Toast.LENGTH_SHORT).show()
                checkTime()
            }
        }

        checkTime()
    }

    private fun setRecyclerView(it : MutableList<LockFile>) {
        if (it.size == 0) {
            bindings.emptyLockedFiles.visibility = View.VISIBLE
        }else{
            if (bindings.emptyLockedFiles.isVisible) bindings.emptyLockedFiles.visibility = View.GONE
        }
        adapter = LockedFileViewAdapter(this, it, this)
        bindings.lockedFilesRecyclerview.adapter = adapter
    }

    override fun onItemClicked(itemView: View, position: Int) {
        if (actionModeCallBack == null) {
            val file = File(adapter.lockedFilesList[position].path)
            val contentUri = FileProvider.getUriForFile(this, "com.hz_apps.filetimelock.FileProvider", file)
            val intent = Intent(Intent.ACTION_VIEW)
            val fileType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(adapter.lockedFilesList[position].extension)
            intent.setDataAndType(contentUri, fileType)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivity(intent)
        }else {
            adapter.checkedItems[position] = !adapter.checkedItems[position]
            adapter.notifyItemChanged(position)
        }
    }

    override fun onItemLongClicked(itemView: View, position: Int): Boolean {
        if (actionModeCallBack == null) startActionMode()
        adapter.checkedItems[position] = !adapter.checkedItems[position]
        adapter.notifyItemChanged(position)
        return true
    }
    private fun startActionMode() {
        actionModeCallBack = object : ActionMode.Callback {
            override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                menuInflater.inflate(R.menu.item_locked_file_context_menu, menu)
                return true
            }

            override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                return true
            }

            override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
                if (item != null) {
                    if (item.title == "Delete") {
                        val dialog = AlertDialog.Builder(this@FilesActivity)
                        dialog.setTitle("Delete")
                        dialog.setMessage("Are you sure you want to delete the selected items?")
                        dialog.setPositiveButton("Yes") { _, _ ->
                            deleteSelectedItems()
                            Toast.makeText(this@FilesActivity, "Items Deleted", Toast.LENGTH_SHORT).show()
                            mode?.finish()
                        }
                        dialog.setNegativeButton("No") { _, _ -> }
                        dialog.show()
                    } else if (item.title == "Select All") {
                        Toast.makeText(this@FilesActivity, "Delete", Toast.LENGTH_SHORT).show()
                        TODO()
                    }
                }
                return true
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun onDestroyActionMode(mode: ActionMode?) {
                adapter.checkedItems.fill(false)
                adapter.notifyDataSetChanged()
                actionModeCallBack = null
                bindings.floatingBtnFilesActivity.visibility = View.VISIBLE
            }
        }
        startSupportActionMode(actionModeCallBack as ActionMode.Callback)
        bindings.floatingBtnFilesActivity.visibility = View.INVISIBLE
    }

    fun deleteSelectedItems() {
        for (i in adapter.checkedItems.indices) {
            if (adapter.checkedItems[i]) {
                val file = File(adapter.lockedFilesList[i].path)
                file.delete()
                val job = CoroutineScope(Dispatchers.IO).launch {
                    repository.delete(adapter.lockedFilesList[i])
                }
                runBlocking {
                    job.join()
                }
                adapter.notifyItemChanged(i)
            }
        }
    }

    override fun onGetTime(dateTime: LocalDateTime) {
        bindings.timeViewActivityFiles.text = dateTime.toString()
    }

    override fun onFailToGetTime(error: String) {
        Toast.makeText(this, "Failed to get time. Make sure you are connected to internet", Toast.LENGTH_SHORT).show()
    }

    private fun checkTime() {
        bindings.timeProgressFilesActivity.visibility = View.VISIBLE
        bindings.timeGetBtn.setImageResource(R.drawable.ic_cancel)
        timeGetJob = CoroutineScope(Dispatchers.IO).launch {
            timeApiClient.getCurrentTime()
            stopGettingTime()
        }
    }

    private fun stopGettingTime() {
        timeGetJob = null
        runOnUiThread{
            bindings.timeGetBtn.setImageResource(R.drawable.ic_refresh)
            bindings.timeProgressFilesActivity.visibility = View.GONE
        }
    }

}