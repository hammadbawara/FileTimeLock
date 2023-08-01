package com.hz_apps.filetimelock.ui.files

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.hz_apps.filetimelock.R
import com.hz_apps.filetimelock.databinding.ActivityFilesBinding
import com.hz_apps.filetimelock.models.CustomPermission
import com.hz_apps.filetimelock.ui.file_picker.FilePickerActivity
import com.hz_apps.filetimelock.ui.permissions.PermissionsActivity

class FilesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bindings = ActivityFilesBinding.inflate(layoutInflater)
        setContentView(bindings.root)

        bindings.floatingBtnFilesActivity.setOnClickListener {
            // check storage permission
            val value = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            if (value == 0) {
                val storagePermission = CustomPermission(
                    "Storage Permission",
                    "This permission is required to access the storage of your device",
                    R.drawable.ic_stroage,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                )

                val permissions = ArrayList<CustomPermission>()
                permissions.add(storagePermission)

                val intent = Intent(this, PermissionsActivity::class.java)
                intent.putExtra("permission", permissions)
                startActivity(intent)
            }
            else {
                val intent = Intent(this, FilePickerActivity::class.java)
                startActivity(intent)
            }
        }


    }
}