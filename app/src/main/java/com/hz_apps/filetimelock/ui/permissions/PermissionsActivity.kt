package com.hz_apps.filetimelock.ui.permissions

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.hz_apps.filetimelock.databinding.ActivityPermissionsBinding
import com.hz_apps.filetimelock.ui.lock_file.LockFileActivity

class PermissionsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bindings = ActivityPermissionsBinding.inflate(layoutInflater)
        setContentView(bindings.root)
        supportActionBar?.hide()

        val requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    val intent = Intent(this, LockFileActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {

                }
            }

        bindings.allowBtnPermission.setOnClickListener{
            requestPermissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        bindings.goBackBtnPermission.setOnClickListener {
            finish()
        }




    }

}