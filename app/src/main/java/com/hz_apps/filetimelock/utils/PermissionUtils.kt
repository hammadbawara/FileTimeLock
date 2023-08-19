package com.hz_apps.filetimelock.utils

import android.content.Context
import android.content.pm.PackageManager

fun isStoragePermissionGranted(context : Context) : Boolean{
    val readPermission = android.Manifest.permission.READ_EXTERNAL_STORAGE
    val writePermission = android.Manifest.permission.WRITE_EXTERNAL_STORAGE

    if (context.checkSelfPermission(readPermission)== PackageManager.PERMISSION_GRANTED &&
        context.checkSelfPermission(writePermission) == PackageManager.PERMISSION_GRANTED) {
        return true
    }
    return false
}
