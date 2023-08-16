package com.hz_apps.filetimelock.ui.files

import DateAPIClient
import OnTimeAPIListener
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.view.ActionMode
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.distinctUntilChanged
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.hz_apps.filetimelock.R
import com.hz_apps.filetimelock.adapters.LockFileListeners
import com.hz_apps.filetimelock.adapters.LockedFileViewAdapter
import com.hz_apps.filetimelock.database.AppDB
import com.hz_apps.filetimelock.database.DBRepository
import com.hz_apps.filetimelock.databinding.ActivityFilesBinding
import com.hz_apps.filetimelock.ui.dialogs.LockFileViewDialog
import com.hz_apps.filetimelock.ui.file_picker.FilePickerActivity
import com.hz_apps.filetimelock.ui.file_transfer.FileTransferActivity
import com.hz_apps.filetimelock.ui.permissions.PermissionsActivity
import com.hz_apps.filetimelock.ui.settings.SettingsActivity
import com.hz_apps.filetimelock.utils.FileSort
import com.hz_apps.filetimelock.utils.openLockFile
import com.hz_apps.filetimelock.utils.shareFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
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
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("E, d MMM, yyyy   hh:mm a")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindings = ActivityFilesBinding.inflate(layoutInflater)
        setContentView(bindings.root)

        setAppTheme()

        // Creating database instance
        val appDB = AppDB.getInstance(applicationContext)
        repository = DBRepository(appDB.lockFileDao())

        // floating action button
        bindings.floatingBtnFilesActivity.setOnClickListener {
            if(ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED) {
                val intent = Intent(this, FilePickerActivity::class.java)
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
            }.join()
        }
    }

    private suspend fun getItemFromDBAndSetInRV() {
        CoroutineScope(Dispatchers.Main).launch{
            bindings.progressBarFilesActivity.visibility = View.VISIBLE
        }.join()

        val lockedFiles = viewModel.getLockedFiles(repository).distinctUntilChanged()

        CoroutineScope(Dispatchers.Main).launch {
            // observer on locked files
            lockedFiles.observe(this@FilesActivity) {
                if (!bindings.progressBarFilesActivity.isVisible) {
                    bindings.progressBarFilesActivity.visibility = View.VISIBLE
                }
                if (it.size == 0) {
                    bindings.emptyLockedFiles.visibility = View.VISIBLE
                }else{
                    if (bindings.emptyLockedFiles.isVisible) bindings.emptyLockedFiles.visibility = View.GONE
                }
                adapter = LockedFileViewAdapter(this@FilesActivity, it, this@FilesActivity, viewModel.timeNow!!)
                bindings.lockedFilesRecyclerview.adapter = adapter
                bindings.progressBarFilesActivity.visibility = View.GONE
            }
        }.join()

    }

    /*
      * RecyclerView items click actions
     */
    override fun onItemClicked(itemView: View, position: Int) {
        if (actionMode == null) {
            showLockFileInfo(position)
        }else {
            onItemSelected(itemView, position)
        }
    }

    override fun onItemLongClicked(itemView: View, position: Int): Boolean {
        if (actionMode == null) {
            startActionMode()
            onItemSelected(itemView, position)
        } else{
            onItemSelected(itemView, position)
        }
        return true
    }

    override fun onMoreOptionClicked(moreOptionView: View, itemView: View, position: Int) {
        val lockFile = adapter.lockedFilesList[position]
        val popupMenu = PopupMenu(this, moreOptionView)

        if (adapter.lockedFilesList[position].isUnlocked) {
            popupMenu.menu.add("Open")
            popupMenu.menu.add("Share")
            popupMenu.menu.add("Move")
        }

        popupMenu.menu.add("Info")
        popupMenu.menu.add("Delete")

        popupMenu.setOnMenuItemClickListener {
            when(it.title) {
                "Info" -> {
                    showLockFileInfo(position)
                }
                "Delete" -> {
                    startActionMode()
                    onItemSelected(itemView, position)
                    deleteItems()
                }
                "Open" -> {
                    openLockFile(this, lockFile)
                }

                "Share" -> {
                    shareFile(this, File(lockFile.path))
                }

                "Move" -> {
                    val intent = Intent(this, FileTransferActivity::class.java)
                    startActivity(intent)
                }
            }
            true
        }
        popupMenu.show()
    }

    private fun showLockFileInfo(position: Int) {
        val fileViewDialog = LockFileViewDialog()
        fileViewDialog.arguments = Bundle().apply {
            putSerializable("LOCK_FILE", adapter.lockedFilesList[position])
        }
        fileViewDialog.show(supportFragmentManager, "FILE_VIEW_DIALOG")
    }

    private fun onItemSelected(itemView: View, position: Int) {
        if (adapter.checkedItems[position]) {
            adapter.checkedItems[position] = false
            viewModel.numOfSelectedItems--
        }
        else{
            adapter.checkedItems[position] = true
            viewModel.numOfSelectedItems++
        }
        adapter.setItemBackground(itemView, position)

        if (viewModel.numOfSelectedItems == 0) {
            actionMode?.finish()
        } else{
            actionMode?.title = "${viewModel.numOfSelectedItems} selected"
        }
    }
    @SuppressLint("NotifyDataSetChanged")
    private fun startActionMode() {
        // hiding more option button from all items
        adapter.isInActionMode = true
        adapter.notifyDataSetChanged()
        val actionModeCallBack = object : ActionMode.Callback {
            override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                menuInflater.inflate(R.menu.locked_files_context_menu, menu)
                return true
            }

            override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                return true
            }

            override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
                if (item != null) {
                    if (item.title == "Delete") {
                        deleteItems()
                    } else if (item.title == "Select All") {
                        if (viewModel.numOfSelectedItems == adapter.lockedFilesList.size) {
                            adapter.checkedItems.fill(false)
                            viewModel.numOfSelectedItems = 0
                        } else {
                            adapter.checkedItems.fill(true)
                            viewModel.numOfSelectedItems = adapter.lockedFilesList.size
                        }
                        adapter.notifyDataSetChanged()
                        actionMode?.title = "${viewModel.numOfSelectedItems} selected"
                    }
                }
                return true
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun onDestroyActionMode(mode: ActionMode?) {
                adapter.checkedItems.fill(false)
                adapter.isInActionMode = false
                adapter.notifyDataSetChanged()
                actionMode = null
                bindings.floatingBtnFilesActivity.visibility = View.VISIBLE
                viewModel.numOfSelectedItems = 0
            }
        }

        actionMode = startSupportActionMode(actionModeCallBack as ActionMode.Callback)
        bindings.floatingBtnFilesActivity.visibility = View.INVISIBLE
    }

    fun shareAllSelectedFiles() {
        val filesToShare = ArrayList<Uri>()
        for (i in adapter.checkedItems.indices) {
            if (adapter.checkedItems[i]) {
                filesToShare.add(Uri.parse(adapter.lockedFilesList[i].path))
            }
        }
        val intent = Intent(Intent.ACTION_SEND_MULTIPLE)
        intent.type = "*/*"
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, filesToShare)
        startActivity(Intent.createChooser(intent, "Share Files"))
    }

    private fun deleteItems() {
        val dialog = MaterialAlertDialogBuilder(this)
        dialog.setTitle("Delete")
        dialog.setMessage("Are you sure you want to delete the selected items?")
        dialog.setPositiveButton("Yes") { _, _ ->
            deleteSelectedItems()
            Toast.makeText(this, "Items Deleted", Toast.LENGTH_SHORT).show()
            actionMode?.finish()
        }
        dialog.setNegativeButton("No") { _, _ -> }
        dialog.show()
    }

    private fun deleteSelectedItems() {
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
                adapter.notifyItemRemoved(i)
            }
        }
    }

    override fun onFileUnlocked(id : Int, position: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val file = File(adapter.lockedFilesList[position].path)
            var newFile = File(file.parent, adapter.lockedFilesList[position].name)
            var i = 1
            while (newFile.exists()) {
                newFile = File(file.parent, adapter.lockedFilesList[position].name + "($i)")
                i++
            }
            file.renameTo(newFile)

            repository.setFileUnlocked(id, newFile.absolutePath)
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


        CoroutineScope(Dispatchers.IO).launch {
            try{
                updateTimeOnAllItems()
            }catch (e: Exception) {
                println("Exception on updateTime ${e.toString()}")
            }

            storeTimeInSharedPrefs(dateTime.toEpochSecond(ZonedDateTime.now().offset))
        }
    }

    private fun updateTimeOnAllItems() {
        adapter.updateTimeNow(viewModel.timeNow!!)
        for (i in 0 until adapter.itemCount){
            if (!adapter.lockedFilesList[i].isUnlocked) {
                adapter.notifyItemChanged(i)
            }
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

            val sortType = preferences.getString("sort_by", FileSort.DATE_UNLOCK.name)!!
            viewModel.sortBy = FileSort.valueOf(sortType)
            viewModel.isAscending = preferences.getBoolean("is_ascending", true)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.files_activity_menu, menu)
        when (viewModel.sortBy) {
            FileSort.NAME -> menu?.findItem(R.id.sort_by_name_files_activity)?.isChecked = true
            FileSort.SIZE -> menu?.findItem(R.id.sort_by_size_files_activity)?.isChecked = true
            FileSort.DATE_ADDED -> menu?.findItem(R.id.sort_by_date_added_files_activity)?.isChecked = true
            FileSort.DATE_UNLOCK -> menu?.findItem(R.id.sort_by_date_unlock_files_activity)?.isChecked = true
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
            R.id.sort_by_date_added_files_activity -> {
                handleSortAction(item, FileSort.DATE_ADDED)
            }
            R.id.sort_by_date_unlock_files_activity -> {
                handleSortAction(item, FileSort.DATE_UNLOCK)
            }
            R.id.sort_by_ascending_files_activity -> {
                handleOrderAction(item, true)
            }
            R.id.sort_by_descending_files_activity -> {
                handleOrderAction(item, false)
            }
            R.id.settings_activity_action_menu -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
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

    private fun setAppTheme() {
        val preferencesManager = PreferenceManager.getDefaultSharedPreferences(this)
        val theme = preferencesManager.getString("theme", "")
        val themeValues = resources.getStringArray(R.array.theme_values)
        if (theme == themeValues[1]) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }else if (theme == themeValues[2]) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }

    }

}