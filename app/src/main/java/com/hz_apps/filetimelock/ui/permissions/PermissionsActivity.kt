package com.hz_apps.filetimelock.ui.permissions

import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.hz_apps.filetimelock.databinding.ActivityPermissionsBinding

class PermissionsActivity : AppCompatActivity() {

    val permissions = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bindings = ActivityPermissionsBinding.inflate(layoutInflater)
        setContentView(bindings.root)
        supportActionBar?.hide()

        val requestPermissionLauncher = registerForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) {
            var allPermissionsGranted = true
            for (i in it.values) {
                if (!i) {
                    allPermissionsGranted = false
                }
            }
            if (allPermissionsGranted) {
                finish()
            }else{
                Toast.makeText(this, "Storage permission is required", Toast.LENGTH_SHORT).show()
            }
        }

        bindings.allowBtnPermission.setOnClickListener{

            requestPermissionLauncher.launch(permissions)

        }

        bindings.goBackBtnPermission.setOnClickListener {
            finish()
        }




    }

}