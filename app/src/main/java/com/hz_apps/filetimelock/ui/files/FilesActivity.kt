package com.hz_apps.filetimelock.ui.files

import DateAPIClient
import OnTimeAPIListener
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
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
import com.hz_apps.filetimelock.adapters.LockFileListeners
import com.hz_apps.filetimelock.adapters.LockedFileViewAdapter
import com.hz_apps.filetimelock.database.AppDB
import com.hz_apps.filetimelock.database.DBRepository
import com.hz_apps.filetimelock.databinding.ActivityFilesBinding
import com.hz_apps.filetimelock.ui.lock_file.LockFileActivity
import com.hz_apps.filetimelock.ui.permissions.PermissionsActivity
import com.hz_apps.filetimelock.utils.FileSort
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class FilesActivity : AppCompatActivity(), LockFileListeners, OnTimeAPIListener{

    private val viewModel = FilesViewModel()
    private lateinit var bindings : ActivityFilesBinding
    private var actionMode : ActionMode? = null
    private lateinit var adapter : LockedFileViewAdapter
    private lateinit var repository : DBRepository
    private val dateGetAPI by lazy { DateAPIClient(this, this) }
    private var dateGetJob : Job? = null
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("E, d MMM, yyyy   HH:mm")

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
                val intent = Intent(this, LockFileActivity::class.java)
                startActivity(intent)
            }else{
                val intent = Intent(this, PermissionsActivity::class.java)
                startActivity(intent)
            }
        }

        getSharedPrefs()

        // Running Coroutine for getting files from database and time from datastore
        CoroutineScope(Dispatchers.IO).launch {
            // Getting time from dataStore

            getItemFromDBAndSetInRV()

            // Running Main thread tasks
            launch(Dispatchers.Main) {
                // Settings date on textView
                bindings.timeViewActivityFiles.text = viewModel.timeNow?.format(dateTimeFormatter)
                // Time Get Button
                bindings.timeGetBtn.setOnClickListener {
                    if (dateGetJob != null && dateGetJob!!.isActive) {
                        dateGetJob!!.cancel()
                        stopGettingTime()
                        Toast.makeText(this@FilesActivity, "Time Get Cancelled", Toast.LENGTH_SHORT).show()
                    }
                    else if (dateGetJob == null) {
                        Toast.makeText(this@FilesActivity, "Getting Time", Toast.LENGTH_SHORT).show()
                        checkTime()
                    }
                }
                // Checking time from internet
                checkTime()
            }
        }
    }

    private suspend fun getItemFromDBAndSetInRV() {
        withContext(Dispatchers.Main){
            bindings.progressBarFilesActivity.visibility = View.VISIBLE
        }

        val lockedFiles = viewModel.getLockedFiles(repository).distinctUntilChanged()

        withContext(Dispatchers.Main) {
            // observer on locked files
            lockedFiles.observe(this@FilesActivity) {
                if (it.size == 0) {
                    bindings.emptyLockedFiles.visibility = View.VISIBLE
                }else{
                    if (bindings.emptyLockedFiles.isVisible) bindings.emptyLockedFiles.visibility = View.GONE
                }
                adapter = LockedFileViewAdapter(this@FilesActivity, it, this@FilesActivity, viewModel.timeNow!!)
                bindings.lockedFilesRecyclerview.adapter = adapter
            }
            bindings.progressBarFilesActivity.visibility = View.GONE
        }

    }

    /*
      * RecyclerView items click actions
     */
    override fun onItemClicked(itemView: View, position: Int) {
        if (actionMode == null) {
            if (adapter.lockedFilesList[position].isUnlocked) {
                openFile(position)
            }else{
                Toast.makeText(this@FilesActivity, "This file is not unlocked yet", Toast.LENGTH_SHORT).show()
            }
        }else {
            onItemSelected(position)
        }
    }

    private fun openFile(position : Int) {
        val file = File(adapter.lockedFilesList[position].path)
        val contentUri = FileProvider.getUriForFile(this, "com.hz_apps.filetimelock.FileProvider", file)
        val intent = Intent(Intent.ACTION_VIEW)
        val fileType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(adapter.lockedFilesList[position].extension)
        intent.setDataAndType(contentUri, fileType)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(intent)
    }

    private fun onItemSelected(position: Int) {
        if (adapter.checkedItems[position]) {
            adapter.checkedItems[position] = false
            viewModel.numOfSelectedItems--
        }
        else{
            adapter.checkedItems[position] = true
            viewModel.numOfSelectedItems++
        }
        adapter.notifyItemChanged(position)

        if (viewModel.numOfSelectedItems == 0) {
            actionMode?.finish()
        } else{
            actionMode?.title = "${viewModel.numOfSelectedItems} selected"
        }
    }

    override fun onItemLongClicked(itemView: View, position: Int): Boolean {
        if (actionMode == null) {
            startActionMode()
            onItemSelected(position)
        } else{
            onItemSelected(position)
        }
        return true
    }

    private fun startActionMode() {
        val actionModeCallBack = object : ActionMode.Callback {
            override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                menuInflater.inflate(R.menu.item_locked_file_context_menu, menu)
                return true
            }

            override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                return true
            }

            @SuppressLint("NotifyDataSetChanged")
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
                        if (viewModel.numOfSelectedItems == adapter.lockedFilesList.size) {
                            adapter.checkedItems.fill(false)
                            viewModel.numOfSelectedItems = 0
                            adapter.notifyDataSetChanged()
                        } else {
                            adapter.checkedItems.fill(true)
                            viewModel.numOfSelectedItems = adapter.lockedFilesList.size
                            adapter.notifyDataSetChanged()
                        }
                        actionMode?.title = "${viewModel.numOfSelectedItems} selected"
                    }
                }
                return true
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun onDestroyActionMode(mode: ActionMode?) {
                adapter.checkedItems.fill(false)
                adapter.notifyDataSetChanged()
                actionMode = null
                bindings.floatingBtnFilesActivity.visibility = View.VISIBLE
                viewModel.numOfSelectedItems = 0
            }
        }

        actionMode = startSupportActionMode(actionModeCallBack as ActionMode.Callback)
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

    override fun onFileUnlocked(id : Int, position: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            repository.setFileUnlocked(id)
            adapter.lockedFilesList[position].isUnlocked = true
            runOnUiThread {
                adapter.notifyItemChanged(position)
            }
        }
    }

    /*
     * Listeners from TimeApiClient
     */
    override fun onGetTime(dateTime: LocalDateTime) {
        viewModel.timeNow = dateTime
        bindings.timeViewActivityFiles.text = dateTime.format(dateTimeFormatter)
        adapter.updateTimeNow(dateTime)
        CoroutineScope(Dispatchers.IO).launch {
            (dateTime.toEpochSecond(ZonedDateTime.now().offset))
        }
    }
    override fun onFailToGetTime(error: String) {
        Toast.makeText(this, "Failed to get time. Make sure you are connected to internet", Toast.LENGTH_SHORT).show()
    }

    /*
      * Time getting from server buttons
     */
    private fun checkTime() {
        bindings.timeProgressFilesActivity.visibility = View.VISIBLE
        bindings.timeGetBtn.setImageResource(R.drawable.ic_cancel)
        dateGetJob = CoroutineScope(Dispatchers.IO).launch {
            dateGetAPI.getCurrentTime()
            stopGettingTime()
        }
    }

    private fun stopGettingTime() {
        dateGetJob = null
        runOnUiThread{
            bindings.timeGetBtn.setImageResource(R.drawable.ic_refresh)
            bindings.timeProgressFilesActivity.visibility = View.GONE
        }
        storeTimeInSharedPrefs(viewModel.timeNow!!.toEpochSecond(ZonedDateTime.now().offset))
    }

    /*
      * Time store in shared preferences
     */
    private fun storeTimeInSharedPrefs(dateTimeInEpoch : Long) {
        val preferences = getSharedPreferences("FILES_ACTIVITY", Context.MODE_PRIVATE)
        preferences.edit().putLong("time", dateTimeInEpoch).apply()
    }

    private fun getSharedPrefs() {
        if (viewModel.timeNow == null) {
            viewModel.timeNow = LocalDateTime.now()
            // if it give error mean dateTime is not saved yet
            val preferences = getSharedPreferences("FILES_ACTIVITY", Context.MODE_PRIVATE)
            val time = preferences.getLong("time", 0)
            if (time != 0L) {
                viewModel.timeNow = LocalDateTime.ofEpochSecond(time, 0, ZonedDateTime.now().offset)
            }

            val sortType = preferences.getString("sort_by", "LOCK_DATE")!!
            viewModel.sortBy = FileSort.valueOf(sortType)
            viewModel.isAscending = preferences.getBoolean("is_ascending", true)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.files_activity_menu, menu)
        when (viewModel.sortBy) {
            FileSort.NAME -> menu?.findItem(R.id.sort_by_name_files_activity)?.isChecked = true
            FileSort.SIZE -> menu?.findItem(R.id.sort_by_size_files_activity)?.isChecked = true
            FileSort.LOCK_DATE -> menu?.findItem(R.id.sort_by_lock_date_files_activity)?.isChecked = true
            FileSort.UNLOCK_DATE -> menu?.findItem(R.id.sort_by_unlock_date_files_activity)?.isChecked = true
            else -> {
            }
        }
        if (viewModel.isAscending) {
            menu?.findItem(R.id.sort_by_ascending_files_activity)?.isChecked = true
        }else{
            menu?.findItem(R.id.sort_by_descending_files_activity)?.isChecked = true
        }
        return super.onCreateOptionsMenu(menu)
    }

    private fun saveStoredPref() {
        getSharedPreferences("FILES_ACTIVITY", Context.MODE_PRIVATE)
            .edit()
            .putString("sort_by", viewModel.sortBy.name)
            .putBoolean("is_ascending", viewModel.isAscending)
            .apply()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.sort_by_name_files_activity -> {
                handleSortAction(item, FileSort.NAME)
            }
            R.id.sort_by_size_files_activity -> {
                handleSortAction(item, FileSort.SIZE)
            }
            R.id.sort_by_lock_date_files_activity -> {
                handleSortAction(item, FileSort.LOCK_DATE)
            }
            R.id.sort_by_unlock_date_files_activity -> {
                handleSortAction(item, FileSort.UNLOCK_DATE)
            }
            R.id.sort_by_ascending_files_activity -> {
                handleOrderAction(item, true)
            }
            R.id.sort_by_descending_files_activity -> {
                handleOrderAction(item, false)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun handleSortAction(item : MenuItem, sortBy: FileSort) {
        if (viewModel.sortBy != sortBy) {
            viewModel.sortBy = sortBy
            item.isChecked = true
            CoroutineScope(Dispatchers.IO).launch {
                getItemFromDBAndSetInRV()
                saveStoredPref()
            }
        }
    }

    private fun handleOrderAction(item : MenuItem, isAscending: Boolean) {
        if (viewModel.isAscending != isAscending) {
            viewModel.isAscending = isAscending
            item.isChecked = true
            CoroutineScope(Dispatchers.IO).launch {
                getItemFromDBAndSetInRV()
                saveStoredPref()
            }
        }
    }

}