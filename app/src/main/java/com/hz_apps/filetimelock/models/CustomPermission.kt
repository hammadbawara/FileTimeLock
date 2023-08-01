package com.hz_apps.filetimelock.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class CustomPermission(
    val name: String,
    val description: String,
    val icon: Int,
    val permission: String
) : Parcelable {
}