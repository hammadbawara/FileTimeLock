package com.hz_apps.filetimelock.ui.permissions

import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hz_apps.filetimelock.adapters.PermissionAdapter
import com.hz_apps.filetimelock.databinding.ActivityPermissionsBinding
import com.hz_apps.filetimelock.models.CustomPermission

class PermissionsActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bindings = ActivityPermissionsBinding.inflate(layoutInflater)
        setContentView(bindings.root)

        val intent = intent


        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableArrayListExtra("permission", CustomPermission::class.java) as ArrayList<CustomPermission>
        } else {
            intent.getParcelableArrayListExtra<CustomPermission>("permission") as ArrayList<CustomPermission>
        }

        val adapter = PermissionAdapter(permissions)

        bindings.permissionsRecyclerview.adapter = adapter


    }
}